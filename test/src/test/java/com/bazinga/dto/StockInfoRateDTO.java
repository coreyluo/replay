package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockInfoRateDTO {

    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal openRate;
    private BigDecimal closeRate;
    private BigDecimal gatherTradeAmount;
    private BigDecimal tradeAmount;

}
