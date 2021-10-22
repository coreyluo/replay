package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DaDieBuyDTO {

    private BigDecimal negLineExchangeMoney;
    private BigDecimal negLineEndRate;
    private StockKbar stockKbar;
    private BigDecimal endRate;
    private BigDecimal lowerRate;
    private BigDecimal profit;
}
