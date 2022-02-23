package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LowTrendReplayDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buyKbarDate;

    @ExcelField(name = "买入时是否在均价下")
    private Integer buyUnderLine;

    @ExcelField(name = "买入距离最高点跌幅")
    private BigDecimal lowRate;

    @ExcelField(name="低于均价天数")
    private Integer underLineDays;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "卖出日期")
    private String sellKbarDate;

    @ExcelField(name = "卖出价格")
    private BigDecimal sellPrice;

    @ExcelField(name = "买卖间隔天数")
    private Integer betweenDay;

    @ExcelField(name = "收益")
    private BigDecimal premium;
}
