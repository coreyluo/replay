package com.bazinga.dto;


import com.bazinga.replay.dto.KBarDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankExchangeDailyDTO {

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

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;


    private BigDecimal exchangeMoney;

    private KBarDTO kBarDTO;

    private boolean plankEnd;
    private BigDecimal startProfit;
    private BigDecimal plankProfit;

    private BigDecimal startRate;

    private boolean startIsPlank;
    private boolean endIsPlank;


    /**
     * 最大成交额日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String maxExchangeMoneyDate;

    /**
     * 最大成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal maxExchangeMoney;

    private BigDecimal exchangePercent;

    private BigDecimal rate3;

    private BigDecimal rate5;

    private BigDecimal rate10;

    private BigDecimal rate15;

    private Long circulateZ;
    private BigDecimal exchangeRate;
    private Long exchangeQuantity;


    private BigDecimal preEndPrice;

    private Long yesterdayQuantity;
    private Integer beforePlankQuantity;

    private String insertTime;

    private boolean isUpperB;

    private BigDecimal openRate;

    private boolean isEndPlank;

    private BigDecimal avgRate;

    private long maxExchange;
    private String maxExchangeDateStr;
    private long avgExchange;
    private int spaceDays;
    private BigDecimal avgRange;


    private long maxExchange100;
    private String maxExchangeDateStr100;
    private long avgExchange100;
    private int spaceDays100;
    private BigDecimal avgRange100;
    private BigDecimal avgRangeOpenClose100;
    private Long maxDayExchange;
    private BigDecimal maxDayAvgPrice;
    private BigDecimal maxDayHighPrice;
    private Integer maxDays;
    private Long minDayExchange;
    private BigDecimal minDayAvgPrice;
    private BigDecimal minDayLowPrice;
    private Integer minDays;

    private BigDecimal twoPlankPrice;

    private int twoPlank;
    private int threePlankHigh;

    private BigDecimal sunTotalRate10;
    private BigDecimal sunAvgRate10;
    private int sunTimes10;
    private Long sunExchange10;

    private BigDecimal beforeFirstRate;
    private Long beforeFirstExchange;



    private long maxExchange15;
    private String maxExchangeDateStr15;
    private long avgExchange15;
    private int spaceDays15;
    private BigDecimal avgRange15;

    private Long maxDayExchange15;
    private BigDecimal maxDayAvgPrice15;
    private BigDecimal maxDayHighPrice15;
    private Integer maxDays15;
    private Long minDayExchange15;
    private BigDecimal minDayAvgPrice15;
    private BigDecimal minDayLowPrice15;
    private Integer minDays15;


    private BigDecimal startExchangeMoney;
    private Long beforeTotalExchangeMoney;
    private Integer beforePlanks5;

    private boolean isHighThanOpen15;
    private boolean isHighThanOpen20;
    private boolean isHighThanOpen25;
    private boolean isHighThanOpen30;

    private BigDecimal buyPrice15;
    private BigDecimal buyPrice20;
    private BigDecimal buyPrice25;
    private BigDecimal buyPrice30;

    private BigDecimal avgRate15;
    private BigDecimal avgRate20;
    private BigDecimal avgRate25;
    private BigDecimal avgRate30;

    private String buyAmount;
    private String sellAmount;
    private String realProfit;
    private String realProfitRate;
    private String realPlanks;

    private int raiseThanTwoPointFive;
    private int downThanTwoPointFive;
    private BigDecimal raiseThanTwoPointFiveRate;
    private BigDecimal downThanTwoPointFiveRate;

    private  String preBlockCode;
    private Integer preBlockLevel;
    private String preBlockName;
    private BigDecimal preBlockRate;

    private  String blockCode;
    private Integer blockLevel;
    private String blockName;
    private BigDecimal blockRate;

    private BigDecimal preHighBlockRate;
    private Integer preHighBlockLevel;

    private String blockName3Min;
    private Integer blockLevel3Min;
    private BigDecimal blockRate3Min;

    private KBarDTO preKbarDTO;
    private KBarDTO prePreKbarDTO;

}
