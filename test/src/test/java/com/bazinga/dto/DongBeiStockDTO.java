package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DongBeiStockDTO {

    private String stockCode;
    private String stockName;
    private String blockName;
    private BigDecimal circulateZ;
    private BigDecimal market;
    private Integer planks;
    private BigDecimal profit;
}
