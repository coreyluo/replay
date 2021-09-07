package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class GatherTransactionDataDTO {

    private String tradeTime;

    private BigDecimal tradePrice;

    private Integer tradeQuantity;

    private BigDecimal tradeMoney;


}
