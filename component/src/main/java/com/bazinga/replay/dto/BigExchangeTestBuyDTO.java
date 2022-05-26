package com.bazinga.replay.dto;

import com.bazinga.replay.model.ThsBlockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BigExchangeTestBuyDTO {

    private String stockCode;

    private String stockName;

    private String tradeDate;

    private Long circulateZ;

    private String planks;

    private Integer endFlag;

    private BigDecimal bigOrderAmount;

    private Integer bigOrderTimes;

    private BigDecimal profit;
}
