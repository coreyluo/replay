package com.tradex.util;

import com.tradex.enums.ExchangeId;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;


/**
 * Created by kongkp on 2017/1/7.
 */
public class StockUtils {
    public static ExchangeId getExchangeId(String stockCode) {
        String market = getMarket(stockCode);
        if (market.startsWith("SZ.")) {
            return ExchangeId.SZ;
        }
        if (market.startsWith("SH.")) {
            return ExchangeId.SH;
        }
        return null;
    }

    public static String getMarket(String stockCode) {
        if (StringUtils.isNotBlank(stockCode)) {
            if (stockCode.startsWith("000")||stockCode.startsWith("001") || stockCode.startsWith("003")) {
                return "SZ.A";
            }
            if (stockCode.startsWith("200")) {
                return "SZ.B";
            }
            if (stockCode.startsWith("002")) {
                return "SZ.ZX";
            }
            if (stockCode.startsWith("30")) {
                return "SZ.CY";
            }
            if (stockCode.startsWith("080")) {
                return "SZ.PG";
            }
            if (stockCode.startsWith("031")) {
                return "SZ.QZ";
            }
            if (stockCode.startsWith("60")) {
                return "SH.A";
            }
            if (stockCode.startsWith("900")) {
                return "SH.B";
            }
            if (stockCode.startsWith("730")) {
                return "SH.SG";
            }
            if (stockCode.startsWith("700")) {
                return "SH.PG";
            }
            if (stockCode.startsWith("580")) {
                return "SH.QZ";
            }
            if (stockCode.startsWith("880")) {
                return "SH.BK";
            }
            if (stockCode.startsWith("999")) {
                return "SH.DP";
            }
            if (stockCode.startsWith("399")) {
                return "SZ.CY";
            }
            if (stockCode.startsWith("688")||stockCode.startsWith("689")) {
                return "SH.KC";
            }
        }
        throw new IllegalArgumentException("股票代码(" + stockCode + ")不属于中国大陆证券代码.");
    }

    /**
     * 是不是自动化可以打的市场
     * @param stockCode
     * @return
     */
    public static boolean isAutoStock(String stockCode) {
        if (StringUtils.isNotBlank(stockCode)) {
            if (stockCode.startsWith("000")||stockCode.startsWith("001") || stockCode.startsWith("003")) {
                return true;
            }
            if (stockCode.startsWith("002")) {
                return true;
            }
            if (stockCode.startsWith("300")) {
                return true;
            }
            if (stockCode.startsWith("60")) {
                return true;
            }
            if (stockCode.startsWith("688")) {
                return true;
            }
        }
        return false;
    }
}
