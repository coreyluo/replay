package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellOrBuyExcelExportDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String buyAmount;
    private String sellAmount;
    private String realProfit;
    private String realProfitRate;
    private String realPlanks;
    private String buyTime;

    private RealBuyOrSellDTO stockRealBuyOrSell;
    private BlockLevelDTO blockLevelDTO;
    private BlockRealBuyOrSellDTO blockRealBuyOrSell;


}
