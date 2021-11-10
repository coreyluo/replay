package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.dto.SellReplayImportDTO;
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
import com.bazinga.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SellReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    public void replayMarket(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<SellReplayImportDTO> importList = Lists.newArrayList();
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210418");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<8){
                continue;
            }
            for (int i = 7; i < kbarList.size()-1; i++) {
                StockKbar buyStockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar sellStockKbar = kbarList.get(i+1);
                List<StockKbar> subList = kbarList.subList(i - 7, i + 1);
                if(!StockKbarUtil.isHighUpperPrice(buyStockKbar,preStockKbar)){
                    continue;
                }
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(subList);
                if(plankHighDTO.getPlankHigh() > 2){
                    continue;
                }
                if(buyStockKbar.getHighPrice().compareTo(buyStockKbar.getClosePrice())==0){
                    continue;
                }
                log.info("满足炸板条件 stockCode{} kbarDate{}",buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                SellReplayImportDTO importDTO = new SellReplayImportDTO();
                importDTO.setKbarDate(DateUtil.parseDate(buyStockKbar.getKbarDate(),DateUtil.yyyyMMdd));
                importDTO.setSealType(0);
                importDTO.setStockCode(buyStockKbar.getStockCode());
                importDTO.setStockName(buyStockKbar.getStockName());
                importList.add(importDTO);

            }


        }
        replay(importList);

    }

    public void replayExcel(){


        File file = new File("E:/excelExport/陈1109.xlsx");
        try {
            List<SellReplayImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SellReplayImportDTO.class);
            replay(importList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replay(List<SellReplayImportDTO> importList){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));

        //  excelExportUtil.setData(dataList);
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);
        List<Map> dataList = new ArrayList<>();
        try {
            for (SellReplayImportDTO sellReplayImportDTO : importList) {
                if(sellReplayImportDTO.getSealType()!=0){
                    continue;
                }
                String currentKbarString = DateUtil.format(sellReplayImportDTO.getKbarDate(),DateUtil.yyyyMMdd);
                String uniqueKey =  sellReplayImportDTO.getStockCode() + SymbolConstants.UNDERLINE + currentKbarString;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);

                Date sellDate = commonComponent.afterTradeDate(sellReplayImportDTO.getKbarDate());
                String sellDateString = DateUtil.format(sellDate,DateUtil.yyyyMMdd);
                Map<String,Object> map = new HashMap<>();
                map.put("stockCode",sellReplayImportDTO.getStockCode());
                map.put("stockName",sellReplayImportDTO.getStockName());
                BigDecimal openPrice = BigDecimal.ZERO;
                map.put("sellDate",sellDateString);
                map.put("buyDate",currentKbarString);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellReplayImportDTO.getStockCode(), sellDateString);
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        openPrice = transactionDataDTO.getTradePrice();
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                map.put("openRate", PriceUtil.getPricePercentRate(openPrice.subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                dataList.add(map);
            }

            Map<String,List<Map>> groupByMap = new HashMap<>();

            for (Map map : dataList) {
                List<Map> mapList = groupByMap.get(map.get("sellDate").toString());
                if(mapList == null){
                    mapList = new ArrayList<>();
                }
                mapList.add(map);
                groupByMap.put(map.get("sellDate").toString(),mapList);
            }

            List<Map> exportList = Lists.newArrayList();
           /* groupByMap.forEach((key,list)->{
                Map map = new HashMap<>();
                map.put("sellDate",key);
                map.put("count",list.size());
                for (int i = 3; i < headList.length; i++) {
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
                  //  map.put(attrKey,totalRate.divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
                    map.put(attrKey,totalRate);
                }
                exportList.add(map);
            });*/
           daysGroupBy(groupByMap,exportList);
            excelExportUtil.setData(exportList);
            excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
            excelExportUtil.writeMainData(1);

            try {
                FileOutputStream output=new FileOutputStream("E:\\excelExport\\未封住封住卖出5日聚合市场.xls");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void daysGroupBy( Map<String,List<Map>> groupByMap, List<Map> exportList){
        String[] headList = getHeadList();
        Map<String,Map> tempMap = Maps.newHashMap();
        groupByMap.forEach((key,list)->{
            Map map = new HashMap<>();
            map.put("sellDate",key);
            map.put("count",list.size());
            for (int i = 3; i < headList.length; i++) {
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
                map.put(attrKey,totalRate);
            }
            tempMap.put(key,map);
          //  exportList.add(map);
        });

        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(DateUtil.parseDate("20210519",DateUtil.yyyyMMdd)));
        query.setTradeDateTo(DateTimeUtils.getDate000000(DateUtil.parseDate("20211108",DateUtil.yyyyMMdd)));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> tradeDateList = tradeDatePools.stream().map(item -> DateUtil.format(item.getTradeDate(), DateUtil.yyyyMMdd)).collect(Collectors.toList());
        for (int i = 4; i < tradeDateList.size(); i++) {
            List<String> day5List = tradeDateList.subList(i - 4, i + 1);
            String currentSellDate = tradeDateList.get(i);
            Map<String,Object> exportMap = new HashMap<>();
            exportMap.put("sellDate",currentSellDate);
            int overOpen = 0;
            int count = 0;
            for (String kbarDate : day5List) {
                Map map = tempMap.get(kbarDate);
                if(new BigDecimal(map.get("13:00").toString()).compareTo(new BigDecimal(map.get("09:25").toString()))>0){
                    overOpen++;
                }
                count = count + Integer.valueOf(map.get("count").toString());
            }
            exportMap.put("count",count);
            for (int j = 3; j < headList.length; j++) {
                BigDecimal totalRate = BigDecimal.ZERO;
                for (String kbarDate : day5List) {
                    Map map = tempMap.get(kbarDate);
                    totalRate = totalRate.add(new BigDecimal(map.get(headList[j]).toString()));
                }
                BigDecimal avgRate = totalRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                exportMap.put(headList[j],avgRate);
            }

            exportMap.put("overCount",overOpen);
            exportList.add(exportMap);

        }


    }



    public void replayStock(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = new ArrayList<>();
        //  excelExportUtil.setData(dataList);
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        File file = new File("E:/excelExport/陈1109.xlsx");
        try {
            List<SellReplayImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SellReplayImportDTO.class);
            for (SellReplayImportDTO sellReplayImportDTO : importList) {
                String currentKbarString = DateUtil.format(sellReplayImportDTO.getKbarDate(),DateUtil.yyyyMMdd);
                String uniqueKey =  sellReplayImportDTO.getStockCode() + SymbolConstants.UNDERLINE + currentKbarString;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);

                Date sellDate = commonComponent.afterTradeDate(sellReplayImportDTO.getKbarDate());
                String sellDateString = DateUtil.format(sellDate,DateUtil.yyyyMMdd);
                Map<String,Object> map = new HashMap<>();
                map.put("stockCode",sellReplayImportDTO.getStockCode());
                map.put("stockName",sellReplayImportDTO.getStockName());
                BigDecimal openPrice = BigDecimal.ZERO;
                map.put("sellDate",sellDateString);
                map.put("buyDate",currentKbarString);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellReplayImportDTO.getStockCode(), sellDateString);
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        openPrice = transactionDataDTO.getTradePrice();
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                map.put("openRate", PriceUtil.getPricePercentRate(openPrice.subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                dataList.add(map);
            }




            excelExportUtil.setData(dataList);
            excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
            excelExportUtil.writeMainData(1);

            try {
                FileOutputStream output=new FileOutputStream("E:\\excelExport\\卖出烂板2板均值.xls");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
     //   headList.add("stockCode");
     //   headList.add("stockName");
     //   headList.add("openRate");
        headList.add("sellDate");
        headList.add("count");
        headList.add("overCount");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210531092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }

        date = DateUtil.parseDate("20210531125900", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }
}
