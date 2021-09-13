package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PositionOwnImportDTO {

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

    @ExcelElement("是否炸板")
    private Integer sealType;

    @ExcelElement("正盈利")
    private BigDecimal premium;

    @ExcelElement("盈亏比")
    private BigDecimal premiumRate;

    @ExcelElement("连板情况")
    private String plankInfo;

    @ExcelElement("板块代码")
    private String blockCode;

    @ExcelElement("板块名称")
    private String blockName;

    @ExcelElement("板块涨幅")
    private BigDecimal blockRate;

    @ExcelElement("板块排名")
    private Integer compareNum;

}
