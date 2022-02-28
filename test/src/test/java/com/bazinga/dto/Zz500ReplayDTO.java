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

    @ExcelField(name = "买入时排名")
    private Integer rank;

    @ExcelField(name = "500开盘涨幅")
    private BigDecimal openRate500;

    @ExcelField(name = "500最高涨幅")
    private BigDecimal highRate500;

    @ExcelField(name = "500最低点涨幅")
    private BigDecimal lowRate500;

    @ExcelField(name = "500买入时涨幅")
    private BigDecimal buyRate500;

    @ExcelField(name = "500前五分钟最高涨幅")
    private BigDecimal highRateMin5;

    @ExcelField(name = "500前五分钟最低涨幅")
    private BigDecimal lowRateMin5;

    @ExcelField(name = "500前5分钟高于开盘跳数")
    private Integer overOpenCountMin5;

    @ExcelField(name = "最高点相对均价点数")
    private  BigDecimal highRelativeRate;

    @ExcelField(name = "最低点相对均价点数")
    private BigDecimal lowRelativeRate;

    @ExcelField(name = "买入前最高价涨幅")
    private BigDecimal highRate;

    @ExcelField(name = "买入前最低价涨幅")
    private BigDecimal lowRate;

    @ExcelField(name = "买入时均价涨幅")
    private BigDecimal avgRate;

    @ExcelField(name = "买入时涨幅")
    private BigDecimal buyRate;

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "15日涨幅")
    private BigDecimal day15Rate;


    @ExcelField(name = "5日涨幅500")
    private BigDecimal day5Rate500;

    @ExcelField(name = "10日涨幅500")
    private BigDecimal day10Rate500;

    @ExcelField(name = "15日涨幅500")
    private BigDecimal day15Rate500;

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
