package com.bazinga.replay.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class ZiDingYiBlockInfoExcelDTO {


    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("代码")
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("名称")
    private String stockName;


}
