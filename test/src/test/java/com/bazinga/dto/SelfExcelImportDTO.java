package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SelfExcelImportDTO {
    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("code")
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券名称")
    private String stockName;

    @ExcelElement("day")
    private Date dragonDate;

    @ExcelElement("direction")
    private String direction;

    @ExcelElement("rank")
    private Integer rank;

    @ExcelElement("abnormal_name")
    private String abnormalName;

    @ExcelElement("buy_value")
    private String buyValue;

    @ExcelElement("买入价格")
    private BigDecimal buyPrice;

    private String buyDate;

    private String sellDate;

    @ExcelElement("收益")
    private BigDecimal premium;

    @ExcelElement("开盘涨幅")
    private BigDecimal openRate;

    @ExcelElement("集合成交金额")
    private BigDecimal openTradeAmount;


}
