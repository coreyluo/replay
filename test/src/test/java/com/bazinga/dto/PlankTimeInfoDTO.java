package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class PlankTimeInfoDTO {
    private String stockCode;

    private String stockName;

    private String tradeDate;

    private int planks;

    private long circulate;

    private BigDecimal marketMoney;

}
