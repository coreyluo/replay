package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class YiZhiHangYeBuyDTO {
    private String stockCode;
    private String stockName;
    private String blockCode;
    private String blockName;
    private Long circulateZ;
    private String tradeDate;
    private boolean yiZhiFlag = false;
    private String buyTime;
    private StockKbar stockKbar;
    private BigDecimal profit;

}
