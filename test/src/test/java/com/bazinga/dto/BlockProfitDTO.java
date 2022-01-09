package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockProfitDTO {
    private String tradeDate;
    private String blockCode;
    private String blockName;
    private BigDecimal rateThanTwo;
    private BigDecimal totalProfit;
}
