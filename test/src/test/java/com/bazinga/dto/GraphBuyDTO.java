package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class GraphBuyDTO {

    private String stockCode;

    private String stockName;

    private String tradeDate;

    private String buyAmount;

    private String sellAmount;

    private String realProfit;

    private String realProfitRate;


    private String realPlanks;


    private String buyTime;


    private BigDecimal increaseRate;

    private Integer spaceTime;

    private BigDecimal increaseRate10;

    private Integer spaceTime10;




}
