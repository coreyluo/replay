package com.bazinga.replay.dto;

import com.bazinga.replay.model.ThsBlockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockDropBuyInfoDTO {

    private String blockCode;

    private BigDecimal blockDropRate;

    private BigDecimal blockRaiseRate;

    private BigDecimal blockDropDayStockExchange;

    private BigDecimal blockRaiseDay5Rate;

    private BigDecimal blockRaiseDayStockRate;

    private ThsBlockKbar dropDayKBar;
    private ThsBlockKbar raiseDayKBar;
}
