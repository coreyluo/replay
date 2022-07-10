package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexDValueBuyDTO {
    private BigDecimal closeA;
    private BigDecimal closeB;
    private BigDecimal closeA0930;
    private BigDecimal closeB0930;

    private BigDecimal sellCloseA;
    private BigDecimal sellCloseB;
    private BigDecimal sellCloseA0930;
    private BigDecimal sellCloseB0930;

    private BigDecimal dvalue;
    private BigDecimal sellDvalue;

    private String buyTime;
    private String sellTime;
    private BigDecimal profitValue;
    private BigDecimal profit;
}
