package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenCompeteDTO {
    private String stockCode;

    private Integer competeNum;

    private BigDecimal rate;
}
