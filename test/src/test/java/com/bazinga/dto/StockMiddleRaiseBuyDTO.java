package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockMiddleRaiseBuyDTO {

    private String tradeDate;

    private int counts;

    private BigDecimal highAvgRate;

    private String highAvgRateTime;

    private BigDecimal openAvgRate;

    private BigDecimal changeRateTotal;

    private BigDecimal raiseRate;

    private List<StockKbar> kbars;

    private String stockCode;
    private String stockName;
    private BigDecimal openRate;
    private BigDecimal buyRate;
    private BigDecimal buyPrice;
    private StockKbar stockKbar;
    private int planks;
    private boolean endPlank;
    private BigDecimal profit;

}
