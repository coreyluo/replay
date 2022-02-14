package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class Index500NDayDTO {

    private BigDecimal day5Rate;

    private BigDecimal day10Rate;

    private BigDecimal day15Rate;

    public Index500NDayDTO(BigDecimal day5Rate, BigDecimal day10Rate, BigDecimal day15Rate) {
        this.day5Rate = day5Rate;
        this.day10Rate = day10Rate;
        this.day15Rate = day15Rate;
    }
}
