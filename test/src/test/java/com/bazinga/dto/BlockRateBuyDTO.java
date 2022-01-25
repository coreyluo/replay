package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockRateBuyDTO {

    private String blockCode;
    private String blockName;
    private String tradeDate;
    private String preTradeDate;
    private String nextTradeDate;
    private BigDecimal preClosePrice;
    private BigDecimal closePrice;
    private String tradeTime;
    private BigDecimal rate;
}
