package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DaPanDropPlankDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String buyTime;
    private BigDecimal profit;
}
