package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.SunPlankAbsortDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
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
import java.util.stream.Collectors;

@Slf4j
@Component
public class HighPlankReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;


    public void replay(){

        List<HighPlankReplayDTO> resultList = Lists.newArrayList();
        Map<String, List<String>> suddenMap = getHighPlankSuddenInfo();
        Map<String, List<HighPlankInfo>> highPlankInfoMap = getHighPlankInfo();
        Map<String, List<HighPlankInfo>> premiumMap = getHighPremiumMap();
        Map<String, List<HighPlankInfo>> plankThreePremiumMap = getPlankThreeMap();
        Map<String, List<String>> oneLinePlankMap = getOneLinePlankMap();

        Map<String,List<HighPlankInfo> > shakeMap = new HashMap<>();

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

            List<HighPlankInfo> plankThreePremiumList = plankThreePremiumMap.get(kbarDate);
            if(CollectionUtils.isEmpty(plankThreePremiumList)){
                exportDTO.setPlank3Count(0);
                exportDTO.setPlank3Premium(BigDecimal.ZERO);
            }else {
                exportDTO.setPlank3Count(plankThreePremiumList.size());
                double average = plankThreePremiumList.stream().map(HighPlankInfo::getPremium).mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
                exportDTO.setPlank3Premium(new BigDecimal(average).setScale(2,BigDecimal.ROUND_HALF_UP));
            }


            List<HighPlankInfo> shakeList;
            if(CollectionUtils.isEmpty(highPlankList)){
                shakeList = Lists.newArrayList();
            }else {
                shakeList = highPlankList.stream().filter(item -> item.getShakeRate().compareTo(new BigDecimal("2")) > 0).collect(Collectors.toList());
            }
            shakeMap.put(kbarDate,shakeList);
            resultList.add(exportDTO);



        });

        for (HighPlankReplayDTO highPlankReplayDTO : resultList) {
            String kbarDate = highPlankReplayDTO.getKbarDate();
            Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd));
            String preKbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
            List<HighPlankInfo> highPlankInfoList = shakeMap.get(preKbarDate);
            if(CollectionUtils.isEmpty(highPlankInfoMap)){
                continue;
            }
            List<String> stockList = suddenMap.get(preKbarDate);
            if(CollectionUtils.isEmpty(stockList)){
                highPlankReplayDTO.setSuddenCount(0);
            }else {
                highPlankReplayDTO.setSuddenCount(stockList.size());
            }
            if(CollectionUtils.isEmpty(highPlankInfoList)){
                highPlankReplayDTO.setShakeCount(0);
                highPlankReplayDTO.setTotalShakeRate(BigDecimal.ZERO);
            }else {
                highPlankReplayDTO.setShakeCount(highPlankInfoList.size());
                highPlankReplayDTO.setTotalShakeRate(highPlankInfoList.stream().map(HighPlankInfo::getShakeRate).reduce(BigDecimal::add).get());
            }
        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\连续板高位板情绪回测.xls");

    }

    private Map<String, List<HighPlankInfo>> getHighPlankInfo(){
        Map<String,List<HighPlankInfo>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20191220");
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
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    if(CollectionUtils.isEmpty(list)){
                        continue;
                    }
                    BigDecimal lowPrice = list.get(0).getTradePrice();
                    int lowIndex = 0;
                    for (int j = 0; j < list.size(); j++) {
                        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                        if(transactionDataDTO.getTradePrice().compareTo(lowPrice)<0){
                            lowIndex = j;
                            lowPrice = transactionDataDTO.getTradePrice();
                        }
                    }
                    BigDecimal highPrice = list.subList(0, lowIndex+1).stream().map(ThirdSecondTransactionDataDTO::getTradePrice).max(BigDecimal::compareTo).get();
                    BigDecimal shakeRate = PriceUtil.getPricePercentRate(highPrice.subtract(lowPrice),stockKbar.getClosePrice());


                    highPlankInfoList.add(new HighPlankInfo(sellStockKbar.getStockCode(),sellStockKbar.getStockName(),rate,shakeRate));
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

        private BigDecimal shakeRate;

        public HighPlankInfo(String stockCode, String stockName, BigDecimal premium, BigDecimal shakeRate) {
            this.stockCode = stockCode;
            this.stockName = stockName;
            this.premium = premium;
            this.shakeRate = shakeRate;
        }
    }

    public Map<String,List<String>> getHighPlankSuddenInfo(){
        Map<String,List<String>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20191210");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<20){
                continue;
            }
            for (int i = 24; i < kbarList.size(); i++) {
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar stockKbar = kbarList.get(i);
                if(!PriceUtil.isSuddenPrice(stockKbar.getStockCode(),stockKbar.getLowPrice(),preStockKbar.getClosePrice())){
                    continue;
                }
                PlankHighDTO plankHighDTO;
                for (int j = i; j > i-15; j--) {
                    plankHighDTO  = PlankHighUtil.calTodayPlank(kbarList.subList(j - 9, j + 1));
                    if(plankHighDTO.getPlankHigh()>=4){
                        List<String> stockCodeList = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k -> new ArrayList<>());
                        stockCodeList.add(stockKbar.getStockCode());
                        break;
                    }
                }

            }
        }


        return resultMap;


    }

    public Map<String,List<HighPlankInfo>> getPlankThreeMap(){
        Map<String,List<HighPlankInfo>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20191220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            kbarList = kbarList.stream().filter(item-> item.getTradeQuantity()>0).collect(Collectors.toList());
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
                int plank = PlankHighUtil.calSerialsPlank(kbarList.subList(i - 9, i ));
                if(plank == 2){
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
                            highPlankInfoList.add(new HighPlankInfo(stockKbar.getStockCode(),stockKbar.getStockName(),premium,BigDecimal.ZERO));
                        }
                    }
                }
            }
        }


        return resultMap;
    }


    public Map<String,List<HighPlankInfo>> getHighPremiumMap(){
        Map<String,List<HighPlankInfo>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20191220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            kbarList = kbarList.stream().filter(item-> item.getTradeQuantity()>0).collect(Collectors.toList());
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
               // PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList.subList(i - 9, i + 1));
                int plank = PlankHighUtil.calSerialsPlank(kbarList.subList(i - 9, i ));
                if(plank>=3){
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
                            highPlankInfoList.add(new HighPlankInfo(stockKbar.getStockCode(),stockKbar.getStockName(),premium,BigDecimal.ZERO));
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
            query.setKbarDateFrom("20191220");
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
