package com.bazinga.replay.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCalDTO {

    @ExcelField(name="发生日期")
    private String tradeDate;

    @ExcelField(name="证券代码")
    private String stockCode;

    @ExcelField(name = "证券名称")
    private String stockName;

    @ExcelField(name = "买金额")
    private String buyAmount;

    @ExcelField(name = "卖金额")
    private String sellAmount;

    @ExcelField(name = "正盈利")
    private BigDecimal premium;

    @ExcelField(name = "盈亏比")
    private BigDecimal premiumRate;

    @ExcelField(name = "连板情况")
    private String plankHigh;

    @ExcelField(name="委托时间")
    private String orderTime;

    @ExcelField(name="成交时间")
    private String tradeTime;

    @ExcelField(name = "成交时间差")
    private String subtractTime;

    @ExcelField(name = "是否炸板")
    private Integer sealType;

    @ExcelField(name = "买入方式")
    private String buyStrategy;

    @ExcelField(name = "所属模式")
    private String mode;

    @ExcelField(name = "备注")
    private String market;

    @ExcelField(name = "账号")
    private String accountName;

    @ExcelField(name = "是否规避")
    private Integer voidType;


}
