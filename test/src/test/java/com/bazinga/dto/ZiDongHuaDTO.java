package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class ZiDongHuaDTO {
    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券代码")
    private String stockCode;

    @ExcelElement("证券名称")
    private String stockName;

    @ExcelElement("发生日期")
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

    @ExcelElement("委托时间")
    private String buyTime;


}
