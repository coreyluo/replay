package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class YesterdayPlankDTO {


    private String stockCode;
    private String tradeDate;
    private StockKbar buyKbar;
    private BigDecimal startRate;
    private BigDecimal endRate;

}
