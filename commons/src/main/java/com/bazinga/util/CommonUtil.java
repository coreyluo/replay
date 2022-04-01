package com.bazinga.util;


import com.bazinga.enums.StockTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static com.bazinga.constant.CommonConstant.ONE_MILLION;

/**
 * @author yunshan
 * @date 2019/5/28
 */
@Slf4j
public class CommonUtil {


    public static int getStockType(Long circulateZ) {
        if (circulateZ < 50 * ONE_MILLION) {
            return StockTypeEnum.POINT_FIVE.getCode();
        } else if (circulateZ >= 50 * ONE_MILLION && circulateZ < 100 * ONE_MILLION) {
            return StockTypeEnum.POINT_FIVE_TO_ONE.getCode();
        } else if (circulateZ >= 100 * ONE_MILLION && circulateZ < 200 * ONE_MILLION) {
            return StockTypeEnum.ONE_TO_TWO.getCode();
        } else if (circulateZ >= 200 * ONE_MILLION && circulateZ < 300 * ONE_MILLION) {
            return StockTypeEnum.TWO_TO_THREE.getCode();
        } else if (circulateZ >= 300 * ONE_MILLION && circulateZ < 550 * ONE_MILLION) {
            return StockTypeEnum.THREE_TO_FIVE_POINT_FIVE.getCode();
        } else if (circulateZ >= 550 * ONE_MILLION && circulateZ < 800 * ONE_MILLION) {
            return StockTypeEnum.FIVE_POINT_FIVE_TO_EIGHT.getCode();
        } else if (circulateZ >= 800 * ONE_MILLION && circulateZ < 1100 * ONE_MILLION) {
            return StockTypeEnum.EIGHT_TO_ELEVEN.getCode();
        } else {
            return StockTypeEnum.OVER_ELEVEN.getCode();
        }
    }

    public static boolean is300Stock(String stockCode){
        if(StringUtils.isNotBlank(stockCode)&&stockCode.startsWith("300")){
            return true;
        }
        return false;
    }

    public static boolean is688Stock(String stockCode){
        if(StringUtils.isNotBlank(stockCode)&&stockCode.startsWith("688")){
            return true;
        }
        return false;
    }

    public static boolean isMainStock(String stockCode){
        if(StringUtils.isNotBlank(stockCode)&&stockCode.startsWith("688")){
            return false;
        }
        if(StringUtils.isNotBlank(stockCode)&&stockCode.startsWith("300")){
            return false;
        }
        return true;
    }

    public static BigDecimal sqrt(BigDecimal value, int scale){
        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        deviation = deviation.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return deviation;
    }

    public static void main(String[] args) {
        System.out.println(sqrt(new BigDecimal(8),3));
    }


}
