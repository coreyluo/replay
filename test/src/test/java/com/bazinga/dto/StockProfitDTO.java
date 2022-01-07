package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockProfitDTO {

    private String stockCode;
    private BigDecimal profit;
}
