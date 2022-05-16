package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockIndustryDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockIndustryDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockIndustryDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockIndustryReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private ThsBlockIndustryDetailService thsBlockIndustryDetailService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public void replay(){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        List<ThsBlockIndustryDetail> thsBlockIndustryDetails = thsBlockIndustryDetailService.listByCondition(new ThsBlockIndustryDetailQuery());
        Map<String,List<String>> blockIndustryMap = new HashMap<>();
        for (ThsBlockIndustryDetail thsBlockIndustryDetail : thsBlockIndustryDetails) {
            List<String> list = blockIndustryMap.computeIfAbsent(thsBlockIndustryDetail.getBlockName(), k -> new ArrayList<>());
            list.add(thsBlockIndustryDetail.getStockCode().substring(2,8));
        }

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210501",DateUtil.yyyy_MM_dd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (int i = 1; i < tradeDatePools.size()-1; i++) {
            TradeDatePool preTradeDatePool = tradeDatePools.get(i-1);
            TradeDatePool tradeDatePool = tradeDatePools.get(i);
            TradeDatePool aftertradeDatePool = tradeDatePools.get(i+1);

            blockIndustryMap.forEach((blockName,stockList)->{

                List<String> groupByList = new ArrayList<>();
                Map<String,BigDecimal> preAmountMap = new HashMap<>();
                for (String stockCode : stockList) {
                    String uniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(preTradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar preStockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                    if(preStockKbar!=null){
                        preAmountMap.put(preStockKbar.getStockCode(),preStockKbar.getTradeAmount());
                    }
                }
                Map<String, BigDecimal> sortMap = SortUtil.sortByValue(preAmountMap);

                for (String stockCode : groupByList) {
                    String uniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                    if(stockKbar == null){
                        continue;
                    }
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                    if(CollectionUtils.isEmpty(list)){
                        continue;
                    }
                    list = historyTransactionDataComponent.getPreOneHourData(list);
                    if(CollectionUtils.isEmpty(list)|| list.size()<3){
                        log.info("分时成交行情不足stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                        continue;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("blockName",blockName);
                    map.put("preTradeAmount",preAmountMap.get(stockCode));
                    map.put("kbarDate",stockKbar.getKbarDate());
                    Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                    BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(stockKbar.getStockCode(),stockKbar.getClosePrice());
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                        if("09:25".equals(transactionDataDTO.getTradeTime())){
                            map.put("openAmount",transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                        }
                        map.put(transactionDataDTO.getTradeTime(),rate);
                    }

                    dataList.add(map);

                }
            });

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
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\同花顺行业走势.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        //  headList.add("stockCode");
        //  headList.add("stockName");
        headList.add("kbarDate");
        headList.add("blockName");
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

        }
        return headList.toArray(new String[]{});
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
