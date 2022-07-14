package com.bazinga.replay.util;

import com.bazinga.constant.CommonConstant;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;

import java.math.BigDecimal;
import java.util.List;

public class PremiumUtil {

    public  static BigDecimal calProfit(List<StockKbar> stockKbars,BigDecimal buyPrice){
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity()).multiply(CommonConstant.DECIMAL_HUNDRED),2,BigDecimal.ROUND_HALF_UP);
                BigDecimal reason = null;
                if(!(stockKbar.getClosePrice().equals(stockKbar.getAdjClosePrice()))&&!(stockKbar.getOpenPrice().equals(stockKbar.getAdjOpenPrice()))){
                    reason = stockKbar.getAdjOpenPrice().divide(stockKbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
                }
                if(reason==null){
                }else{
                    avgPrice = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
                    buyPrice = buyPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyPrice), buyPrice);
                return profit;
            }
        }
        return null;
    }




    public static BigDecimal calDaysProfit(List<StockKbar> list, int days){
        StockKbar buyStockKbar = list.get(0);
        if(days<list.size()){
            StockKbar sellStockKbar = list.get(days);
            if(sellStockKbar.getAdjFactor().compareTo(buyStockKbar.getAdjFactor())==0){
                return  PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(buyStockKbar.getOpenPrice()),buyStockKbar.getOpenPrice());
            }else {
                return  PriceUtil.getPricePercentRate(sellStockKbar.getAdjOpenPrice().subtract(buyStockKbar.getAdjOpenPrice()),buyStockKbar.getAdjOpenPrice());
            }
        }else {
            return null;
        }


    }


}
