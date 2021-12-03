package com.bazinga.replay.dto;


import com.bazinga.annotation.ExcelElement;
import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
public class PositionCalDTO {

    @ExcelField(name="发生日期")
    @ExcelElement("发生日期")
    private String tradeDate;

    @ExcelField(name="证券代码")
    @ExcelElement("证券代码")
    private String stockCode;

    @ExcelField(name = "证券名称")
    @ExcelElement("证券名称")
    private String stockName;

    @ExcelField(name = "买金额")
    @ExcelElement("买金额")
    private BigDecimal buyAmount;

    @ExcelField(name = "卖金额")
    @ExcelElement("卖金额")
    private BigDecimal sellAmount;

    @ExcelField(name = "正盈利")
    @ExcelElement("正盈利")
    private BigDecimal premium;

    @ExcelField(name = "盈亏比")
    @ExcelElement("盈亏比")
    private BigDecimal premiumRate;

    @ExcelField(name = "连板情况")
    @ExcelElement("连板情况")
    private String plankHigh;

    @ExcelField(name="委托时间")
    @ExcelElement("委托时间")
    private String orderTime;

    @ExcelField(name="成交时间")
    @ExcelElement("成交时间")
    private String tradeTime;

    @ExcelField(name = "成交时间差")
    @ExcelElement("成交时间差")
    private String subtractTime;

    @ExcelField(name = "是否炸板")
    @ExcelElement("是否炸板")
    private Integer sealType;

    @ExcelField(name = "买入方式")
    @ExcelElement("买入方式")
    private String buyStrategy;

    @ExcelField(name = "所属模式")
    @ExcelElement("所属模式")
    private String mode;

    @ExcelField(name = "备注")
    @ExcelElement("备注")
    private String market;

    @ExcelField(name = "账号")
    @ExcelElement("账号")
    private String accountName;

    @ExcelField(name="成交股数")
    @ExcelElement("成交股数")
    private Integer tradeQuantity;


}
