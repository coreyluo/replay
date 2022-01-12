package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BigOrderBuyDTO {

    private String stockCode;
    private String stockName;
    private BigDecimal circulateZ;
    private String tradeDate;
    private String tradeTime;
    private BigDecimal buyPrice;
    private BigDecimal buyRate;
    private BigDecimal buyAmount;
    private BigDecimal avgTradeAmountDay5;
    private BigDecimal buyTimeAvgPrice;
    private Integer planks;
    private BigDecimal openRate;
    private BigDecimal changePrice;
    private Integer direct;
    private BigDecimal profit;
}
