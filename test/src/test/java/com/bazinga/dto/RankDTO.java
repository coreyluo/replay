package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankDTO {

    private Integer rank;

    private BigDecimal tradeAmount;

    public RankDTO(Integer rank, BigDecimal tradeAmount) {
        this.rank = rank;
        this.tradeAmount = tradeAmount;
    }
}
