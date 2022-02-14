package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexRateDTO {

    private BigDecimal closeRate;

    private BigDecimal highRate;


    public IndexRateDTO(BigDecimal closeRate, BigDecimal highRate) {
        this.closeRate = closeRate;
        this.highRate = highRate;
    }
}
