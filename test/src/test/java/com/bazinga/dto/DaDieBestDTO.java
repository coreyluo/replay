package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import com.google.common.collect.Lists;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DaDieBestDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String highDate;
    private StockKbar stockKbar;
    private BigDecimal maxExchangeMoney;
    private BigDecimal highRate;
    private int continuePlanks;
    private int beautifulPlanks;

    private BigDecimal negLineExchangeMoney;
    private BigDecimal negLineEndRate;

    private List<DaDieBuyDTO> buys1 = Lists.newArrayList();
    private List<DaDieBuyDTO> buys2 = Lists.newArrayList();
}
