package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexRate500DTO {


    private BigDecimal openRate;

    private BigDecimal lowRate;

    private BigDecimal highRate;

    private BigDecimal buyRate;


    public IndexRate500DTO(BigDecimal openRate, BigDecimal lowRate, BigDecimal highRate, BigDecimal buyRate) {
        this.openRate = openRate;
        this.lowRate = lowRate;
        this.highRate = highRate;
        this.buyRate = buyRate;
    }
}
