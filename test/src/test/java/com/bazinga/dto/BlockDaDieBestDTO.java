package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import com.google.common.collect.Lists;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BlockDaDieBestDTO {

    private String blockCode;
    private String blockName;
    private String highDate;
    private BigDecimal raiseRate;
    private BigDecimal highTradeAmount;
    private BigDecimal dropTradeAmount;
    private String buyDate;
    private BigDecimal dropRate;

}
