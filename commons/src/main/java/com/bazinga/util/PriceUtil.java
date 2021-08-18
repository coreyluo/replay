package com.bazinga.util;

import com.bazinga.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtil {
    public static float getPricePercentRate(Float price, Float basePrice) {
        return Math.round(price / basePrice * 10000) * 0.01f;
    }

    public static BigDecimal getPricePercentRate(BigDecimal price, BigDecimal basePrice) {
        return price.divide(basePrice,4, RoundingMode.HALF_UP).multiply(CommonConstant.DECIMAL_HUNDRED);
    }

    public static BigDecimal getRatePrice(float rate,BigDecimal currentPrice){
        return currentPrice.add(currentPrice.multiply(new BigDecimal(rate)).multiply(new BigDecimal("0.01"))).setScale(2,BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 是否是涨停价
     *
     * @return
     */
    public static boolean isUpperPrice(String stockCode,BigDecimal currentPrice, BigDecimal yesterdayPrice) {
        if(StringUtils.isNotBlank(stockCode)&&(stockCode.startsWith("3")||stockCode.startsWith("688"))){
            return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.UPPER_RATE300).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
        }
        return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.UPPER_RATE).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    public static boolean isUpperPrice(BigDecimal currentPrice, BigDecimal yesterdayPrice) {
        return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.UPPER_RATE).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
    }


    public static boolean isStarUpperPrice(BigDecimal currentPrice, BigDecimal yesterdayPrice) {
        return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.STAR_UPPER_RATE).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    /**
     * 是否是跌停价
     *
     * @return
     */
    public static boolean isSuddenPrice(BigDecimal currentPrice, BigDecimal yesterdayPrice) {
        return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.SUDDEN_RATE).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    public static boolean isSuddenPrice(String stockCode,BigDecimal currentPrice, BigDecimal yesterdayPrice) {
        if(StringUtils.isNotBlank(stockCode)&&(stockCode.startsWith("30")||stockCode.startsWith("688"))){
            return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.SUDDEN_RATE300).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
        }
        return currentPrice.compareTo(yesterdayPrice.multiply(CommonConstant.SUDDEN_RATE).setScale(2, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    public static BigDecimal calUpperPrice(String stockCode,BigDecimal yesterdayPrice) {
        if(StringUtils.isNotBlank(stockCode)&&(stockCode.startsWith("30")||stockCode.startsWith("688"))){
            return yesterdayPrice.multiply(CommonConstant.UPPER_RATE300).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return yesterdayPrice.multiply(CommonConstant.UPPER_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal calSuddenPrice(BigDecimal yesterdayPrice) {
        return yesterdayPrice.multiply(CommonConstant.SUDDEN_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal calSuddenPrice(String stockCode,BigDecimal yesterdayPrice) {
        if(StringUtils.isNotBlank(stockCode)&&(stockCode.startsWith("30")||stockCode.startsWith("688"))){
            return yesterdayPrice.multiply(CommonConstant.STAR_MARKET_SUDDEN_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return yesterdayPrice.multiply(CommonConstant.SUDDEN_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     *
     * @param rate  绝对涨幅  例如:2个点   就是2
     * @param yesterdayPrice  昨日收盘价格
     * @return  当前价格
     */
    public static BigDecimal absoluteRateToPrice(BigDecimal rate, BigDecimal yesterdayPrice) {
        BigDecimal mulRate = new BigDecimal("1").add(rate.multiply(new BigDecimal("0.01")).setScale(4, BigDecimal.ROUND_HALF_UP));
        return yesterdayPrice.multiply(mulRate).setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public static void main(String[] args) {
        System.out.println(absoluteRateToPrice(new BigDecimal("8"),new BigDecimal("20")));
        System.out.println(getPricePercentRate(new BigDecimal("6.69").subtract(new BigDecimal("6.41")),new BigDecimal("6.41")));
    }
}
