package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShIndexReplayJumpDTO {

    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "第一跳金额")
    private Long firstAmount;

    @ExcelField(name = "开盘涨幅")
    private BigDecimal openRate;

    @ExcelField(name = "放量上涨")
    private String up;

    @ExcelField(name = "上升跳数")
    private Integer upCount;

    @ExcelField(name = "上升金额")
    private Long upAmount;

    @ExcelField(name = "上升变化点数")
    private BigDecimal upChangePoint;

    @ExcelField(name = "放量上涨")
    private String down;

    @ExcelField(name = "下降跳数")
    private Integer downCount;

    @ExcelField(name = "下降金额")
    private Long downAmount;

    @ExcelField(name = "下降变化点数")
    private BigDecimal downChangePoint;

    @ExcelField(name = "买入点数")
    private BigDecimal buyPoint;

    @ExcelField(name = "收盘点数")
    private BigDecimal closePoint;

}
