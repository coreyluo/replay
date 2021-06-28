package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
public class BlockHeadExportDTO {

    @ExcelField(name = "股票代码")
    private String stockCode;

    @ExcelField(name = "股票名称")
    private String stockName;

    @ExcelField(name = "买入日期")
    private String kbarDate;

    @ExcelField(name = "上板时间")
    private String plankTime;

    @ExcelField(name = "板块")
    private String blockCode;

    @ExcelField(name = "龙头地位")
    private Integer maxDragonNum;

    @ExcelField(name = "买入价格")
    private BigDecimal buyPrice;

    @ExcelField(name = "一日溢价")
    private BigDecimal premium;
}
