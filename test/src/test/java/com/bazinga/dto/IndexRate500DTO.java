package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexRate500DTO {


    private BigDecimal openRate;

    private BigDecimal lowRate;

    private BigDecimal highRate;

    private BigDecimal buyRate;

    //前5分钟高于开盘条数
    private Integer overOpenCount;

    public IndexRate500DTO(BigDecimal openRate, BigDecimal lowRate, BigDecimal highRate, BigDecimal buyRate,Integer overOpenCount) {
        this.openRate = openRate;
        this.lowRate = lowRate;
        this.highRate = highRate;
        this.buyRate = buyRate;
        this.overOpenCount = overOpenCount;
    }
}
