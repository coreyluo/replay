package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class HangYeAmountRedisDTO {
    private String blockCode;
    private String blockName;
    private BigDecimal blockAmountMinute5;
    private BigDecimal blockAmountBuy;
    private BigDecimal blockAmountDay1;
    private BigDecimal blockAmountDay2;
    private BigDecimal blockAmountDay3;
    private BigDecimal blockAmountDay4;
    private BigDecimal blockAmountDay5;
}
