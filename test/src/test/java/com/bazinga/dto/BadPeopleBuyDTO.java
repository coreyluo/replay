package com.bazinga.dto;


import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BadPeopleBuyDTO {

    private String stockCode;
    private String stockName;
    private BigDecimal circulateZ;
    private String tradeDate;
    private boolean badFlag = false;
    private int highDays;
    private int lowDays;
    private int secondLowDays;
    private BigDecimal highRate;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal secondLowRate;
    private BigDecimal secondLowPrice;

    private BigDecimal avgAmount;
    private BigDecimal lowDayAmount;
    private BigDecimal lowDayEndRate;
    private String plankTime;
    private BigDecimal buyPrice;
    private BigDecimal profit;
}
