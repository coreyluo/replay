package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotBlockDropBuyExcelDTO {

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelElement("证券代码")
    private String stockCode;

    @ExcelElement("交易日期")
    private String tradeDate;

    @ExcelElement("大涨涨幅")
    private BigDecimal blockRaiseRate;

    @ExcelElement("大跌涨幅")
    private BigDecimal blockDropRate;

    @ExcelElement("股票大跌日成交量")
    private Long dropDayExchange;

    @ExcelElement("股票大涨日涨幅")
    private BigDecimal raiseDayRate;

    @ExcelElement("大涨日板块5日涨幅")
    private BigDecimal raiseDayBlockRate5;

    @ExcelElement("盈利")
    private BigDecimal profit;
}
