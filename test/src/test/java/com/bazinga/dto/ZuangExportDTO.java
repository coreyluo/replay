package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ZuangExportDTO {

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

    @ExcelField(name="流通市值z")
    private BigDecimal circulateAmountZ;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "")
    private Integer min2Plank;

    @ExcelField(name = "开盘成交金额")
    private BigDecimal openTradeAmount;

    @ExcelField(name = "收益")
    private BigDecimal premium;

}
