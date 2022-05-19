package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@Slf4j
public class One2TwoSellDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "交易日")
    private String kbarDate;

    @ExcelField(name = "上板时间")
    private String plankTime;

    private Integer quoteCount;

    @ExcelField(name = "板上条数")
    private Integer plankCount;

    @ExcelField(name = "板上成交金额")
    private BigDecimal plankAmount;

    @ExcelField(name = "非板上成交金额")
    private BigDecimal unPlankAmount;
}
