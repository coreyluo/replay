package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockKbarSumInfoDTO {
    private String stockCode;
    //交易日期字符串 yyyymmdd
    private String dateStr;
    //开盘涨幅
    private BigDecimal openRate;
    //收盘涨幅
    private BigDecimal endRate;
    //5日涨幅
    private BigDecimal endRateDay5;
}
