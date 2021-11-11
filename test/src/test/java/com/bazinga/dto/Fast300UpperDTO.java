package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Fast300UpperDTO {

    @ExcelElement("股票代码")
    private String stockCode;
    @ExcelElement("股票名称")
    private String stockName;

    @ExcelElement("买入日期")
    private String buykbarDate;
    @ExcelElement("卖出日期")
    private String sellkbarDate;
    @ExcelElement("买入价格")
    private BigDecimal buyPrice;

    @ExcelElement("距离均价涨幅")
    private BigDecimal relativeAvgRate;

    @ExcelElement("上板时间")
    private String plankTime;

    @ExcelElement("2分钟涨速")
    private BigDecimal rate2min;

    @ExcelElement("收益")
    private BigDecimal premium;
}
