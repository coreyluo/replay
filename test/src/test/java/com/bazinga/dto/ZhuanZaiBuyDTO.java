package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ZhuanZaiBuyDTO {
    private String stockCode;

    private String stockName;

    private String marketAmount;

    private String tradeDate;

    private BigDecimal buyPrice;

    private String buyTime;

    private BigDecimal relativeOpenRate;

    private int havePlank;

    private BigDecimal profit;

}
