package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TbondUseMainDTO {

    private String stockCode;
    private String stockName;
    private String mainCode;
    private String mainName;
    private String tradeDate;
    private BigDecimal tradePrice;
    private String tradeTime;
    private BigDecimal sellPrice;
    private String sellTime;
    private Integer beforeSellQuantity;
    private Integer beforeTradeDeal;
    private Integer sellQuantity;
    private Integer tradeDeal;
    private Integer avg10TradeDeal;
    private BigDecimal profit;
}
