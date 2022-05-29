package com.bazinga.dto;


import com.xuxueli.poi.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
public class BlockOpenReplayDTO {

    @ExcelField(name = "板块代码")
    private String blockCode;

    @ExcelField(name = "板块名称")
    private String blockName;

    @ExcelField(name = "交易日期")
    private String kbarDate;

    @ExcelField(name = "200票板块开盘成交额")
    private BigDecimal openAmount200;

    @ExcelField(name = "板块开盘成交额排名")
    private Integer openAmountRank;

    @ExcelField(name = "板块数量排名")
    private Integer openCountRank;

    @ExcelField(name = "板块个股数量")
    private Integer detailSize;

    @ExcelField(name = "200票竞价总金额")
    private BigDecimal totalOpenAmount200;

    @ExcelField(name = "板块竞价总涨幅")
    private BigDecimal totalOpenRate;

    @ExcelField(name = "上证开盘涨幅")
    private BigDecimal shOpenRate;

    @ExcelField(name = "买入前1日涨幅")
    private BigDecimal dayRate;

    @ExcelField(name = "买入前3日涨幅")
    private BigDecimal day3Rate;

    @ExcelField(name = "买入前5日涨幅")
    private BigDecimal day5Rate;

    @ExcelField(name = "当日开盘涨幅")
    private BigDecimal blockOpenRate;

    @ExcelField(name = "30秒超过开盘跳数")
    private Integer overOpenCount;

    @ExcelField(name = "板块前一日封住数量")
    private Integer closePlankCount;

    @ExcelField(name = "板块涨停未封住数量")
    private Integer closeUnPlankCount;

    @ExcelField(name = "前一日最高板位")
    private Integer plankHigh;

    @ExcelField(name = "收益总和")
    private BigDecimal premium;


}