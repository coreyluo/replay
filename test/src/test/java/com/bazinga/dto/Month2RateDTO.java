package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class Month2RateDTO {

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

    @ExcelElement("最高点涨幅")
    private BigDecimal highPriceRate;

    @ExcelElement("距离最高点的交易天数")
    private Integer daysToHigh;
    @ExcelElement("收益")
    private BigDecimal premium;
}
