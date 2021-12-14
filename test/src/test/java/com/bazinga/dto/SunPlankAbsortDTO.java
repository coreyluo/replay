package com.bazinga.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SunPlankAbsortDTO {


    @ExcelField(name = "股票代码")
    private String stockCode;
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "买入涨幅")
    private BigDecimal buyRate;

    @ExcelField(name = "集合成交金额排名")
    private Integer competeNum;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断板天数")
    private Integer unPlankHigh;

    @ExcelField(name = "前一日成交额")
    private BigDecimal preTradeAmount;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "前一天开盘涨幅")
    private BigDecimal preOpenRate;

    @ExcelField(name = "开盘成交金额")
    private BigDecimal openTradeAmount;

    @ExcelField(name = "流通z")
    private Long circulateZ;

    @ExcelField(name = "流通市值z")
    private BigDecimal circulateAmountZ;

    @ExcelField(name = "前一天涨停数量")
    private Integer prePlankNum;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
