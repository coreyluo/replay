package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockStockBestDTO {

    private String blockCode;
    private String blockName;
    private String highDate;
    private BigDecimal raiseRate;
    private BigDecimal highTradeAmount;
    private BigDecimal dropTradeAmount;
    private String buyDate;
    private BigDecimal dropRate;

    private String stockCode;
    private String stockName;
    private Long circulate;
    private BigDecimal marketMoney;
    private BigDecimal openRate;
    private BigDecimal gatherAmount;
    private BigDecimal blockGatherAmount;
    private BigDecimal block300GatherAmount;
    private BigDecimal blockTotalMarketAmount;
    private Integer detailCount;
    private Integer level;
    private boolean isChungYe;
    private StockKbar buyKbar;
    private BigDecimal beforeRate3;
    private BigDecimal beforeRate10;
    private BigDecimal profitGather;
    private BigDecimal profitBlock;


}
