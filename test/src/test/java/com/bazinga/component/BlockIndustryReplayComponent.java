package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.BigExchangeTestBuyDTO;
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


    public void printShRateRelaticeFixTime(){
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (TradeDatePool tradeDatePool : tradeDatePools) {
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData("999999", tradeDatePool.getTradeDate());
            Date date = DateUtil.parseDate("20210818093000", DateUtil.yyyyMMddHHmmss);
            int count = 0;

            List<String> headList = new ArrayList<>();
            while (count< 8){
                date = DateUtil.addMinutes(date, 1);
                count++;
                headList.add(DateUtil.format(date,"HH:mm"));

            }
            List<BigDecimal> rateList = new ArrayList<>();
            ThirdSecondTransactionDataDTO close = list.get(list.size() - 1);
            for (String timeStr : headList) {
                if("20220518".equals(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd))){
                    tradeDatePool.getTradeDate();
                }
                ThirdSecondTransactionDataDTO fixTimeData = historyTransactionDataComponent.getFixTimeDataOne(list, timeStr);
                BigDecimal rate = PriceUtil.getPricePercentRate(close.getTradePrice().subtract(fixTimeData.getTradePrice()), fixTimeData.getTradePrice());
                rateList.add(rate);
            }
            log.info("kbarDate{} 收益{}",DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd),JSONObject.toJSONString(rateList));
        }




    }


    public void replay(String kbarDateFrom , String kbarDateTo){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        Map<String, CirculateInfo> circulateInfoMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            circulateInfoMap.put(circulateInfo.getStockCode(),circulateInfo);
        }


        List<ThsBlockIndustryDetail> thsBlockIndustryDetails = thsBlockIndustryDetailService.listByCondition(new ThsBlockIndustryDetailQuery());
        Map<String,List<String>> blockIndustryMap = new HashMap<>();
        for (ThsBlockIndustryDetail thsBlockIndustryDetail : thsBlockIndustryDetails) {
            if(thsBlockIndustryDetail.getBlockName() == null){
                continue;
            }
            List<String> list = blockIndustryMap.computeIfAbsent(thsBlockIndustryDetail.getBlockName(), k -> new ArrayList<>());
            list.add(thsBlockIndustryDetail.getStockCode().substring(2,8));
        }

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate(kbarDateFrom,DateUtil.yyyyMMdd));
        tradeDateQuery.setTradeDateTo(DateUtil.parseDate(kbarDateTo,DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);
        List<Map> exportList = Lists.newArrayList();

        for (int i = 1; i < tradeDatePools.size()-1; i++) {
            TradeDatePool preTradeDatePool = tradeDatePools.get(i-1);
            TradeDatePool tradeDatePool = tradeDatePools.get(i);
            TradeDatePool aftertradeDatePool = tradeDatePools.get(i+1);

            blockIndustryMap.forEach((blockName,stockList)->{
                List<Map> dataList = Lists.newArrayList();
                int buyCount =0;
                BigDecimal premium = BigDecimal.ZERO;
                for (String stockCode : stockList) {
                    StockKbarQuery query = new StockKbarQuery();
                    query.setStockCode(stockCode);
                    query.setKbarDateTo(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(7);
                    List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<7){
                        continue;
                    }
                    StockKbar stockKbar = stockKbarList.get(0);
                    StockKbar preStockKbar = stockKbarList.get(1);
                    if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                        continue;
                    }

                    if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())==0){
                        continue;
                    }

                    int plank = calSerialsPlank(stockKbarList);
                    if(plank!=1){
                        continue;
                    }
                    String uniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(aftertradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar sellStockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                    if(sellStockKbar == null || sellStockKbar.getTradeQuantity()==0){
                        log.info("停牌 stockCode{} kbarDate{}",stockCode,DateUtil.format(aftertradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
                        continue;
                    }
                    buyCount++;
                    BigDecimal sellPrice = historyTransactionDataComponent.calAvgPrice(stockCode, aftertradeDatePool.getTradeDate());
                    if(sellPrice!=null){
                        BigDecimal sellRate = PriceUtil.getPricePercentRate(sellPrice.subtract(stockKbar.getHighPrice()),stockKbar.getHighPrice());
                        premium = premium.add(sellRate);
                    }
                }
                List<String> groupByList = new ArrayList<>();
                Map<String,BigDecimal> preAmountMap = new HashMap<>();
                for (String stockCode : stockList) {
                    String uniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(preTradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar preStockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                    CirculateInfo circulateInfo = circulateInfoMap.get(stockCode);
                    if(preStockKbar!=null && circulateInfo!=null /*&&  preStockKbar.getClosePrice().floatValue() * circulateInfo.getCirculate()  < 300 * CommonConstant.ONE_BILLION*/){
                        preAmountMap.put(preStockKbar.getStockCode(),preStockKbar.getTradeAmount());
                    }
                }
                Map<String, BigDecimal> sortMap = SortUtil.sortByValue(preAmountMap);
                int count = 0;
                for (Map.Entry<String, BigDecimal> entry : sortMap.entrySet()) {
                    if(count<10){
                        groupByList.add(entry.getKey());
                    }
                    count++;
                }
                String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
                String preKbarDate = DateUtil.format(preTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
                Map<String,Object> exportMap = new HashMap<>();
                exportMap.put("kbarDate",kbarDate);
                exportMap.put("blockName",blockName);
                exportMap.put("shOpenRate",shOpenRateMap.get(kbarDate));
                exportMap.put("canBuyCount",buyCount);
                exportMap.put("totalPremium",premium);
                BigDecimal preTradeAmount = BigDecimal.ZERO;
                if("汽车零部件".equals(blockName) && "20220513".equals(kbarDate)){
                    log.info("汽车零部件{}", JSONObject.toJSONString(groupByList));
                }

                for (String stockCode : groupByList) {
                    preTradeAmount = preTradeAmount.add(preAmountMap.get(stockCode));
                    String uniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate;
                    String preUniqueKey = stockCode + SymbolConstants.UNDERLINE + preKbarDate;
                    StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                    StockKbar preStockKbar = stockKbarService.getByUniqueKey(preUniqueKey);
                    if(stockKbar == null || preStockKbar == null){
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

                    Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                    BigDecimal openAmount = BigDecimal.ZERO;
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                        if("09:25".equals(transactionDataDTO.getTradeTime())){
                            openAmount = openAmount.add(transactionDataDTO.getTradePrice().multiply(new BigDecimal(String.valueOf(transactionDataDTO.getTradeQuantity()*100))));
                        }
                        map.put(transactionDataDTO.getTradeTime(),rate);
                    }
                    map.put("openAmount",openAmount);

                    dataList.add(map);
                }
                exportMap.put("preTradeAmount",preTradeAmount);
                BigDecimal openAmount = BigDecimal.ZERO;
                for (Map itemMap : dataList) {
                    if(itemMap.get("openAmount")!=null){
                        openAmount = openAmount.add(new BigDecimal(itemMap.get("openAmount").toString()));
                    }
                }

                for (int j = 7; j < headList.length; j++) {
                    String attrKey = headList[j];
                    BigDecimal preRate = BigDecimal.ZERO;
                    BigDecimal totalRate = BigDecimal.ZERO;
                    for (Map itemMap : dataList) {
                        BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                        if(itemMap.get(attrKey) != null){
                            preRate = new BigDecimal(itemMap.get(attrKey).toString());
                        }
                        totalRate = totalRate.add(rate);
                    }
                    exportMap.put(attrKey,totalRate);
                }
                exportMap.put("openAmount",openAmount);
                exportList.add(exportMap);
            });
        }
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\同花顺行业"+kbarDateFrom+"_"+kbarDateTo+"走势.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("kbarDate");
        headList.add("blockName");
        headList.add("preTradeAmount");
        headList.add("openAmount");
        headList.add("shOpenRate");
        headList.add("canBuyCount");
        headList.add("totalPremium");

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
        for (int i = stockKbarList.size() - 1; i > 1; i--) {
            StockKbar stockKbar = stockKbarList.get(i-1);
            StockKbar preStockKbar = stockKbarList.get(i - 2);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
            } else {
                return planks;
            }
        }
        return planks;
    }

}
