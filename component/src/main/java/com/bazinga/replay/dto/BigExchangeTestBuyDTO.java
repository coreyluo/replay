package com.bazinga.replay.dto;

import com.bazinga.replay.model.ThsBlockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BigExchangeTestBuyDTO {

    private String tradeDate;

    private BigDecimal bigOrderAmountB = BigDecimal.ZERO;

    private Integer bigOrderBTimes = 0;

    private BigDecimal bigOrderAmountS = BigDecimal.ZERO;

    private Integer bigOrderSTimes = 0;
}
