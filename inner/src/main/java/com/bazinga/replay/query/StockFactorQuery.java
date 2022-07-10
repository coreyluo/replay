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
 * 〈StockFactor 查询参数〉<p>
 *
 * @author
 * @date 2022-07-06
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockFactorQuery extends PagingQuery implements Serializable {

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

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 因子1
     */
    private BigDecimal index1;

    /**
     * 因子2a
     */
    private BigDecimal index2a;

    /**
     * 因子2b
     */
    private BigDecimal index2b;

    /**
     * 因子2c
     */
    private BigDecimal index2c;

    /**
     * 因子3
     */
    private BigDecimal index3;

    /**
     * 因子4
     */
    private BigDecimal index4;

    /**
     * 因子5
     */
    private BigDecimal index5;

    /**
     * 因子6
     */
    private BigDecimal index6;

    /**
     * 因子7
     */
    private BigDecimal index7;

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