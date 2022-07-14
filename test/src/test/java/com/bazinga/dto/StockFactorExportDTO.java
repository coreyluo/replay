package com.bazinga.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockFactorExportDTO {

    /**
     * 股票代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    @ExcelField(name = "股票代码")
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "股票名称")
    private String stockName;

    /**
     * 交易时间
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "买入日期")
    private String kbarDate;
    /**
     * 因子1
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子1")
    private BigDecimal index1;

    /**
     * 因子2a
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子2a")
    private BigDecimal index2a;

    /**
     * 因子2b
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子2b")
    private BigDecimal index2b;

    /**
     * 因子2c
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子2c")
    private BigDecimal index2c;

    /**
     * 因子3
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子3")
    private BigDecimal index3;

    /**
     * 因子4
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子4")
    private BigDecimal index4;

    /**
     * 因子5
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子5")
    private BigDecimal index5;

    /**
     * 因子6
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子6")
    private BigDecimal index6;

    /**
     * 因子7
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    @ExcelField(name = "因子7")
    private BigDecimal index7;

    @ExcelField(name = "1日收益")
    private BigDecimal premiumDay1;
    @ExcelField(name = "2日收益")
    private BigDecimal premiumDay2;
    @ExcelField(name = "3日收益")
    private BigDecimal premiumDay3;
    @ExcelField(name = "4日收益")
    private BigDecimal premiumDay4;
    @ExcelField(name = "5日收益")
    private BigDecimal premiumDay5;
    @ExcelField(name = "6日收益")
    private BigDecimal premiumDay6;
    @ExcelField(name = "7日收益")
    private BigDecimal premiumDay7;
    @ExcelField(name = "8日收益")
    private BigDecimal premiumDay8;
    @ExcelField(name = "9日收益")
    private BigDecimal premiumDay9;
    @ExcelField(name = "10日收益")
    private BigDecimal premiumDay10;
}
