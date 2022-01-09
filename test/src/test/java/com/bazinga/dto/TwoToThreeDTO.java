package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TwoToThreeDTO {

    private String tradeDate;
    private Integer twoPlanks=0;
    private Integer threePlanks=0;
    private Integer preTwoPlanks = 0;
    private BigDecimal rate;
}
