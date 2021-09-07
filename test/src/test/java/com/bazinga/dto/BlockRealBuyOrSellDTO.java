package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockRealBuyOrSellDTO {

    private String blockCode;
    private String blockName;
    private String dateStr;
    private BigDecimal money;
    private BigDecimal realBuy;
    private BigDecimal realSell;
    private Integer stockCounts;

}
