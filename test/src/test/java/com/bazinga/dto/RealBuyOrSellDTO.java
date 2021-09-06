package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RealBuyOrSellDTO {

    private String stockCode;
    private String stockName;
    private String dateStr;
    private BigDecimal money;
    private BigDecimal realBuy;
    private BigDecimal realSell;

}
