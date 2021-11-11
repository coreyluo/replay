package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsQuoteInfo 查询参数〉<p>
 *
 * @author
 * @date 2021-11-11
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ThsQuoteInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 交易日期
     */
    private String quoteDate;


    /**
     * 行情时间
     */
    private String quoteTime;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 买一价
     */
    private BigDecimal bid1;

    /**
     * 买二价
     */
    private BigDecimal bid2;

    /**
     * 卖一价
     */
    private BigDecimal ask1;

    /**
     * 卖二价
     */
    private BigDecimal ask2;

    private Long bidSize1;

    private Long bidSize2;

    private Long askSize1;

    private Long askSize2;

    /**
     * 总卖数量
     */
    private Long totalSellVolume;

    /**
     * 总卖数量
     */
    private Long totalBuyVolume;

    /**
     * 买均价
     */
    private BigDecimal avgBuyPrice;

    /**
     * 卖均价
     */
    private BigDecimal avgSellPrice;

    /**
     * 成交额
     */
    private BigDecimal amt;

    /**
     * 成交量
     */
    private Long vol;

    /**
     * 累计成交额
     */
    private BigDecimal amount;

    /**
     * 累计成交量
     */
    private Long volume;

    /**
     * 创建时间 开始
     */
    private Date createTimeFrom;

    /**
     * 创建时间 结束
     */
    private Date createTimeTo;

    /**
     * 更新时间 开始
     */
    private Date updateTimeFrom;

    /**
     * 更新时间 结束
     */
    private Date updateTimeTo;


}