package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManyCannonDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buyKbarDate;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "收盘涨幅")
    private BigDecimal closeRate;

    @ExcelField(name = "前1日收盘涨幅")
    private BigDecimal pre1CloseRate;

    @ExcelField(name = "前2日收盘涨幅")
    private BigDecimal pre2CloseRate;

    @ExcelField(name = "收益")
    private BigDecimal premium;

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "3日涨幅")
    private BigDecimal day3Rate;

}
