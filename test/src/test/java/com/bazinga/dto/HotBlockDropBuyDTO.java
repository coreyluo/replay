package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotBlockDropBuyDTO {

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    private Long circulateZ;

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

    private String blockCode;
    private String blockName;
    private Integer openBlockLevel;
    private BigDecimal openBlockRate;

    private BigDecimal blockRaiseRate;
    private Integer blockRaiseLevel;
    private String blockRaiseDateStr;
    private Integer raiseDays;
    private BigDecimal blockDropRate;

    private BigDecimal openRate;

    private BigDecimal raiseDayRate;
    private BigDecimal dropDayRate;
    private Long dropDayExchange;

    private Integer dropDayLevel;
    private Integer raiseDayLevel;
    private Integer buyDayLevel;

    private String raiseDayPlankTime;

    private BigDecimal beforeRate3;
    private BigDecimal beforeRate5;
    private BigDecimal beforeRate10;
    private BigDecimal beforeCloseRate;
    private Long beforeAvgExchangeDay5;
    private Integer dropDayReds;
    private Integer dropDayGreens;
    private boolean dropDayPlankFlag=false;
    private Integer dropDayBlockPlanks;

    private Integer beforePlankDay5;
    private Integer beforeOpenPlankDay5;
    private Boolean raiseNextDayOpenPlankFlag;
    private BigDecimal raiseNextDayOpenRate;

    private BigDecimal raiseDayBlockRate5;
    private BigDecimal raiseDayBlockRate10;

    private BigDecimal raiseDayRate5;
    private BigDecimal raiseDayRate10;



    private BigDecimal profit;
}
