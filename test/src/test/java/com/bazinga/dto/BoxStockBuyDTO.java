package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BoxStockBuyDTO {
    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private Long totalCirculateZ;
    private String tradeDate;
    private BigDecimal beforeHighLowRate;
    private BigDecimal firstHighRate;
    private BigDecimal afterHighLowRate;
    private BigDecimal buyPrice;
    private String buyTime;
    private BigDecimal openRate;
    private String firstHighTime;
    private Integer betweenTime;
    private BigDecimal buyTimeTradeAmount;
    private BigDecimal preTradeAmount;
    private Integer prePlanks;
    private BigDecimal rateDay3;
    private BigDecimal rateDay5;
    private BigDecimal rateDay10;
    private BigDecimal profit;
}
