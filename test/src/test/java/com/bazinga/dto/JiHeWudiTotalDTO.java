package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class JiHeWudiTotalDTO {
    private String stockCode;

    private BigDecimal rate915 = BigDecimal.ZERO;
    private BigDecimal amount915= BigDecimal.ZERO;
    private BigDecimal buyTwoAmount915= BigDecimal.ZERO;
    private int buyTwoCount915;
    private int rateCount915;

    private BigDecimal rate920= BigDecimal.ZERO;
    private BigDecimal amount920= BigDecimal.ZERO;
    private BigDecimal buyTwoAmount920= BigDecimal.ZERO;
    private int buyTwoCount920;
    private int rateCount920;

    private BigDecimal rate925= BigDecimal.ZERO;
    private BigDecimal amount925= BigDecimal.ZERO;
    private BigDecimal buyTwoAmount925= BigDecimal.ZERO;
    private int buyTwoCount925;
    private int rateCount925;



}
