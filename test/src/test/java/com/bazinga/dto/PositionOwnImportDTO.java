package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

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

    @ExcelElement("是否炸板")
    private Integer sealType;

}
