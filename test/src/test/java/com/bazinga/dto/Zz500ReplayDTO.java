package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Zz500ReplayDTO {


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


    @ExcelField(name = "买入均价")
    private BigDecimal buyAvgPrice;

    @ExcelField(name = "买入时涨幅")
    private BigDecimal buyRate;

    @ExcelField(name = "总跳数")
    private Integer totalJump;
    @ExcelField(name = "超过均价跳数")
    private Integer overJump;

    @ExcelField(name="流通z")
    private Long circulateZ;

    @ExcelField(name = "10日去最值平均振幅")
    private BigDecimal avgHighLowRate;

    @ExcelField(name = "最近一次阴线跌幅")
    private BigDecimal moonRate;

    @ExcelField(name = "三天前10天最高点的价格")
    private BigDecimal day10HighPrice;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "上一日成交金额")
    private BigDecimal preDayTradeAmount;


    @ExcelField(name = "收益")
    private BigDecimal premium;
}
