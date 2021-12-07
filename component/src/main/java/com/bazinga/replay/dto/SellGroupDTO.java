package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellGroupDTO {

    private String stockCode;

    private BigDecimal sellAmount;

    private Integer sellQuantity;



}
