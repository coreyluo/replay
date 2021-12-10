package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SuddenAbsortDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name = "跌停日成交量")
    private BigDecimal lastSuddenTradeAmount;

    @ExcelField(name = "封死跌停1 被翘0")
    private Integer suddenClose;

    @ExcelField(name = "跌停日收盘点数")
    private BigDecimal suddenCloseRate;

    @ExcelField(name = "跌停日小于-8数量")
    private Integer day1LowRateCount;

    @ExcelField(name = "跌停2日小于-8数量")
    private Integer day2LowRateCount;

    @ExcelField(name = "收益")
    private BigDecimal premium;


}
