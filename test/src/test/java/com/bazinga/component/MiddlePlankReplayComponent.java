package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockCompeteDTO;
import com.bazinga.dto.IndexRateDTO;
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
import lombok.Data;
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
    private BlockReplayComponent blockReplayComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    public void invokeSecond(){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
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
          /*  if(!"002011".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20200407");
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
                List<StockKbar> kbar10List = stockKbars.subList(i - 10, i + 1);
                int plank = calSerialsPlank(kbarList);

                if(plank!=3 ){
                    continue;
                }

              /*  PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList);
                if(plankHighDTO.getPlankHigh()<2 || plankHighDTO.getPlankHigh() > 4 ){
                    continue;
                }*/
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                BigDecimal day10Rate = calDay10Rate(kbar10List);
              /*  boolean isUpperS = false;

                List<ThirdSecondTransactionDataDTO> plankList = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : plankList) {
                    if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType()==1){
                       // log.info("判断有涨停s stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                       // stockKbar.setClosePrice(stockKbar.getHighPrice());
                        isUpperS = true;
                        break;
                    }
                }*/

               /* if(!isUpperS){
                    log.info("没有出现涨停S stockCode{},kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
              /*  Integer oneLinePlank = PlankHighUtil.calOneLinePlank(kbarList);
                if(plank == oneLinePlank){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                /*if(oneLinePlank >= 2){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
               /* BigDecimal highLowRatio = getHighLowRatio(kbar15List,stockKbar.getAdjHighPrice());
                if(highLowRatio.compareTo(new BigDecimal("1.80"))>0){
                    log.info("满足大于1.8系数 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                log.info("满足3板条件 stockCode{} sellKbarDate{}", stockKbar.getStockCode(),sellStockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                list = historyTransactionDataComponent.getPreOneHourData(list);
                if(CollectionUtils.isEmpty(list)|| list.size()<3){
                    log.info("分时成交行情不足stockCode{} kbarDate{}",sellStockKbar.getStockCode(),sellStockKbar.getKbarDate());
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("preTradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(sellStockKbar.getStockCode(),stockKbar.getClosePrice());
               /* if(sellUpperPrice.compareTo(list.get(0).getTradePrice())==0){
                    continue;
                }*/
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        map.put("openAmount",transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                    }
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }

                dataList.add(map);
            }
        }

        getDiTianInfo();

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
            Map<String,BigDecimal> openRateMap = new HashMap<>();
            BigDecimal preTradeAmount = BigDecimal.ZERO;
            BigDecimal openAmount = BigDecimal.ZERO;
            for (Map itemMap : list) {
                preTradeAmount = preTradeAmount.add(new BigDecimal(itemMap.get("preTradeAmount").toString()));
                openAmount = openAmount.add(new BigDecimal(itemMap.get("openAmount").toString()));
            }
            map.put("preTradeAmount",preTradeAmount);
            map.put("openAmount",openAmount);
            map.put("shOpenRate",shOpenRateMap.get(key));
            for (int i = 5; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                BigDecimal minRate = new BigDecimal("20");
                BigDecimal maxRate = new BigDecimal("-20");
                List<BigDecimal> rateList = Lists.newArrayList();
                int overOpenCount = 0;
                for (Map itemMap : list) {
                    String stockCode = itemMap.get("stockCode").toString();
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    if(i==5){
                        openRateMap.put(stockCode,rate);
                    }
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                    BigDecimal relativeOpenRate = rate.subtract(openRateMap.get(stockCode));
                    if(relativeOpenRate.compareTo(BigDecimal.ZERO) > 0){
                        overOpenCount = overOpenCount + 1;
                    }
                    totalRate = totalRate.add(rate);

                    if(rate.compareTo(minRate)<0){
                        minRate = rate;
                    }
                    // rateList.add(rate);
                }
              /*  if(i>11 && i<18){
                    map.put("overOpen"+ attrKey,""+ overOpenCount +"/"+ list.size());
                }*/
                map.put(attrKey,totalRate);
            }
            exportList.add(map);
        });

        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\3板包含未封住去新股聚合走势.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public  void invokeOver3Rate(){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20200407");
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
                List<StockKbar> kbar10List = stockKbars.subList(i - 10, i + 1);
                int plank = calSerialsPlank(kbarList);

                if(plank!=3 ){
                    continue;
                }

              /*  PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList);
                if(plankHighDTO.getPlankHigh()<2 || plankHighDTO.getPlankHigh() > 4 ){
                    continue;
                }*/
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                BigDecimal day10Rate = calDay10Rate(kbar10List);
              /*  boolean isUpperS = false;

                List<ThirdSecondTransactionDataDTO> plankList = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : plankList) {
                    if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType()==1){
                       // log.info("判断有涨停s stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                       // stockKbar.setClosePrice(stockKbar.getHighPrice());
                        isUpperS = true;
                        break;
                    }
                }*/

               /* if(!isUpperS){
                    log.info("没有出现涨停S stockCode{},kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
              /*  Integer oneLinePlank = PlankHighUtil.calOneLinePlank(kbarList);
                if(plank == oneLinePlank){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                /*if(oneLinePlank >= 2){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
               /* BigDecimal highLowRatio = getHighLowRatio(kbar15List,stockKbar.getAdjHighPrice());
                if(highLowRatio.compareTo(new BigDecimal("1.80"))>0){
                    log.info("满足大于1.8系数 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                log.info("满足3板条件 stockCode{} sellKbarDate{}", stockKbar.getStockCode(),sellStockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                list = historyTransactionDataComponent.getPreOneHourData(list);
                if(CollectionUtils.isEmpty(list)|| list.size()<3){
                    log.info("分时成交行情不足stockCode{} kbarDate{}",sellStockKbar.getStockCode(),sellStockKbar.getKbarDate());
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("preTradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(sellStockKbar.getStockCode(),stockKbar.getClosePrice());
               /* if(sellUpperPrice.compareTo(list.get(0).getTradePrice())==0){
                    continue;
                }*/
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        map.put("openAmount",transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                    }
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }

                dataList.add(map);
            }
        }

        getDiTianInfo();

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
            Map<String,BigDecimal> openRateMap = new HashMap<>();
            BigDecimal preTradeAmount = BigDecimal.ZERO;
            BigDecimal openAmount = BigDecimal.ZERO;
            for (Map itemMap : list) {
                preTradeAmount = preTradeAmount.add(new BigDecimal(itemMap.get("preTradeAmount").toString()));
                openAmount = openAmount.add(new BigDecimal(itemMap.get("openAmount").toString()));
            }
            map.put("preTradeAmount",preTradeAmount);
            map.put("openAmount",openAmount);
            map.put("shOpenRate",shOpenRateMap.get(key));
            for (int i = 5; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                BigDecimal minRate = new BigDecimal("20");
                BigDecimal maxRate = new BigDecimal("-20");
                List<BigDecimal> rateList = Lists.newArrayList();
                int overOpenCount = 0;
                for (Map itemMap : list) {
                    String stockCode = itemMap.get("stockCode").toString();
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    if(i==5){
                        openRateMap.put(stockCode,rate);
                    }
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                    BigDecimal relativeOpenRate = rate.subtract(openRateMap.get(stockCode));
                    if(relativeOpenRate.compareTo(BigDecimal.ZERO) > 0){
                        overOpenCount = overOpenCount + 1;
                    }
                    totalRate = totalRate.add(rate);

                    if(rate.compareTo(minRate)<0){
                        minRate = rate;
                    }
                    // rateList.add(rate);
                }
              /*  if(i>11 && i<18){
                    map.put("overOpen"+ attrKey,""+ overOpenCount +"/"+ list.size());
                }*/
                map.put(attrKey,totalRate);
            }
            exportList.add(map);
        });

        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\3板包含未封住去新股聚合走势.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 昨日碰涨停聚合走势
     */
    public void prePlankGroupBy(){

        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getJiaQuanHeadList();
        excelExportUtil.setHeadKey(headList);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210415");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 15; i < stockKbars.size()-1; i++) {

                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                List<StockKbar> kbarList = stockKbars.subList(i - 7, i + 1);
                List<StockKbar> kbar15List = stockKbars.subList(i - 15, i + 1);
                List<StockKbar> kbar10List = stockKbars.subList(i - 10, i + 1);

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                log.info("满足做题碰涨停条件 stockCode{} sellKbarDate{}", stockKbar.getStockCode(),sellStockKbar.getKbarDate());

                BigDecimal openRate = PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice());
                if(openRate.compareTo(new BigDecimal("3")) <0 || openRate.compareTo(new BigDecimal("8"))>0){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                list = historyTransactionDataComponent.getPreOneHourData(list);
                if(CollectionUtils.isEmpty(list)|| list.size()<3){
                    log.info("分时成交行情不足stockCode{} kbarDate{}",sellStockKbar.getStockCode(),sellStockKbar.getKbarDate());
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("preTradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(sellStockKbar.getStockCode(),stockKbar.getClosePrice());
           /* if(sellUpperPrice.compareTo(list.get(0).getTradePrice())==0){
                continue;
            }*/
                BigDecimal totalTradeAmount = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getHighPrice()), stockKbar.getHighPrice());
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        map.put("openAmount",transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                    }
                    totalTradeAmount = totalTradeAmount.add(transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                    map.put(transactionDataDTO.getTradeTime(),rate);
                    map.put(transactionDataDTO.getTradeTime()+ "_trade",new BigDecimal(totalTradeAmount.toString()));
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
            Map<String,BigDecimal> openRateMap = new HashMap<>();
            BigDecimal preTradeAmount = BigDecimal.ZERO;
            BigDecimal openAmount = BigDecimal.ZERO;
            for (Map itemMap : list) {
                preTradeAmount = preTradeAmount.add(new BigDecimal(itemMap.get("preTradeAmount").toString()));
                String  openAmountString = itemMap.get("openAmount") == null ? "0":itemMap.get("openAmount").toString();
                openAmount = openAmount.add(new BigDecimal(openAmountString));
            }
            map.put("preTradeAmount",preTradeAmount);
            map.put("openAmount",openAmount);
            map.put("shOpenRate",shOpenRateMap.get(key));
            for (int i = 5; i < 15; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal totalTrade = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                BigDecimal preTrade = BigDecimal.ZERO;
                BigDecimal minRate = new BigDecimal("20");

                int overOpenCount = 0;
                for (Map itemMap : list) {
                    String stockCode = itemMap.get("stockCode").toString();
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    BigDecimal trade = itemMap.get(attrKey+"_trade") == null ? preTrade:new BigDecimal(itemMap.get(attrKey+"_trade").toString());
                    if(i==5){
                        openRateMap.put(stockCode,rate);
                    }
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                        preTrade = new BigDecimal(itemMap.get(attrKey+"_trade").toString());
                    }
                    BigDecimal relativeOpenRate = rate.subtract(openRateMap.get(stockCode));
                    if(relativeOpenRate.compareTo(BigDecimal.ZERO) > 0){
                        overOpenCount = overOpenCount + 1;
                    }
                    // totalRate = totalRate.add(rate);
                    totalRate = totalRate.add(rate);
                    totalTrade = totalTrade.add(trade);
                    if(rate.compareTo(minRate)<0){
                        minRate = rate;
                    }
                }
                map.put(attrKey,totalRate);
                map.put(attrKey+"_trade",totalTrade);
            }
            exportList.add(map);
        });

        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\触及涨停开3到8去新股聚合走势.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public void invoke(){
        Map<String, Integer> plankHighCountMap = commonReplayComponent.getPlankHighCountMap();

        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
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
        circulateInfos = circulateInfos.stream().filter(item->item.getStockCode().startsWith("0")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {
          /*  if(!"002011".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20200415");
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
                List<StockKbar> kbar10List = stockKbars.subList(i - 10, i + 1);
                int plank = calSerialsPlank(kbarList);

                if(plank<2 || plank > 4 ){
                    continue;
                }

              /*  PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList);
                if(plankHighDTO.getPlankHigh()<2 || plankHighDTO.getPlankHigh() > 4 ){
                    continue;
                }*/
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                BigDecimal day10Rate = calDay10Rate(kbar10List);
              /*  boolean isUpperS = false;

                List<ThirdSecondTransactionDataDTO> plankList = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : plankList) {
                    if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType()==1){
                       // log.info("判断有涨停s stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                       // stockKbar.setClosePrice(stockKbar.getHighPrice());
                        isUpperS = true;
                        break;
                    }
                }*/

               /* if(!isUpperS){
                    log.info("没有出现涨停S stockCode{},kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                Integer oneLinePlank = PlankHighUtil.calOneLinePlank(kbarList);
                if(plank == oneLinePlank){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                /*if(oneLinePlank >= 2){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                BigDecimal highLowRatio = getHighLowRatio(kbar15List,stockKbar.getAdjHighPrice());
                if(highLowRatio.compareTo(new BigDecimal("1.80"))>0){
                    log.info("满足大于1.8系数 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                log.info("满足中位股条件 stockCode{} sellKbarDate{}", stockKbar.getStockCode(),sellStockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                list = historyTransactionDataComponent.getPreOneHourData(list);
                if(CollectionUtils.isEmpty(list)|| list.size()<3){
                    log.info("分时成交行情不足stockCode{} kbarDate{}",sellStockKbar.getStockCode(),sellStockKbar.getKbarDate());
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("preTradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(sellStockKbar.getStockCode(),stockKbar.getClosePrice());
               /* if(sellUpperPrice.compareTo(list.get(0).getTradePrice())==0){
                    continue;
                }*/
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        map.put("openAmount",transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                    }
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }

                dataList.add(map);
            }
        }

        getDiTianInfo();

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
            Map<String,BigDecimal> openRateMap = new HashMap<>();
            BigDecimal preTradeAmount = BigDecimal.ZERO;
            BigDecimal openAmount = BigDecimal.ZERO;
            for (Map itemMap : list) {
                preTradeAmount = preTradeAmount.add(new BigDecimal(itemMap.get("preTradeAmount").toString()));
                String  openAmountString = itemMap.get("openAmount") == null ? "0":itemMap.get("openAmount").toString();
                openAmount = openAmount.add(new BigDecimal(openAmountString));
            }
            map.put("preTradeAmount",preTradeAmount);
            map.put("openAmount",openAmount);
            map.put("shOpenRate",shOpenRateMap.get(key));
            for (int i = 5; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                BigDecimal preRate = BigDecimal.ZERO;
                BigDecimal minRate = new BigDecimal("20");
                BigDecimal maxRate = new BigDecimal("-20");
                List<BigDecimal> rateList = Lists.newArrayList();
                int overOpenCount = 0;
                for (Map itemMap : list) {
                    String stockCode = itemMap.get("stockCode").toString();
                    BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                    if(i==5){
                        openRateMap.put(stockCode,rate);
                    }
                    if(itemMap.get(attrKey) != null){
                        preRate = new BigDecimal(itemMap.get(attrKey).toString());
                    }
                    BigDecimal relativeOpenRate = rate.subtract(openRateMap.get(stockCode));
                    if(relativeOpenRate.compareTo(BigDecimal.ZERO) > 0){
                        overOpenCount = overOpenCount + 1;
                    }
                    totalRate = totalRate.add(rate);
                    if(rate.compareTo(minRate)<0){
                        minRate = rate;
                    }
                   // rateList.add(rate);
                }
              /*  if(i>11 && i<18){
                    map.put("overOpen"+ attrKey,""+ overOpenCount +"/"+ list.size());
                }*/
                map.put(attrKey,totalRate);
            }
            exportList.add(map);
        });

        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\深触及涨停含未封住相对打板价加权去一字连板去新股含1.8.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getDiTianInfo() {





    }

    private boolean isFirstPlankOneLine(List<StockKbar> kbarList,Integer plank) {
        StockKbar stockKbar = kbarList.get(kbarList.size() - plank);
        List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(0);
        ThirdSecondTransactionDataDTO transactionDataDTO1 = list.get(1);
        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO1.getTradePrice().compareTo(stockKbar.getHighPrice())==0 &&
                transactionDataDTO1.getTradeType()==1){
            return true;
        }
        return false;
    }

    private boolean isOneLineOpen(StockKbar stockKbar, StockKbar preStockKbar) {
        List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(0);
        ThirdSecondTransactionDataDTO transactionDataDTO1 = list.get(1);
        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO1.getTradePrice().compareTo(stockKbar.getHighPrice())==0 &&
            transactionDataDTO1.getTradeType()==1){
            list=  historyTransactionDataComponent.getData(preStockKbar.getStockCode(),preStockKbar.getKbarDate());
            transactionDataDTO = list.get(0);
            transactionDataDTO1 = list.get(1);
            if(transactionDataDTO.getTradePrice().compareTo(preStockKbar.getHighPrice()) ==0 && transactionDataDTO1.getTradePrice().compareTo(preStockKbar.getHighPrice())==0 &&
                    transactionDataDTO1.getTradeType()==1){
                return true;
            }
        }

        return false;

    }

    private BigDecimal calDay10Rate(List<StockKbar> kbar15List) {
        StockKbar stockKbar = kbar15List.get(kbar15List.size() - 1);
        StockKbar day10StockKbar = kbar15List.get(0);

        if(stockKbar.getAdjFactor().compareTo(day10StockKbar.getAdjFactor())==0){
            return PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(day10StockKbar.getClosePrice()),day10StockKbar.getClosePrice());
        }else {
            return PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(day10StockKbar.getAdjClosePrice()),day10StockKbar.getAdjClosePrice());
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
      //  headList.add("stockCode");
      //  headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("preTradeAmount");
        headList.add("openAmount");
        headList.add("shOpenRate");

        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
            /*for (int i = 1; i < 21; i++) {
                headList.add(DateUtil.format(date,"HH:mm") + SymbolConstants.UNDERLINE +i);
            }*/
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

    private  String[] getJiaQuanHeadList(){
        List<String> headList = Lists.newArrayList();
        //  headList.add("stockCode");
        //  headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("preTradeAmount");
        headList.add("openAmount");
        headList.add("shOpenRate");

        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 10){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int trade = 0;
        while (trade< 10){
            date = DateUtil.addMinutes(date, 1);
            trade++;
            headList.add(DateUtil.format(date,"HH:mm")+ "_trade");
        }
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
