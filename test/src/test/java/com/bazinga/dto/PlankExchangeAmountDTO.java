package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankExchangeAmountDTO {

    private String stockCode;
    private String stockName;
    private String tradeDate;
    private Long circulateZ;
    private Long circulate;
    private StockKbar stockKbar;
    private BigDecimal plankTradeAmount ;
    private BigDecimal plankPrice;

    private BigDecimal rateDay3;
    private BigDecimal rateDay5;
    private BigDecimal rateDay10;

    private BigDecimal profit;

}
