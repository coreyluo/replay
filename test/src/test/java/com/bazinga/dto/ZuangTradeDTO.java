package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ZuangTradeDTO {

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

    @ExcelField(name = "上证开盘涨幅")
    private BigDecimal shOpenRate;

    @ExcelField(name = "成交金额")
    private BigDecimal tradeAmount;

    @ExcelField(name = "一跳涨幅")
    private BigDecimal rate;

    @ExcelField(name = "封板_1封住 0未封住" )
    private Integer sealType;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
