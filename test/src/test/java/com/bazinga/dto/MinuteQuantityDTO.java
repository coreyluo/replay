package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class MinuteQuantityDTO{

    private String tradeTime;

    private Integer quantity = 0;

    private BigDecimal avgPrice;

    private BigDecimal tradePrice;

}

