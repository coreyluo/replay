package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockBuyProfitDTO {
    private BigDecimal avgProfit;
    private int count=0;
}
