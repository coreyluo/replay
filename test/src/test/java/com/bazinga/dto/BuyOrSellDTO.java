package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuyOrSellDTO {

    private String stockCode;
    private String stockName;
    private String timeStamp;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal money;
    private Boolean isBuy;

}
