package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class KBarDTO {
    //交易日期
    private Date date;
    //交易日期字符串 yyyy-mm-dd
    private String dateStr;
    //开盘价格
    private BigDecimal startPrice;
    //收盘价格
    private BigDecimal endPrice;
    //最高价
    private BigDecimal highestPrice;
    //最低价
    private BigDecimal lowestPrice;
    //总量
    private Long totalExchange;
    //总额
    private Long totalExchangeMoney;

    private BigDecimal adjStartPrice;
    private BigDecimal adjHighPrice;
    private BigDecimal adjLowPrice;
    private BigDecimal adjEndPrice;
}
