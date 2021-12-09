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


    @ExcelField(name = "收益")
    private BigDecimal premium;


}
