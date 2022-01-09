package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ZhuanZaiBuyDTO {
    private String stockCode;

    private String stockName;

    private String marketAmount;

    private String tradeDate;

    private BigDecimal buyPrice;
    private BigDecimal avgPrice;

    private String buyTime;

    private BigDecimal relativeOpenRate;

    private int havePlank;

    private Integer level;

    private Long gatherFenShi;
    private Long beforeBuyTotalSell;
    private Long buyTotalSell;
    private Long beforeBuyFenShi;
    private Long buyFenShi;
    private String sellTime;
    private BigDecimal sellPrice;
    private Long beforeAvgFenShi;
    private int raiseVolFlag = 0;


    private Integer sellType;

    private int isGatherFlag = 0;
    private int isUpperAvg = 0;
    private BigDecimal profit;

}
