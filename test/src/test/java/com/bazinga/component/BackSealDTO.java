package com.bazinga.component;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BackSealDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buyDate;

    @ExcelField(name = "首封时间")
    private String firstPlankTime;

    @ExcelField(name = "回封时间")
    private String backSealTime;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断天数")
    private Integer unPlankHigh;

    @ExcelField(name = "封住1 未封住0")
    private Integer sealType;

    @ExcelField(name = "一字开1 ")
    private Integer oneLineOpen;

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "30日涨幅")
    private BigDecimal day30Rate;

    @ExcelField(name = "上证开盘")
    private BigDecimal shOpenRate;

    @ExcelField(name = "上一日成交额")
    private BigDecimal preDayAmount;

    @ExcelField(name = "当日买入时成交额")
    private BigDecimal totalTradeAmount;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
