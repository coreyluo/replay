package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankerStockReplayDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;
    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String buykbarDate;

    @ExcelElement("买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "涨速发生日期")
    private String rateKbarDate;

    @ExcelField(name = "涨速前后5min成交金额")
    private BigDecimal rateTradeAmount;

    @ExcelField(name="涨速次数")
    private Integer rateCount;

    @ExcelField(name = "板高")
    private Integer plankHigh;

    @ExcelField(name = "断板日")
    private Integer unPlank;

    @ExcelElement("10日平均成交金额")
    private BigDecimal day10AvgTradeAmount;

    @ExcelElement("10日涨幅")
    private BigDecimal day10Rate;

    @ExcelField(name="流通z")
    private Long circulateZ;

    @ExcelField(name="总股本")
    private Long circulate;

    @ExcelField(name = "10日内触及涨停次数")
    private Integer day10PlankCount;

    @ExcelElement("收益")
    private BigDecimal premium;



}
