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
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private BigDecimal buyTimeRate;
    private BigDecimal tbondBuyTimeRate;
    private String tradeDate;
    private BigDecimal tradePrice;
    private String tradeTime;
    private String tbondTradeTime;

    private BigDecimal sellPrice;
    private String tbondSellTime;
    private String sellTime;
    private Long beforeSellQuantity;
    private Long beforeTradeDeal;
    private Long sellQuantity;
    private Long tradeDeal;
    private Long avg10TradeDeal;

    private BigDecimal preEndPrice;
    private BigDecimal profit;
}
