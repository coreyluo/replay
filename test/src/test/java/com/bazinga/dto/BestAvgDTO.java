package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestAvgDTO {

    @ExcelElement("股票代码")
    private String stockCode;
    @ExcelElement("股票名称")
    private String stockName;

    @ExcelElement("几日均线")
    private Integer avgDays;
    @ExcelElement("买入日期")
    private String buykbarDate;
    @ExcelElement("卖出日期")
    private String sellkbarDate;
    @ExcelElement("买入价格")
    private BigDecimal buyPrice;
    @ExcelElement("卖出价格")
    private BigDecimal sellPrice;
    @ExcelElement("收益")
    private BigDecimal premium;
}
