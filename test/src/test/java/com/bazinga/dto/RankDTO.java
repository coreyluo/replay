package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankDTO {

    private String stockCode;

    private Integer rank;

    private BigDecimal tradeAmount;

    private BigDecimal openRate;

    private BigDecimal premium;

    public RankDTO(String stockCode, Integer rank, BigDecimal tradeAmount, BigDecimal openRate, BigDecimal premium) {
        this.stockCode = stockCode;
        this.rank = rank;
        this.tradeAmount = tradeAmount;
        this.openRate = openRate;
        this.premium = premium;
    }

    public RankDTO(String stockCode, Integer rank, BigDecimal tradeAmount) {
        this.stockCode = stockCode;
        this.rank = rank;
        this.tradeAmount = tradeAmount;
    }

    public RankDTO(Integer rank, BigDecimal tradeAmount) {
        this.rank = rank;
        this.tradeAmount = tradeAmount;
    }

    public RankDTO() {
    }
}
