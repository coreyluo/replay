package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MiddlePlankReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public  void invokeOver3Rate(){

        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210418");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 6; i < stockKbars.size()-2; i++) {

                // StockKbar aftstockKbar = stockKbars.get(i+1);
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                List<StockKbar> kbarList = stockKbars.subList(i - 6, i + 1);

                if(!StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                BigDecimal openRate = PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                if(openRate.compareTo(new BigDecimal("0"))<0 || openRate.compareTo(new BigDecimal("3"))>0 ){
                    continue;
                }
                log.info("满足板票涨幅大于3条件 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                dataList.add(map);
            }
        }

        Map<String,List<Map>> groupByMap = new HashMap<>();

        for (Map map : dataList) {
            List<Map> mapList = groupByMap.get(map.get("kbarDate").toString());
            if(mapList == null){
                mapList = new ArrayList<>();
            }
            mapList.add(map);
            groupByMap.put(map.get("kbarDate").toString(),mapList);
        }
        List<Map> exportList = Lists.newArrayList();
        groupByMap.forEach((key,list)->{
            Map map = new HashMap<>();
            map.put("kbarDate",key);
            map.put("count",list.size());
            for (int i = 2; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                for (Map itemMap : list) {
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    totalRate = totalRate.add(rate);
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                }
                map.put(attrKey,totalRate.divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
            }
            exportList.add(map);
        });

       /* for (TradeDatePool tradeDatePool : tradeDatePools) {
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
           // List<Map>

        }*/
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\plank0-3.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void invokeUnPlank(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getUnPlankHeadList();
        excelExportUtil.setHeadKey(headList);
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20200710");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 6; i < stockKbars.size()-2; i++) {

             //   StockKbar aftstockKbar = stockKbars.get(i+1);
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                List<StockKbar> kbarList = stockKbars.subList(i - 6, i + 1);
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    continue;
                }
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                int plank = calSerialsPlank(kbarList);
                if(plank<2){
                    continue;
                }
              /*  if(!PlankHighUtil.isTodayFirstPlank(kbarList)){
                    continue;
                }*/
                log.info("满足lianban炸板条件 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",stockKbar.getStockCode());
                map.put("stockName",stockKbar.getStockName());
                map.put("tradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",stockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                dataList.add(map);
            }
        }

        Map<String,List<Map>> groupByMap = new HashMap<>();

        for (Map map : dataList) {
            List<Map> mapList = groupByMap.get(map.get("kbarDate").toString());
            if(mapList == null){
                mapList = new ArrayList<>();
            }
            mapList.add(map);
            groupByMap.put(map.get("kbarDate").toString(),mapList);
        }
        List<Map> exportList = Lists.newArrayList();
        groupByMap.forEach((key,list)->{
            Map map = new HashMap<>();
            map.put("kbarDate",key);
            map.put("count",list.size());
            String attrKey = headList[2];
            BigDecimal totalTradeAmount = BigDecimal.ZERO;
            for (Map itemMap : list) {
                BigDecimal tradeAmount = new BigDecimal(itemMap.get(attrKey).toString());
                totalTradeAmount = totalTradeAmount.add(tradeAmount);
            }
            map.put(attrKey,totalTradeAmount);
           /* for (int i = 2; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                for (Map itemMap : list) {
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    totalRate = totalRate.add(rate);
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                }
                map.put(attrKey,totalRate.divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
            }*/
            exportList.add(map);
        });

       /* for (TradeDatePool tradeDatePool : tradeDatePools) {
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
           // List<Map>

        }*/
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\碰板成交额.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void invoke(){

        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210401");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 15; i < stockKbars.size()-1; i++) {

               // StockKbar aftstockKbar = stockKbars.get(i+1);
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                List<StockKbar> kbarList = stockKbars.subList(i - 7, i + 1);
                List<StockKbar> kbar15List = stockKbars.subList(i - 15, i + 1);

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

                if(stockKbar.getHighPrice().compareTo(stockKbar.getClosePrice())!=0){
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType()==1){
                            log.info("判断有涨停s stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                            stockKbar.setClosePrice(stockKbar.getHighPrice());
                            break;
                        }
                    }
                }
                int plank = calSerialsPlank(kbarList);
                if(plank<2 || plank >4){
                    continue;
                }
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                BigDecimal highLowRatio = getHighLowRatio(kbar15List,stockKbar.getAdjHighPrice());
                if(highLowRatio.compareTo(new BigDecimal("1.80"))>0){
                    log.info("满足大于1.8系数 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                /*      BigDecimal openRate = PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                if(openRate.compareTo(new BigDecimal("0"))>0  ){
                    continue;
                }*/
                log.info("满足中位股条件 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                dataList.add(map);
            }
        }

        Map<String,List<Map>> groupByMap = new HashMap<>();

        for (Map map : dataList) {
            List<Map> mapList = groupByMap.get(map.get("kbarDate").toString());
            if(mapList == null){
                mapList = new ArrayList<>();
            }
            mapList.add(map);
            groupByMap.put(map.get("kbarDate").toString(),mapList);
        }
        List<Map> exportList = Lists.newArrayList();
        groupByMap.forEach((key,list)->{
            Map map = new HashMap<>();
            map.put("kbarDate",key);
            map.put("count",list.size());
            for (int i = 2; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                for (Map itemMap : list) {
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    totalRate = totalRate.add(rate);
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                }
                map.put(attrKey,totalRate.divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
            }
            exportList.add(map);
        });

       /* for (TradeDatePool tradeDatePool : tradeDatePools) {
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
           // List<Map>

        }*/
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\middlePlank包含炸板纯粹连板去20天新股含1.8.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BigDecimal getHighLowRatio(List<StockKbar> kbar15List,BigDecimal adjHighPrice) {
        BigDecimal lowestPrice= kbar15List.get(0).getAdjLowPrice();
        for (StockKbar stockKbar : kbar15List) {
            if(stockKbar.getAdjLowPrice().compareTo(BigDecimal.ZERO)<0){
                lowestPrice = stockKbar.getAdjLowPrice();
            }
        }
        return adjHighPrice.divide(lowestPrice,2, BigDecimal.ROUND_HALF_UP);
    }


    private  String[] getUnPlankHeadList(){
        List<String> headList = Lists.newArrayList();
        //    headList.add("stockCode");
        //    headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("tradeAmount");
       /* headList.add("09:25");
        headList.add("09:30");
        Date date = DateUtil.parseDate("20210818093000", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }*/
     /*   headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }*/
        return headList.toArray(new String[]{});
    }

    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
    //    headList.add("stockCode");
    //    headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("09:25");
        headList.add("09:30");
        Date date = DateUtil.parseDate("20210818093000", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
     /*   headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }*/
        return headList.toArray(new String[]{});
    }
    private int calSerialsPlankDuan(List<StockKbar> stockKbarList) {
        int planks = 1;
        int unPlanks = 0;
        for (int i = stockKbarList.size() - 1; i > 2; i--) {
            StockKbar stockKbar = stockKbarList.get(i-1);
            StockKbar preStockKbar = stockKbarList.get(i - 2);
            StockKbar prePreStockKbar = stockKbarList.get(i - 3);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
                continue;
            } else {
                //unPlanks++;
                if(StockKbarUtil.isUpperPrice(preStockKbar, prePreStockKbar)){
                    return 11;
                }else {
                    return planks;
                }
            }
          /*  if(unPlanks>=2){
                return planks;
            }*/
        }
        return planks;
    }

    private int calSerialsPlank(List<StockKbar> stockKbarList) {
        int planks = 1;
        int unPlanks = 0;
        for (int i = stockKbarList.size() - 1; i > 1; i--) {
            StockKbar stockKbar = stockKbarList.get(i-1);
            StockKbar preStockKbar = stockKbarList.get(i - 2);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
                continue;
            } else {
                //unPlanks++;
                return planks;
            }
          /*  if(unPlanks>=2){
                return planks;
            }*/
        }
        return planks;
    }
}
