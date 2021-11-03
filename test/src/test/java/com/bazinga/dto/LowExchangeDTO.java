package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LowExchangeDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private BigDecimal marketMoney;
    private String tradeDate;
    private StockKbar stockKbar;
    private BigDecimal lwoExchangePercent;
    private BigDecimal downRate;
    private boolean endPlank;
    private String plankTime;
    private int planks;
    private BigDecimal buyRateThanHigh;
    private BigDecimal profit;
}
