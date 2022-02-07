package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexRate500DTO {


    private BigDecimal openRate;

    private BigDecimal lowRate;

    private BigDecimal highRate;

    private BigDecimal buyRate;

    private BigDecimal min5HighRate;

    private BigDecimal min5LowRate;

    public IndexRate500DTO(BigDecimal openRate, BigDecimal lowRate, BigDecimal highRate, BigDecimal buyRate, BigDecimal min5HighRate, BigDecimal min5LowRate) {
        this.openRate = openRate;
        this.lowRate = lowRate;
        this.highRate = highRate;
        this.buyRate = buyRate;
        this.min5HighRate = min5HighRate;
        this.min5LowRate = min5LowRate;
    }
}
