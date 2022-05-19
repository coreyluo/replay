package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankDayDTO {

    private String stockCode;

    private Integer sealType;

    private BigDecimal tradeAmount;

    public PlankDayDTO(String stockCode, Integer sealType, BigDecimal tradeAmount) {
        this.stockCode = stockCode;
        this.sealType = sealType;
        this.tradeAmount = tradeAmount;
    }
}
