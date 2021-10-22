package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import com.google.common.collect.Lists;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FastRaiseBestDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private BigDecimal marketMoney;
    private String tradeDate;
    private StockKbar stockKbar;
    private BigDecimal raiseRate;
    private String raiseTime;
    private BigDecimal exchangeMoney;
    private boolean endPlank;
    private BigDecimal profit;
}
