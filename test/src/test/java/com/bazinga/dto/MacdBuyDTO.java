package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class MacdBuyDTO {
    private String stockCode;

    private String stockName;

    private int redirect;

    private BigDecimal preBar;
    private BigDecimal bar;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String buyTime;
    private String sellTime;
    private BigDecimal preClosePrice;

    private BigDecimal buyKbarAmount;
    private BigDecimal preKbarAmount;

    private BigDecimal raiseRateDay5;
    private BigDecimal highBar;
    private BigDecimal sellBar;
    private BigDecimal dropPercent;
    private Integer sellIntelTime;
    private int sellDropBuy;




}
