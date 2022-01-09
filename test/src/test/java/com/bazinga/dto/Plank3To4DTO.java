package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Plank3To4DTO {

    @ExcelField(name = "股票代码")
    private String stockCode;
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelElement("买入价格")
    private BigDecimal buyPrice;

    @ExcelElement("收益")
    private BigDecimal premium;
}
