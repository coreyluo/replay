package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankQuantityDivideDTO {


    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buyDate;

    @ExcelField(name = "首封时间")
    private String firstPlankTime;

    @ExcelField(name = "前一日最高点时间")
    private String preHighTime;

    @ExcelField(name = "超过中位价量")
    private BigDecimal overMidAmount;

    @ExcelField(name = "低于中位价量")
    private BigDecimal lowMidAmount;

    @ExcelField(name = "前一日收盘涨幅")
    private BigDecimal preCloseRate;

    @ExcelField(name = "3日涨幅")
    private BigDecimal day3Rate;

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "5日最低到收盘涨幅")
    private BigDecimal day5LowRate;

    @ExcelField(name = "10日最低到收盘涨幅")
    private BigDecimal day10LowRate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "振幅")
    private BigDecimal upmRate;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "买入前一日最高点涨幅")
    private BigDecimal preDayHighRate;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断天数")
    private Integer unPlankHigh;

    @ExcelField(name = "封住1 未封住0")
    private Integer sealType;

    @ExcelField(name = "收益")
    private BigDecimal premium;

}
