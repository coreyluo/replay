package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockCommonReplayService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.exception.TradeException;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class StockCommonReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockCommonReplayService stockCommonReplayService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;


    public void saveCommonReplay(Date date){

        try {
            List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
            Date currentDate = date;
            if(!commonComponent.isTradeDate(currentDate)){
                log.info("当前日期不是交易日期");
                return;
            }
            Date preTradeDate = commonComponent.preTradeDate(currentDate);
            String currentKbarDate = DateUtil.format(currentDate,DateUtil.yyyyMMdd);
            for (CirculateInfo circulateInfo : circulateInfos) {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 2);
                List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(dataTable);

                for (KBarDTO kBarDTO : kBarDTOS) {
                    if(DateUtil.format(preTradeDate,DateUtil.yyyy_MM_dd).equals(kBarDTO.getDateStr())){
                        BigDecimal closePrice = kBarDTO.getEndPrice();
                        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), currentDate);
                        List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getPreOneHourData(data);
                        Float averagePrice = historyTransactionDataComponent.calAveragePrice(list);
                        String uniqueKey = circulateInfo.getStockCode() + SymbolConstants.UNDERLINE + currentKbarDate;
                        StockCommonReplay byUniqueKey = stockCommonReplayService.getByUniqueKey(uniqueKey);
                        if(byUniqueKey == null){

                            StockCommonReplay stockCommonReplay = new StockCommonReplay();
                            stockCommonReplay.setStockCode(circulateInfo.getStockCode());
                            stockCommonReplay.setStockName(circulateInfo.getStockName());
                            stockCommonReplay.setAvgPre1Price(new BigDecimal(averagePrice.toString()));
                            stockCommonReplay.setKbarDate(currentKbarDate);
                            stockCommonReplay.setUniqueKey(uniqueKey);
                            stockCommonReplay.setAvgPre1Rate(PriceUtil.getPricePercentRate(new BigDecimal(averagePrice.toString()).subtract(closePrice),closePrice));
                            stockCommonReplayService.save(stockCommonReplay);
                            log.info("保存护盘数据成功 stockCode{} kbarDate{}",circulateInfo.getStockCode(),stockCommonReplay.getKbarDate());
                        }


                    }
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }


    }

    public void firstPlankNoBuyInfo(Date date){
        Date currentDate = date;
        if(!commonComponent.isTradeDate(currentDate)){
            log.info("当前日期不是交易日期");
            return;
        }
        StockCommonReplayQuery stockCommonReplayQuery = new StockCommonReplayQuery();
        stockCommonReplayQuery.setKbarDate(DateUtil.format(date,DateUtil.yyyyMMdd));
        List<StockCommonReplay> stockCommonReplays = stockCommonReplayService.listByCondition(stockCommonReplayQuery);
        int index = 0;
        for (StockCommonReplay replay:stockCommonReplays) {
            index++;
            try {
                List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNew(replay.getStockCode(), 50, 11);
                if(kbars.size()>=2) {
                    kbars = kbars.subList(1, kbars.size());
                    List<StockKbar> stockKbars = Lists.reverse(kbars);
                    Long totalExchange = 0l;
                    int days = 1;
                    BigDecimal lowAdjPrice = null;
                    if (!CollectionUtils.isEmpty(stockKbars)) {
                        for (StockKbar kbar : stockKbars) {
                            totalExchange = totalExchange + kbar.getTradeQuantity();
                            days++;
                            if (lowAdjPrice == null || kbar.getAdjLowPrice().compareTo(lowAdjPrice) == -1) {
                                lowAdjPrice = kbar.getAdjLowPrice();
                            }
                        }
                    }
                    if (days > 0) {
                        long avgExchange = totalExchange / days;
                        replay.setAvgExchange10(avgExchange);
                    }
                    if (lowAdjPrice != null) {
                        BigDecimal plankPrice = PriceUtil.calUpperPrice(replay.getStockCode(), stockKbars.get(0).getClosePrice());
                        BigDecimal divide = plankPrice.divide(lowAdjPrice, 2, BigDecimal.ROUND_HALF_UP);
                        replay.setPlankPriceThanLow10(divide);
                    }
                    BigDecimal price1455 = null;
                    BigDecimal price1500 = null;
                    BigDecimal priceBefore1455 = null;
                    List<ThirdSecondTransactionDataDTO> data = currentDayTransactionDataComponent.getData(replay.getStockCode());
                    if (!CollectionUtils.isEmpty(data)) {
                        for (ThirdSecondTransactionDataDTO dto : data) {
                            if (dto.getTradeTime().startsWith("14:54") || dto.getTradeTime().startsWith("14:53") || dto.getTradeTime().startsWith("14:52") || dto.getTradeTime().startsWith("14:51")) {
                                priceBefore1455 = dto.getTradePrice();
                            }
                            if (price1455 == null && dto.getTradeTime().startsWith("14:55")) {
                                price1455 = dto.getTradePrice();
                            }
                            if (price1500 == null && dto.getTradeTime().startsWith("15")) {
                                price1500 = dto.getTradePrice();
                            }
                        }
                    }
                    if (price1455 == null) {
                        price1455 = priceBefore1455;
                    }
                    if (price1455 != null && price1500 != null) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(price1500.subtract(price1455), stockKbars.get(1).getClosePrice());
                        replay.setEndRaiseRate55(rate);
                    }
                }
                stockCommonReplayService.updateById(replay);
                System.out.println(replay.getStockCode()+"==firstPlankNoBuyInfo=="+index);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }

    public void highRaiseStockInfo(Date date){
        Date currentDate = date;
        if(!commonComponent.isTradeDate(currentDate)){
            log.info("当前日期不是交易日期");
            return;
        }
        int index = 0;
        StockCommonReplayQuery stockCommonReplayQuery = new StockCommonReplayQuery();
        stockCommonReplayQuery.setKbarDate(DateUtil.format(date,DateUtil.yyyyMMdd));
        List<StockCommonReplay> stockCommonReplays = stockCommonReplayService.listByCondition(stockCommonReplayQuery);
        for (StockCommonReplay replay:stockCommonReplays) {
            index++;
            try {
                List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNewDays(replay.getStockCode(), 50, 11);
                List<StockKbar> stockKbars = Lists.reverse(kbars);
                BigDecimal preEndPrice = null;
                BigDecimal currentDayEndPrice = null;
                BigDecimal rateDay5 = null;
                long planks = 0;
                int i = 0;
                if (!CollectionUtils.isEmpty(stockKbars)) {
                    for (StockKbar kbar : stockKbars) {
                        i++;
                        if (i == 1) {
                            currentDayEndPrice = kbar.getAdjClosePrice();
                        }
                        if(preEndPrice!=null) {
                            boolean flag = PriceUtil.isUpperPrice(kbar.getStockCode(), preEndPrice, kbar.getClosePrice());
                            if (flag) {
                                planks++;
                            }
                            if (i == 6) {
                                rateDay5 = PriceUtil.getPricePercentRate(currentDayEndPrice.subtract(kbar.getAdjClosePrice()), kbar.getAdjClosePrice());
                            }
                        }
                        preEndPrice = kbar.getClosePrice();
                    }
                }
                replay.setRateDay5(rateDay5);
                replay.setPlanksDay10(planks);
                stockCommonReplayService.updateById(replay);
                System.out.println(replay.getStockCode()+"==highRaiseStockInfo=="+index);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }

    public void forTwoPlankWuDi(Date date){
        Date currentDate = date;
        if(!commonComponent.isTradeDate(currentDate)){
            log.info("当前日期不是交易日期");
            return;
        }
        StockCommonReplayQuery stockCommonReplayQuery = new StockCommonReplayQuery();
        stockCommonReplayQuery.setKbarDate(DateUtil.format(date,DateUtil.yyyyMMdd));
        List<StockCommonReplay> stockCommonReplays = stockCommonReplayService.listByCondition(stockCommonReplayQuery);
        int index = 0;
        for (StockCommonReplay replay:stockCommonReplays) {
            index++;
            try {
                List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNew(replay.getStockCode(), 50, 11);
                List<StockKbar> stockKbars = Lists.reverse(kbars);
                BigDecimal currentDayEndPrice = null;
                BigDecimal rateDay3 = null;
                BigDecimal lowPrice  = null;
                int i = 0;
                if (!CollectionUtils.isEmpty(stockKbars)) {
                    for (StockKbar kbar : stockKbars) {
                        i++;
                        if (i == 1) {
                            currentDayEndPrice = kbar.getAdjClosePrice();
                        }
                        if (i == 4) {
                            rateDay3 = PriceUtil.getPricePercentRate(currentDayEndPrice.subtract(kbar.getAdjClosePrice()), kbar.getAdjClosePrice());
                        }
                        if(i<=10){
                            if(lowPrice==null||kbar.getAdjLowPrice().compareTo(lowPrice)==-1){
                                lowPrice = kbar.getAdjLowPrice();
                            }
                        }
                    }
                }
                replay.setRateDay3(rateDay3);
                replay.setGatherPriceThanLow10(lowPrice);
                stockCommonReplayService.updateById(replay);
                System.out.println(replay.getStockCode()+"==forTwoPlankWuDi=="+index);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


}
