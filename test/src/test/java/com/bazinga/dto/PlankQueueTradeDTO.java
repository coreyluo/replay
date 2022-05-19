package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankQueueTradeDTO {


    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "排队数量")
    private Integer queueCount;

    @ExcelField(name = "成交数量")
    private Integer tradeCount;

    @ExcelField(name = "上证涨幅")
    private BigDecimal shRate;

    @ExcelField(name = "收益总数")
    private BigDecimal premium;
}
