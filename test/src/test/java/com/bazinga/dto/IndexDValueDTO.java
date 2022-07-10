package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexDValueDTO {
    private BigDecimal closeA;
    private BigDecimal closeB;
    private BigDecimal closeA0930;
    private BigDecimal closeB0930;
    private BigDecimal dvalue;
    private String tradeTime;

    private String buyTIme;
    private String sellTime;
    private BigDecimal profit;
}
