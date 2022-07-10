package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class HangYePlankBuyDTO {
    private String stockCode;
    private String stockName;
    private String blockCode;
    private String blockName;
    private String tradeDate;
    private Long start;
    private int planks;
    private BigDecimal openRate;
    private BigDecimal openAmount;
    private BigDecimal rateDay1;
    private BigDecimal rateDay3;
    private BigDecimal rateDay5;
    private BigDecimal rateDay10;
    private BigDecimal blockAmountMinute5;
    private BigDecimal blockAmountBuy;
    private BigDecimal blockAmountDay1;
    private BigDecimal blockAmountDay2;
    private BigDecimal blockAmountDay3;
    private BigDecimal blockAmountDay4;
    private BigDecimal blockAmountDay5;
    private BigDecimal profit;


}
