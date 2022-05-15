package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ThsQuoteDTO {
    private String stockCode;
    private String stockName;
    private Date quoteTime;
    //09:30:30
    private String tradeTime;
    //2022-04-15
    private String tradeDate;
    private BigDecimal preEndPrice;
    private BigDecimal tradeAmount;
    private BigDecimal currentPrice;
    private BigDecimal buyOnePrice;
    private BigDecimal buyTwoPrice;
    private BigDecimal sellOnePrice;
    private Long buyOneQuantity;
    private Long buyTwoQuantity;
    private Long sellOneQuantity;

}
