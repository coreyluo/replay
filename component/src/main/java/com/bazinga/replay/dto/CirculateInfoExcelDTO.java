package com.bazinga.replay.dto;

import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class CirculateInfoExcelDTO {


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
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("名称")
    private String stockName;


    /**
     * 流通量z
     *
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement(value = "流通z", pattern = "#,#0.0")
    private Float circulateZ;


    @ExcelElement(value = "总股本", pattern = "#,#0.0")
    private Float totalQuantity;
}
