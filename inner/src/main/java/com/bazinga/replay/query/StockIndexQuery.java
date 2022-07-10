package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockIndex 查询参数〉<p>
 *
 * @author
 * @date 2022-05-23
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockIndexQuery extends PagingQuery implements Serializable {

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
    private String kbarDate;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * macd数值
     */
    private BigDecimal macd;

    /**
     * macd相关diff指标
     */
    private BigDecimal diff;

    /**
     * macd相关dea数值
     */
    private BigDecimal dea;


    private BigDecimal bias6;
    private BigDecimal bias12;
    private BigDecimal bias24;


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