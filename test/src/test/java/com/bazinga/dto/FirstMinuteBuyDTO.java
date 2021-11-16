package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FirstMinuteBuyDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String tradeDate;
    private StockKbar stockKbar;
    private boolean endPlank;
    private String plankTime;
    private int planks;
    private BigDecimal openRate;
    private Integer gatherQuantity;
    private BigDecimal gatherAmount;
    private BigDecimal gatherPercent;

    private Integer beforeBuyQuantity;
    private BigDecimal beforeBuyAmount;
    private BigDecimal beforeBuyPercent;
    private BigDecimal profit;


    private BigDecimal highDropRate;
    private String highTime;
    private BigDecimal highRange;
    private BigDecimal lowRange;
    private BigDecimal realRangeAvg;

    private BigDecimal maxTradeMoney;
    private Integer maxTradeQuantity;
    private BigDecimal avgTradeMoney;
    private Integer avgTradeQuantity;
}
