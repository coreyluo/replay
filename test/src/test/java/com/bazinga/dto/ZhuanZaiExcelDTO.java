package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class ZhuanZaiExcelDTO {
    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("代码")
    private String stockCode;

    @ExcelElement("名称")
    private String stockName;

    @ExcelElement("流通市值")
    private String marketAmount;


}
