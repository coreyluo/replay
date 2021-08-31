package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class OtherExcelDTO {
    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("代码")
    private String stock;



    /**
     * 流通量z
     *
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("日期")

    private String tradeDate;

    @ExcelElement("买金额")
    private String buyAmount;

    @ExcelElement("卖金额")
    private String sellAmount;
    @ExcelElement("正盈利")
    private String realProfit;
    @ExcelElement("盈亏比")
    private String realProfitRate;

    @ExcelElement("连板情况")
    private String realPlanks;







}
