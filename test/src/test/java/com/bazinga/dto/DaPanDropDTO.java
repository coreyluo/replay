package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DaPanDropDTO {
    private String tradeDate;
    private BigDecimal dropRate;
    private BigDecimal percent;
    private String timeStamp;
    private BigDecimal relativeOpenRate;
    private String preTradeDate;
    private String nextTradeDate;
    private Integer days;
}
