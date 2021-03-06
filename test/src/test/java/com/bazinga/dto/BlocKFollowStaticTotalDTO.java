package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlocKFollowStaticTotalDTO {
    private String tradeDate;
    private Integer planksTenCount;
    private Integer plankBlockCount;
    private Integer allBuyCount;
    private Integer oneBuyCount;

    private Integer buyBlocks;
    private Integer buys;
    private BigDecimal profit;
    private Integer noSameBuys;
    private BigDecimal noSameProfit;

    private Integer amountBuys;
    private BigDecimal amountProfit;
    private Integer noSameAmountBuys;
    private BigDecimal noSameAmountProfit;

    private Integer rateBuys;
    private BigDecimal rateProfit;
    private Integer noSameRateBuys;
    private BigDecimal noSameRateProfit;

    private BigDecimal noSameRateProfitEnd;
    private BigDecimal noSameRateProfitTen;

}
