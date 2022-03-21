package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;
import org.apache.commons.collections4.splitmap.AbstractIterableGetMapDecorator;

import java.math.BigDecimal;

@Data
public class BoxBuyDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal boxRate;
    private StockKbar buyKbar;
    private BigDecimal boxPercent;
    private BigDecimal boxMaxExchangeMoney;
    private BigDecimal avgExchangeMoneyDay11;
    private String sellDate;
    private int handleDays;
    private BigDecimal profit;
}
