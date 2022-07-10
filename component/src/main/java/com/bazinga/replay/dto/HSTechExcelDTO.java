package com.bazinga.replay.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class HSTechExcelDTO {



    /**
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("time_key")
    private String time;


    /**
     * 流通量z
     *
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("open")
    private BigDecimal open;
    @ExcelElement("close")
    private BigDecimal close;
    @ExcelElement("high")
    private BigDecimal high;
    @ExcelElement("low")
    private BigDecimal low;


}
