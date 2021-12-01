package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RaiseAndDropBestDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String tradeDate;
    private StockKbar stockKbar;
    private String plankTime;
    private int planks;
    private boolean  endPlankFlag = false;

    private BigDecimal leftRate;
    private BigDecimal rightRate;
    private BigDecimal totalRate;
    private BigDecimal tradeAmount;

    private BigDecimal beforeDay3;
    private BigDecimal beforeDay5;
    private BigDecimal beforeDay10;
    private BigDecimal profit;
}
