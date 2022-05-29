package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ZuangLowAmountDTO {

    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "流通z")
    private Long  circulateZ;

    @ExcelField(name = "发生时间")
    private String tradeTime;

    @ExcelField(name = "上板时间")
    private String plankTime;

    @ExcelField(name = "委托时间")
    private String orderTime;

    @ExcelField(name = "上证开盘涨幅")
    private BigDecimal shOpenRate;

    @ExcelField(name = "最大成交金额")
    private BigDecimal tradeAmount;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "开盘成交额")
    private BigDecimal openAmount;

    @ExcelField(name = "板高")
    private String plankHigh;

    @ExcelField(name = "封板_1封住 0未封住" )
    private Integer sealType;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
