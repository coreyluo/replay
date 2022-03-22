package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Sell300ExportDTO {

    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券代码")
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

    @ExcelElement("发生日期")
    private String kbarDate;

    @ExcelElement("委托时间")
    private String orderTime;

    @ExcelElement("是否炸板")
    private Integer sealType;

    private Integer competeNum;

    private BigDecimal openRate;

    private Integer overOpen;

    private BigDecimal circulateAmount;
    private BigDecimal circulateZAmount;

/*    @ExcelElement("盈亏比")
    private BigDecimal premiumRate;

    @ExcelField(name = "上午均价收益")
    private BigDecimal monitorSellRate;*/
}
