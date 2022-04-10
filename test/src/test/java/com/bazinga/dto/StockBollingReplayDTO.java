package com.bazinga.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockBollingReplayDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelField(name = "买入时间")
    private String buyTime;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "买入时涨幅")
    private BigDecimal buyRate;

    @ExcelField(name = "3日涨幅")
    private BigDecimal day3Rate;

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "前一天成功封板数量")
    private Integer marketSealCount;

    @ExcelField(name = "最近10日封板次数")
    private Integer day10SealCount;

    @ExcelField(name="前一天收盘涨幅")
    private BigDecimal preCloseRate;

    @ExcelField(name = "前5日平均成交金额")
    private BigDecimal preDay5AvgAmount;

    @ExcelField(name = "前一日成交金额")
    private BigDecimal preDayAmount;

    @ExcelField(name = "布林带系数")
    private BigDecimal bollRatio;

    @ExcelField(name = "前三到8日涨幅")
    private BigDecimal day3to8Rate;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
