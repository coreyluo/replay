package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BigOrderExcelDTO {

    @ExcelElement( "股票代码")
    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelElement("股票名称")
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelElement("交易日期")
    @ExcelField(name = "交易日期")
    private Date kbarDate;

    @ExcelElement("时间")
    @ExcelField(name = "买入时间")
    private String tradeTime;


    @ExcelField(name = "上板时间")
    @ExcelElement("上板时间")
    private String plankTime;

    @ExcelField(name = "板高")
    @ExcelElement("板高")
    private Integer plankHigh;

    @ExcelField(name = "打板收益")
    @ExcelElement("打板收益")
    private BigDecimal premium;

}
