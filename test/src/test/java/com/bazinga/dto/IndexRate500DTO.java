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

    private Integer overOpenCount10;
    private Integer overOpenCount20;

    private BigDecimal min5TradeAmount;

    private BigDecimal min10TradeAmount;



    public IndexRate500DTO() {
    }

    public IndexRate500DTO(BigDecimal openRate, BigDecimal lowRate, BigDecimal highRate, BigDecimal buyRate, Integer overOpenCount, Integer overOpenCount10, Integer overOpenCount20,BigDecimal min5TradeAmount, BigDecimal min10TradeAmount) {
        this.openRate = openRate;
        this.lowRate = lowRate;
        this.highRate = highRate;
        this.buyRate = buyRate;
        this.overOpenCount = overOpenCount;
        this.overOpenCount10 = overOpenCount10;
        this.overOpenCount20 = overOpenCount20;
        this.min5TradeAmount = min5TradeAmount;
        this.min10TradeAmount = min10TradeAmount;
    }
}
