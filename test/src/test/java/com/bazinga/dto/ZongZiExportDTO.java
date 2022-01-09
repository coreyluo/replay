package com.bazinga.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
public class ZongZiExportDTO {

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

    @ExcelField(name = "5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "30日涨幅")
    private BigDecimal day30Rate;

    @ExcelField(name = "10日平均成交金额")
    private BigDecimal day10AvgAmount;

    @ExcelField(name = "总跳数")
    private Integer totalJump;
    @ExcelField(name = "超过均价跳数")
    private Integer overJump;

    @ExcelField(name="流通z")
    private Long circulateZ;

    @ExcelField(name = "最高点相对均价点数")
    private  BigDecimal highRelativeRate;

    @ExcelField(name = "最低点相对均价点数")
    private BigDecimal lowRelativeRate;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "上一日成交金额")
    private BigDecimal preDayTradeAmount;

    @ExcelField(name = "开盘成交金额")
    private BigDecimal openTradeAmount;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
