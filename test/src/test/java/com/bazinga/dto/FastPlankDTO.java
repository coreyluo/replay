package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FastPlankDTO {
    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private  BigDecimal marketMoney;

    private StockKbar buyKbar;
    private StockKbar preKbar;
    private StockKbar prePreKbar;
    private String plankTimeStr;
    private BigDecimal buyDayGatherMoney;

    private BigDecimal beforeDay3;
    private BigDecimal beforeDay5;
    private BigDecimal beforeDay10;
    private BigDecimal beforeDay30;

    private BigDecimal rateBeforeDay255;
    private BigDecimal rateBeforeEnd;
    private BigDecimal rateBefore255ToEnd;
    private BigDecimal rateBeforeLower230ToEnd;
    private BigDecimal rateBeforeAvgRate;

    private Integer planksDay10;
    private BigDecimal lowerPriceDay10;
    private Long avgExchangeDay10;
    private BigDecimal avgExchangePercentDay10;
    private Long avgExchangeDay11To50;
    private BigDecimal exchangeDay10DivideDay50;

    private BigDecimal plankPriceDivideLowerDay10;

    private BigDecimal profit;




}
