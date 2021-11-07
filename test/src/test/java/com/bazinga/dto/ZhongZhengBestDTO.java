package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ZhongZhengBestDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String tradeDate;
    private String fitDate;
    private StockKbar stockKbar;
    private BigDecimal rateDay10;
    private BigDecimal neLineAvgMoney;
    private BigDecimal poLineAvgMoney;
    private BigDecimal profit;

}
