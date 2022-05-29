package com.bazinga.dto;

import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SHIndexReplayDTO {


    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "放量上涨")
    private String direction;

    @ExcelField(name = "变化点数")
    private BigDecimal changePoint;

    @ExcelField(name = "变化次数")
    private Integer changCount;

    @ExcelField(name = "变化金额")
    private Long changeAmount;

    @ExcelField(name = "放量下跌")
    private String direction2;

    @ExcelField(name = "变化点数")
    private BigDecimal changePoint2;

    @ExcelField(name = "变化次数")
    private Integer changCount2;

    @ExcelField(name = "变化金额")
    private Long changeAmount2;

    @ExcelField(name = "缩量上涨")
    private String direction3;

    @ExcelField(name = "变化点数")
    private BigDecimal changePoint3;

    @ExcelField(name = "变化次数")
    private Integer changCount3;

    @ExcelField(name = "变化金额")
    private Long changeAmount3;

    @ExcelField(name = "缩量下跌")
    private String direction4;

    @ExcelField(name = "变化点数")
    private BigDecimal changePoint4;

    @ExcelField(name = "变化次数")
    private Integer changCount4;

    @ExcelField(name = "变化金额")
    private Long changeAmount4;
}
