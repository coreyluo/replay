package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenAmountReplayDTO {

    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "大于500w开盘成交总金额")
    private BigDecimal over500OpenAmount;

    @ExcelField(name = "大于500w数量")
    private Integer over500Count;

    @ExcelField(name = "大于500w总市值")
    private BigDecimal over500TotalMarket;

    @ExcelField(name = "大于5000w数量")
    private Integer over5000Count;
    @ExcelField(name = "大于3000w数量")
    private Integer over3000Count;

    @ExcelField(name = "开盘成交前10总金额")
    private BigDecimal pre10TotalAmount;

    @ExcelField(name = "前10最小金额")
    private BigDecimal minAmount;
}
