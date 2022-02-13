package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HighThanAvgBuyDTO {
    private String stockCode;
    private String stockName;
    private  BigDecimal marketMoney;
    private String tradeDate;
    private Integer nQuantity;
    private Integer mQuantity;
    private Integer xTimes;
    private Integer quantityUpperTimes;
    private BigDecimal avgPrice;
    private String buyTimeStr;
    private BigDecimal buyRate;
    private BigDecimal xMThanN;

    private BigDecimal beforeDay8;
    private BigDecimal beforeDay18;
    private BigDecimal beforeDay38;
    private BigDecimal beforeDay88;
    private BigDecimal before1AvgThanN;
    private BigDecimal before5AvgThanN;
    private BigDecimal before15AvgThanN;

    private BigDecimal before1AvgThanM;
    private BigDecimal before5AvgThanM;
    private BigDecimal before15AvgThanM;
    private BigDecimal profit;




}
