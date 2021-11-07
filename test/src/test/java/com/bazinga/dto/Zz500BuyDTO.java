package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class Zz500BuyDTO {


    private String stockCode;
    private String stockName;
    private String tradeDate;
    private Long circulateZ;
    private BigDecimal endRate;
    private BigDecimal endPrice;
    private BigDecimal exchangeAmount;
    private BigDecimal profit;



}
