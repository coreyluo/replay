package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestBuyStockDTO {
    private String stockCode;

    private String stockName;

    private String tradeDate;

    private String reason1;

    private String reason2;

    private String reason3;

    private String reason4;

    private String reason5;

    private BigDecimal profit;
}
