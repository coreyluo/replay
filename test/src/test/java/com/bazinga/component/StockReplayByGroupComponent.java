package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PlankDayDTO;
import com.bazinga.dto.RankDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateUtil;
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
import java.util.stream.Stream;

@Slf4j
@Component
public class StockReplayByGroupComponent {

    @Autowired
    private RedisMoniorService redisMoniorService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private BlockOpenReplayComponent blockOpenReplayComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    public void replayDayPlank(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));

        String[] headList = getPlankDayHeadList();
        excelExportUtil.setHeadKey(headList);
        Map<String, List<PlankDayDTO>> plankDayInfoMap = commonReplayComponent.getPlankDayInfoMap();
        List<Map> exportList = Lists.newArrayList();

        plankDayInfoMap.forEach((kbarDate,plankDayDTOList)->{
            Date afterTradeDate = commonComponent.afterTradeDate(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd));
            Map<String,BigDecimal> tempRateMap = new HashMap<>();

            plankDayDTOList.sort(Comparator.comparing(PlankDayDTO::getTradeAmount));
            plankDayDTOList = Lists.reverse(plankDayDTOList);
            int plankCount = 0;
            int unPlankCount = 0;
            BigDecimal plankAmount = BigDecimal.ZERO;
            BigDecimal unPlankAmount = BigDecimal.ZERO;

            int count = 0;
            for (PlankDayDTO plankDayDTO : plankDayDTOList) {
                if(plankDayDTO.getSealType()==1){
                    if(count<10){
                        plankCount++;
                        plankAmount = plankAmount.add(plankDayDTO.getTradeAmount());
                    }
                }else {
                    unPlankCount++;
                    unPlankAmount = unPlankAmount.add(plankDayDTO.getTradeAmount());
                    continue;
                }
                StockKbarQuery query = new StockKbarQuery();
                query.setStockCode(plankDayDTO.getStockCode());
                query.setKbarDateTo(kbarDate);
                query.addOrderBy("kbar_date", Sort.SortType.DESC);
                query.setLimit(2);
                List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                if(CollectionUtils.isEmpty(stockKbarList)){
                    continue;
                }

                StockKbar currentKbar = stockKbarList.get(0);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(plankDayDTO.getStockCode(), afterTradeDate);
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                if(count< 10){
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(currentKbar.getClosePrice()), currentKbar.getClosePrice());
                        //tempRateMap.put(plankDayDTO.getStockCode() + transactionDataDTO.getTradeTime(),rate.multiply(plankDayDTO.getTradeAmount()));
                        tempRateMap.put(plankDayDTO.getStockCode() + transactionDataDTO.getTradeTime(),rate);
                    }
                }
                count++;
            }
            Map map = new HashMap<>();
            map.put("kbarDate",DateUtil.format(afterTradeDate,DateUtil.yyyyMMdd));
            map.put("plankCount",plankCount);
            map.put("unPlankCount",unPlankCount);
            map.put("plankAmount",plankAmount);
            map.put("unPlankAmount",unPlankAmount);
            log.info("kbarDate{} 完成",DateUtil.format(afterTradeDate,DateUtil.yyyyMMdd));
            for (int i =5; i < headList.length; i++) {
                String attrKey = headList[i];
                BigDecimal totalRate = BigDecimal.ZERO;
                for (PlankDayDTO plankDayDTO : plankDayDTOList) {
                    BigDecimal rate = tempRateMap.get(plankDayDTO.getStockCode() + attrKey);
                    if(rate !=null){
                        totalRate = totalRate.add(rate);
                    }
                }
               // map.put(attrKey,totalRate.divide(plankAmount.add(unPlankAmount),2,BigDecimal.ROUND_HALF_UP));
                map.put(attrKey,totalRate);
            }
            exportList.add(map);


        });


        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\封住涨停票前10聚合走势"+".xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void replay(String kbarDateFrom ,String kbarDateTo){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));

        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);
        Map<String, List<RankDTO>> resultMap = blockOpenReplayComponent.getOpenAmountRank(kbarDateFrom, kbarDateTo);
        List<Map> exportList = Lists.newArrayList();

        resultMap.forEach((kbarDate,rankList)->{
          //  if("20210804".equals(kbarDate)){
                Map<String,BigDecimal> tempRateMap = new HashMap<>();
                for (RankDTO rankDTO : rankList) {
                    if(rankDTO.getRank()>50){
                        continue;
                    }
                    StockKbarQuery query = new StockKbarQuery();
                    query.setStockCode(rankDTO.getStockCode());
                    query.setKbarDateTo(kbarDate);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(2);
                    List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(stockKbarList)){
                        continue;
                    }
                    StockKbar preStockKbar = stockKbarList.get(1);
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(rankDTO.getStockCode(), kbarDate);
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                        tempRateMap.put(rankDTO.getStockCode() + transactionDataDTO.getTradeTime(),rate);
                    }
                }
                Map map = new HashMap<>();
                map.put("kbarDate",kbarDate);
                log.info("kbarDate{} 完成",kbarDate);
                for (int i =1; i < headList.length; i++) {
                    String attrKey = headList[i];
                    BigDecimal totalRate = BigDecimal.ZERO;
                    for (RankDTO rankDTO : rankList) {
                        BigDecimal rate = tempRateMap.get(rankDTO.getStockCode() + attrKey);
                        if(rate !=null){
                            totalRate = totalRate.add(rate);
                        }
                    }
                    map.put(attrKey,totalRate);
                }
                exportList.add(map);
           // }

        });


        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\竞价50聚合走势"+kbarDateFrom+"_"+kbarDateTo+".xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("kbarDate");
        headList.add("plankCount");
        headList.add("unPlankCount");
        headList.add("plankAmount");
        headList.add("unPlankAmount");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }

    private  String[] getPlankDayHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("kbarDate");
        headList.add("plankCount");
        headList.add("unPlankCount");
        headList.add("plankAmount");
        headList.add("unPlankAmount");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }


}
