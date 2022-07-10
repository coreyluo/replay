package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlocKFollowBuyDTO {
    private String blockCode;
    private String blockName;
    private String tradeDate;

    private int before10Planks;
    private int before10OnePlanks;
    private int beautifulPlanks10;
    private int beautifulPlanks3;
    private int beautifulPlanks3First;
    private BigDecimal biasDay6;
    private BigDecimal biasDay12;
    private BigDecimal biasDay24;
    private BigDecimal macd;

    private Long timeStamp;
    private BigDecimal profit10First;
    private Integer count10First;
    private BigDecimal profit10Two;
    private Integer count10Two;
    private BigDecimal profit10Three;
    private Integer count10Three;

    private Long timeStamp3;
    private BigDecimal profit3PlankFirst;
    private Integer count3PlankFirst;
    private BigDecimal profit3PlankTwo;
    private Integer count3PlankTwo;
    private BigDecimal profit3PlankThree;
    private Integer count3PlankThree;

    private Long timeStamp3First;
    private BigDecimal profit3FirstPlankFirst;
    private Integer count3FirstPlankFirst;
    private BigDecimal profit3FirstPlankTwo;
    private Integer count3FirstPlankTwo;
    private BigDecimal profit3FirstPlankThree;
    private Integer count3FirstPlankThree;
}
