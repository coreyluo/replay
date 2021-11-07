package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FastRaiseBankerDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private BigDecimal marketMoney;
    private String tradeDate;
    private StockKbar stockKbar;
    private BigDecimal openRate;
    private BigDecimal buyPrice;
    private BigDecimal openExchange;
    private BigDecimal raiseRate;
    private String raiseTime;
    private BigDecimal exchangeMoney;
    private boolean endPlank;
    private int planks;
    private boolean plankBack = false;
    private BigDecimal raiseMoney;

    private String plankTime;
    private BigDecimal  plankExchangeMoney;
    private BigDecimal eightExchangeMoney;
    private BigDecimal profit;
}
