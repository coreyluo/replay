package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BankerStockReplayDTO;
import com.bazinga.dto.PlankHighDTO;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BankerStockReplayComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<BankerStockReplayDTO> resultList = Lists.newArrayList();

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20211120");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item->item.getTradeQuantity()!=0).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<20){
                continue;
            }

            for (int i = 11; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar sellstockKbar = stockKbarList.get(i+1);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                List<StockKbar> pre10List = stockKbarList.subList(i - 11, i);

                RateAmountDTO rateAmountDTO = getAmountResult(pre10List);
                if(rateAmountDTO.getRateCount()>0){
                    PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(stockKbarList.subList(i - 10, i + 1));
                    log.info("满足条件 stockCode{} {}", circulateInfo.getStockCode(),JSONObject.toJSONString(rateAmountDTO));
                    BankerStockReplayDTO exportDTO = new BankerStockReplayDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                    exportDTO.setCirculate(circulateInfo.getCirculate());
                    exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    exportDTO.setBuyPrice(stockKbar.getHighPrice());
                    exportDTO.setRateCount(rateAmountDTO.getRateCount());
                    exportDTO.setRateKbarDate(rateAmountDTO.getKbarDate());
                    exportDTO.setRateTradeAmount(rateAmountDTO.getRateTradeAmount());
                    exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    exportDTO.setUnPlank(plankHighDTO.getUnPlank());

                    exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),10));

                    BigDecimal total10Amout = stockKbarList.subList(i - 10, i).stream().map(StockKbar::getTradeAmount).reduce(BigDecimal::add).get();
                    exportDTO.setDay10AvgTradeAmount(total10Amout.divide(new BigDecimal("10"),0,BigDecimal.ROUND_HALF_UP));
                    BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellstockKbar.getStockCode(), sellstockKbar.getKbarDate());
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    String uniqueKey = stockKbar.getStockCode() + SymbolConstants.UNDERLINE + rateAmountDTO.getKbarDate();
                    StockKbar byUniqueKey = stockKbarService.getByUniqueKey(uniqueKey);
                    exportDTO.setRateTradeAmount(byUniqueKey.getTradeAmount());
                    resultList.add(exportDTO);
                }
            }



        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\庄股分析.xls");


    }

    private RateAmountDTO getAmountResult(List<StockKbar> stockKbarList) {
        Map<String, BigDecimal> tradeAmountMap = new HashMap<>();
        BigDecimal maxTradeAmount = BigDecimal.ZERO;
        int resultRateCount = 0 ;
        String resultKbarDate ="";
        for (int i = 1; i < stockKbarList.size(); i++) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i-1);
            if(StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                continue;
            }
            BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice());
            if(highRate.compareTo(new BigDecimal("5")) < 0){
                continue;
            }

            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
            if(CollectionUtils.isEmpty(list)){
                continue;
            }
            Integer tiggerTimeInteger = 925;

            List<ThirdSecondTransactionDataDTO> resultList = Lists.newArrayList();
            int rateCount = 0;
            for (int j = 0; j < list.size(); j++) {
                ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);

                String tradeTime = transactionDataDTO.getTradeTime();
                Date date = DateUtil.parseDate(tradeTime, DateUtil.HH_MM);
                Date pre5MinDate = DateUtil.addMinutes(date,-4);
                Date after5MinDate = DateUtil.addMinutes(date,5);
                String preMin5Time = DateUtil.format(pre5MinDate,DateUtil.HH_MM);
                String afterMin5Time = DateUtil.format(after5MinDate,DateUtil.HH_MM);



                if(Integer.parseInt(preMin5Time.replaceAll(":",""))<930){
                    preMin5Time = "09:25";
                }

                ThirdSecondTransactionDataDTO fixTimeDataOne = historyTransactionDataComponent.getFixTimeDataOne(list, preMin5Time);


                BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(fixTimeDataOne.getTradePrice()), preStockKbar.getClosePrice());
                Integer currentTimeInteger  = Integer.parseInt(tradeTime.replaceAll(":",""));
                if(rate.compareTo(new BigDecimal("5"))>0 && currentTimeInteger> tiggerTimeInteger ){
                    Date tigerBeginDate = DateUtil.addMinutes(date,20);
                    String tigerBeginTime = DateUtil.format(tigerBeginDate,DateUtil.HH_MM);
                    if(Integer.parseInt(tigerBeginTime.replaceAll(":",""))> 1130 && Integer.parseInt(tigerBeginTime.replaceAll(":","")) < 1300){
                         tiggerTimeInteger = 1300+ Integer.parseInt(tigerBeginTime.replaceAll(":",""))-1130;
                    }else {
                        tiggerTimeInteger = Integer.parseInt(tigerBeginTime.replaceAll(":",""));
                    }
                    log.info("满足涨速大于5 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    rateCount++;
                    List<ThirdSecondTransactionDataDTO> rangeList = historyTransactionDataComponent.getRangeTimeData(list, preMin5Time, afterMin5Time);
                    resultList.addAll(rangeList);
                }

            }
            BigDecimal dayRateTradeAmount = BigDecimal.ZERO;
            if(!CollectionUtils.isEmpty(resultList)){

                for (ThirdSecondTransactionDataDTO transactionDataDTO : resultList) {
                    dayRateTradeAmount = dayRateTradeAmount.add(transactionDataDTO.getTradePrice().multiply(CommonConstant.DECIMAL_HUNDRED)
                            .multiply(new BigDecimal(transactionDataDTO.getTradeQuantity().toString())));
                }
            }
            if(dayRateTradeAmount.compareTo(maxTradeAmount)>0){
                maxTradeAmount = dayRateTradeAmount;
                resultRateCount = rateCount;
                resultKbarDate = stockKbar.getKbarDate();
            }



        }

        return new RateAmountDTO(resultKbarDate,maxTradeAmount,resultRateCount);

    }

    @Data
    class RateAmountDTO{
        private String kbarDate;

        private BigDecimal rateTradeAmount;

        private Integer rateCount;


        public RateAmountDTO(String kbarDate, BigDecimal rateTradeAmount, Integer rateCount) {
            this.kbarDate = kbarDate;
            this.rateTradeAmount = rateTradeAmount;
            this.rateCount = rateCount;
        }
    }
}
