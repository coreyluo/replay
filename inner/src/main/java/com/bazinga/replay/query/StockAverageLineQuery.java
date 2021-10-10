package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockAverageLine 查询参数〉<p>
 *
 * @author
 * @date 2021-10-10
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockAverageLineQuery extends PagingQuery implements Serializable {

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
     * 交易时间
     */
    private String kbarDate;

    private String kbarDateFrom;

    private String kbarDateTo;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 几日均线
     */
    private Integer dayType;

    /**
     * 均价
     */
    private BigDecimal averagePrice;

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