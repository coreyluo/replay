package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankFirstSealDTO {


    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name="流通z")
    private Long circulateZ;

    @ExcelField(name = "板上成交金额")
    private BigDecimal plankTradeAmount;

    @ExcelField(name = "上板时间")
    private String plankTime;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断板天数")
    private Integer unPlankHigh;

    @ExcelField(name = "收益")
    private BigDecimal premium;




}
