package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DaPanDropDTO {
    private String tradeDate;
    private BigDecimal dropRate;
    private Integer days;
}
