package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenRateDTO {

    private String stockCode;

    private BigDecimal OpenRate;

    public OpenRateDTO(String stockCode, BigDecimal openRate) {
        this.stockCode = stockCode;
        OpenRate = openRate;
    }
}
