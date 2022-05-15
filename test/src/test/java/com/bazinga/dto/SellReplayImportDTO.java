package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SellReplayImportDTO {

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
    private Date kbarDate;

    @ExcelElement("委托时间")
    private Date orderTime;

    @ExcelElement("成交时间")
    private Date tradeTime;

    @ExcelElement("买入方式")
    private String buyType;

   /* @ExcelElement("是否炸板")
    private Integer sealType;*/

    @ExcelElement("盈亏比")
    private BigDecimal premiumRate;

    @ExcelField(name = "流通")
    private Long circulateZ;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "总股本")
    private Long totalQty;

}
