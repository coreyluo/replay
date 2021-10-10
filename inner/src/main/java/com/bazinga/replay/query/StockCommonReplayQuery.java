package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockCommonReplay 查询参数〉<p>
 *
 * @author
 * @date 2021-08-15
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockCommonReplayQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String stockCode;

    /**
     * 
     */
    private String stockName;

    /**
     * k线时间
     */
    private String kbarDate;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 上午1个小时均价
     */
    private BigDecimal avgPre1Price;

    /**
     * 上午均价涨幅
     */
    private BigDecimal avgPre1Rate;

    /**
     * 2点55到收盘涨幅
     */
    private BigDecimal endRaiseRate55;

    /**
     * 明天板价除以10日内最低价格
     */
    private BigDecimal plankPriceThanLow10;

    /**
     * 10日内平均换手
     */
    private Long avgExchange10;
    private BigDecimal rateDay5;
    private BigDecimal rateDay3;
    private BigDecimal gatherPriceThanLow10;
    private Long planksDay10;

    /**
     *  开始
     */
    private Date createTimeFrom;

    /**
     *  结束
     */
    private Date createTimeTo;

    /**
     *  开始
     */
    private Date updateTimeFrom;

    /**
     *  结束
     */
    private Date updateTimeTo;


}