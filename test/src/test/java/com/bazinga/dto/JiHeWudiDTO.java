package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class JiHeWudiDTO {
    private String stockCode;

    private BigDecimal rate915;
    private BigDecimal amount915;
    private BigDecimal buyTwoAmount915;
    private BigDecimal buyTwoCount915;

    private BigDecimal rate920;
    private BigDecimal amount920;
    private BigDecimal buyTwoAmount920;
    private BigDecimal buyTwoCount920;

    private BigDecimal rate924;
    private BigDecimal amount924;
    private BigDecimal buyTwoAmount924;
    private BigDecimal buyTwoCount924;

    private BigDecimal rate925;
    private BigDecimal amount925;
    private BigDecimal buyTwoAmount925;
    private BigDecimal buyTwoCount925;



}
