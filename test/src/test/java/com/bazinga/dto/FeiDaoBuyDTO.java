package com.bazinga.dto;


import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class FeiDaoBuyDTO {

    private String stockCode;
    private String stockName;
    private BigDecimal circulateZ;
    private String tradeDate;
    private LimitQueue<ThirdSecondTransactionDataDTO> limitQueue100;
    private List<FeiDaoRateDTO> buys;
    private BigDecimal buyAvgPrice;
    private Integer planks;
    private String buyTime;
    private Integer plankSecond;
    private BigDecimal profit;


}
