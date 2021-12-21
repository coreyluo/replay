package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.SunPlankAbsortDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Component
public class HighPlankReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;


    public void replay(){

        List<HighPlankReplayDTO> resultList = Lists.newArrayList();
        Map<String, List<HighPlankInfo>> premiumMap = getHighPremiumMap();
        Map<String, List<HighPlankInfo>> highPlankInfoMap = getHighPlankInfo();
        Map<String, List<String>> oneLinePlankMap = getOneLinePlankMap();

        highPlankInfoMap.forEach((kbarDate, highPlankList)->{
            List<String> oneLineList = oneLinePlankMap.get(kbarDate);
            BigDecimal openTotalPremium = BigDecimal.ZERO;

            for (HighPlankInfo highPlankInfo : highPlankList) {
                openTotalPremium = openTotalPremium.add(highPlankInfo.getPremium());
            }

            HighPlankReplayDTO exportDTO = new HighPlankReplayDTO();
            exportDTO.setKbarDate(kbarDate);
            exportDTO.setHighPlankNum(highPlankList.size());
            exportDTO.setOneLineNum(CollectionUtils.isEmpty(oneLineList)?0:oneLineList.size());
            exportDTO.setTotalPremium(openTotalPremium);

            BigDecimal moodValue = exportDTO.getTotalPremium().divide(new BigDecimal(exportDTO.getHighPlankNum().toString()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(exportDTO.getOneLineNum().toString())).setScale(2, RoundingMode.HALF_UP);
            exportDTO.setMoodValue(moodValue);

            List<HighPlankInfo> premiumList = premiumMap.get(kbarDate);
            if(CollectionUtils.isEmpty(premiumList)){
                exportDTO.setBuyCount(0);
                exportDTO.setAvgPremium(BigDecimal.ZERO);
            }else {
                exportDTO.setBuyCount(premiumList.size());
                double average = premiumList.stream().map(HighPlankInfo::getPremium).mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
                exportDTO.setAvgPremium(new BigDecimal(average).setScale(2,BigDecimal.ROUND_HALF_UP));
            }

            resultList.add(exportDTO);



        });
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\高位板情绪回测.xls");

    }

    private Map<String, List<HighPlankInfo>> getHighPlankInfo(){
        Map<String,List<HighPlankInfo>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<20){
                continue;
            }
            for (int i = 9; i < kbarList.size()-1; i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar sellStockKbar = kbarList.get(i+1);
                PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(kbarList.subList(i - 9, i + 1));
                if(plankHighDTO.getPlankHigh()>=4){
                    List<HighPlankInfo> highPlankInfoList = resultMap.computeIfAbsent(sellStockKbar.getKbarDate(), k -> new ArrayList<>());
                    BigDecimal rate = PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    highPlankInfoList.add(new HighPlankInfo(sellStockKbar.getStockCode(),sellStockKbar.getStockName(),rate));
                }
            }
        }
        return resultMap;
    }
    @Data
    class HighPlankInfo{

        private String stockCode;

        private String stockName;

        private BigDecimal premium;

        public HighPlankInfo(String stockCode, String stockName, BigDecimal premium) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.premium = premium;
        }
    }


    public Map<String,List<HighPlankInfo>> getHighPremiumMap(){
        Map<String,List<HighPlankInfo>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<20){
                continue;
            }
            for (int i = 9; i < kbarList.size()-1; i++) {
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar stockKbar = kbarList.get(i);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                StockKbar sellStockKbar = kbarList.get(i+1);
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList.subList(i - 9, i + 1));
                if(plankHighDTO.getPlankHigh()>=4){
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                    ThirdSecondTransactionDataDTO open = list.get(0);
                    if(open.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        open.setTradeType(1);
                    }
                    boolean upperSFlag= false;
                    for (int j = 1; j < list.size(); j++) {
                        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                        ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j-1);
                        if(transactionDataDTO.getTradeType()==1 && stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) ==0 ){
                            if(preTransactionDataDTO.getTradeType()!=1 || stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) <0){
                                upperSFlag = true;
                                break;
                            }
                        }
                    }
                    if(upperSFlag){
                        List<HighPlankInfo> highPlankInfoList = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k -> new ArrayList<>());
                        BigDecimal avgPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                        if(avgPrice!=null){
                            BigDecimal premium = PriceUtil.getPricePercentRate(avgPrice.subtract(stockKbar.getHighPrice()), stockKbar.getHighPrice());
                            highPlankInfoList.add(new HighPlankInfo(stockKbar.getStockCode(),stockKbar.getStockName(),premium));
                        }
                    }
                }
            }
        }


        return resultMap;
    }


    private Map<String, List<String>> getOneLinePlankMap(){
        Map<String,List<String>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<20){
                continue;
            }
            for (int i = 2; i < kbarList.size(); i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar prePreStockKbar = kbarList.get(i-2);
                if(!StockKbarUtil.isUpperPrice(preStockKbar,prePreStockKbar)){
                    continue;
                }
                if(PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getOpenPrice(),preStockKbar.getClosePrice())){
                    List<String> oneLinePlankList = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k -> new ArrayList<>());
                    oneLinePlankList.add(stockKbar.getStockCode());
                }
            }
        }
        return resultMap;
    }
}
