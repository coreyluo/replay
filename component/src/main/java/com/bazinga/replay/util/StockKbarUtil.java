package com.bazinga.replay.util;

import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.PriceUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class StockKbarUtil {


    /**
     * 判断是否涨停
     * @param stockKbar 当前交易日
     * @param preStockKbar 上一个交易日
     * @return 是否涨停
     */
    public static boolean isUpperPrice(StockKbar stockKbar, StockKbar preStockKbar){
        if(stockKbar.getAdjFactor().compareTo(preStockKbar.getAdjFactor())==0){
            return PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getClosePrice(),preStockKbar.getClosePrice());
        }else {
            return PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getAdjClosePrice(),preStockKbar.getAdjClosePrice());
        }
    }
    public static boolean is10UpperPrice(StockKbar stockKbar, StockKbar preStockKbar){
        if(stockKbar.getAdjFactor().compareTo(preStockKbar.getAdjFactor())==0){
            return PriceUtil.isUpperPrice(stockKbar.getClosePrice(),preStockKbar.getClosePrice());
        }else {
            return PriceUtil.isUpperPrice(stockKbar.getAdjClosePrice(),preStockKbar.getAdjClosePrice());
        }
    }

    public static boolean isHighUpperPrice(StockKbar stockKbar, StockKbar preStockKbar){
        if(stockKbar.getAdjFactor().compareTo(preStockKbar.getAdjFactor())==0){
            return PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getHighPrice(),preStockKbar.getClosePrice());
        }else {
            return PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getAdjHighPrice(),preStockKbar.getAdjClosePrice());
        }
    }

    public static BigDecimal getNDaysUpperRateDesc(List<StockKbar> stockKbarList45, int i) {
        StockKbar stockKbar = stockKbarList45.get(i);
        StockKbar currentStockKbar = stockKbarList45.get(0);

        return PriceUtil.getPricePercentRate(currentStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
    }

    public static BigDecimal getNDaysUpperRate(List<StockKbar> stockKbarList45, int i) {
        StockKbar stockKbar = stockKbarList45.get(stockKbarList45.size() - i-1);
        StockKbar currentStockKbar = stockKbarList45.get(stockKbarList45.size() - 1);

        return PriceUtil.getPricePercentRate(currentStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
    }

    public static BigDecimal getNDaysLowCloseRate(List<StockKbar> stockKbarList45, int i) {
        StockKbar currentStockKbar = stockKbarList45.get(stockKbarList45.size() - 1);
        BigDecimal lowPrice = currentStockKbar.getAdjLowPrice();
        for (int j = 1; j < i; j++) {
            StockKbar stockKbar = stockKbarList45.get(stockKbarList45.size() - j);
            if(lowPrice.compareTo(stockKbar.getLowPrice())>0){
                lowPrice = stockKbar.getAdjLowPrice();
            }
        }
        StockKbar stockKbar  = stockKbarList45.get(stockKbarList45.size()-i-1);
        return PriceUtil.getPricePercentRate(currentStockKbar.getAdjClosePrice().subtract(lowPrice), stockKbar.getAdjOpenPrice());
    }

    public static BigDecimal getLowPrice(List<ThirdSecondTransactionDataDTO> preHalfOneHourList) {
        BigDecimal lowPrice = preHalfOneHourList.get(0).getTradePrice();
        for (ThirdSecondTransactionDataDTO transactionDataDTO : preHalfOneHourList) {
            if(transactionDataDTO.getTradePrice().compareTo(lowPrice)<0){
                lowPrice = transactionDataDTO.getTradePrice();
            }
        }
        return lowPrice;
    }

    public static BigDecimal getHighPrice(List<ThirdSecondTransactionDataDTO> preHalfOneHourList) {
        BigDecimal highPrice = preHalfOneHourList.get(0).getTradePrice();
        for (ThirdSecondTransactionDataDTO transactionDataDTO : preHalfOneHourList) {
            if(transactionDataDTO.getTradePrice().compareTo(highPrice)>0){
                highPrice = transactionDataDTO.getTradePrice();
            }
        }
        return highPrice;
    }

    public static Integer getDownDays(List<StockKbar> day5KbarList) {
        Long preKbarQuantity = day5KbarList.get(4).getTradeQuantity();

        int ltNum= 0;

        int downDays = 0;
        int serialsDays= 0;
        for (StockKbar stockKbar : day5KbarList) {
            if(stockKbar.getTradeQuantity()<preKbarQuantity){
                ltNum++;
            }
            if(stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice())<0){
                downDays++;
                serialsDays++;
            }else {
                if(serialsDays<3){
                    serialsDays = 0;
                }
            }
        }
        if(ltNum>=2){
            log.info("成交量非倒数 stockCode={}",day5KbarList.get(0).getStockCode());
            return -1;
        }
        if(downDays<3){
            return -1;
        }
        return serialsDays<3?0:serialsDays;
    }

    public static boolean isPlank(List<StockKbar> tempList) {
        for (int i = 1; i < tempList.size()-1; i++) {
            StockKbar tempStockKbar = tempList.get(tempList.size() - i);
            StockKbar preTempStockKbar = tempList.get(tempList.size() - i-1);

            boolean isPlank = StockKbarUtil.isUpperPrice(tempStockKbar, preTempStockKbar);
            if(isPlank){
                return true;
            }
        }
        return false;
    }

}
