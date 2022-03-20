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
    private String buyKbarDate;

    @ExcelField(name = "买入时间")
    private String buyTime;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "最低点到涨停前1日涨幅")
    private BigDecimal low2UpRate;

    @ExcelField(name = "最低点到涨停前1日天数")
    private Integer low2UpDays;

    @ExcelField(name = "平均成交金额")
    private BigDecimal lowAvgAmount;

    @ExcelField(name = "最低点前5日平均成交金额")
    private BigDecimal preDay5LowAvgAmount;

    @ExcelField(name = "买入前一天价格")
    private BigDecimal preBuyPrice;

    @ExcelField(name = "流通z")
    private Long circulateZ;

    @ExcelField(name = "总股本")
    private Long circulate;

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
