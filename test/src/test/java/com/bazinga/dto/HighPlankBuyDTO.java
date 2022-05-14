package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class HighPlankBuyDTO {

    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal buyPrice;
    private  Long circulateZ;
    private String buyTime;
    private String planks;
    private int endFlag;
    private BigDecimal profit;
}
