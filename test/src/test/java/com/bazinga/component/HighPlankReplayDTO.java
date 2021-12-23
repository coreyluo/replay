package com.bazinga.component;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HighPlankReplayDTO {

    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "高位板数量")
    private Integer highPlankNum;

    @ExcelField(name = "高位开盘总收益")
    private BigDecimal totalPremium;

    @ExcelField(name = "开盘一字数量")
    private Integer oneLineNum;

    @ExcelField(name = "情绪值")
    private BigDecimal moodValue;

    @ExcelField(name = "可打票数量")
    private Integer buyCount;

    @ExcelField(name = "平均收益")
    private BigDecimal avgPremium;

    @ExcelField(name = "振幅大总和")
    private BigDecimal totalShakeRate;

    @ExcelField(name = "振幅大票数")
    private Integer shakeCount;

}
