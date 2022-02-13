package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HighPositionDTO {

    private String stockCode;
    private String stockName;
    private BigDecimal circulateZ;
    private BigDecimal marketAmount;
    private StockKbar stockKbar;
    private String tradeDate;
    private String plankTime;
    private Integer highPlanks;
    private Integer endPlanks;
    private Boolean yesterdayEndPlankFlag;
    private Boolean yesterdayHighPlankFlag;
    private boolean endPlankFlag;
    private BigDecimal profit;

}
