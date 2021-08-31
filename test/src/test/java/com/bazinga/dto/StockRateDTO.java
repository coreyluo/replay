package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockRateDTO {

    private String stockCode;
    private String stockName;
    private BigDecimal rate;

}
