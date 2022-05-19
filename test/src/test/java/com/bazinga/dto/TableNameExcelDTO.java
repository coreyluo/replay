package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class TableNameExcelDTO {
    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("name")
    private String stockCode;



}
