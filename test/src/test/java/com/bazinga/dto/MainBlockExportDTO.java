package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MainBlockExportDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String kbarDate;

    @ExcelField(name = "上板时间")
    private String plankTime;

    @ExcelField(name = "板块名称")
    private String blockName;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断板天数")
    private Integer unPlank;

    @ExcelField(name = "一日溢价")
    private BigDecimal premium;

    @ExcelField(name = "是否封住 1封住 0 未封住")
    private Integer sealType;
}
