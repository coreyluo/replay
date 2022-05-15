package com.bazinga.util;

import com.bazinga.enums.MarketTypeEnum;
import org.apache.commons.lang.StringUtils;

/**
 * @author huliang
 * @version $Id: DateUtil.java, v 0.1 2011-12-19 下午7:23:39 huliang Exp $
 */
public class ThsCommonUtil {

    public static String getThsStockCode(String stockCode) {
       if(StringUtils.isEmpty(stockCode)){
           return null;
       }
       if(stockCode.startsWith("30")||stockCode.startsWith("00")){
           return stockCode+".SZ";
       }
       return stockCode+".SH";
    }


}
