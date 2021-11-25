package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockRateDTO {

    private String blockCode;
    private String blockName;
    private String tradeDate;
    private BigDecimal openRate;
    private BigDecimal closeRate;
    private BigDecimal gatherTradeAmount;
    private BigDecimal totalTradeAmount;

    private BigDecimal preCloseRate;
    private BigDecimal preTotalTradeAmount;

}
