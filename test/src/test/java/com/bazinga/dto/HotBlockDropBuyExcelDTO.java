package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotBlockDropBuyExcelDTO {

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelElement(value = "stockCode",notNull = true)
    private String stockCode;

    @ExcelElement(value = "交易日期",notNull = true)
    private String tradeDate;

    @ExcelElement(value = "大涨涨幅",notNull = false)
    private BigDecimal blockRaiseRate;

    @ExcelElement(value = "大跌幅度",notNull = false)
    private BigDecimal blockDropRate;

    @ExcelElement(value = "股票大跌日成交量",notNull = false)
    private Long dropDayExchange;

    @ExcelElement(value = "股票大涨日涨幅",notNull = false)
    private BigDecimal raiseDayRate;

    @ExcelElement(value = "大涨日板块5日涨幅",notNull = false)
    private BigDecimal raiseDayBlockRate5;
    @ExcelElement(value = "买入前3天是否涨停",notNull = false)
    private Boolean beforePlankDay3;
    @ExcelElement(value = "股票买入日开盘涨幅",notNull = false)
    private BigDecimal buyDayOpenRate;


    @ExcelElement(value = "盈利",notNull = false)
    private BigDecimal profit;
}
