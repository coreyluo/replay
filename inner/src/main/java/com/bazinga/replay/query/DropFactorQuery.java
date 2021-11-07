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
 * 〈DropFactor 查询参数〉<p>
 *
 * @author
 * @date 2021-10-10
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class DropFactorQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 因子类型
     */
    private Integer factorType;

    /**
     * 板块大跌幅度区段
     */
    private BigDecimal blockDropRate;

    /**
     * 板块大涨幅度区段
     */
    private BigDecimal blockRaiseRate;

    /**
     * 股票大跌日换手区段
     */
    private Long stockDropDayExchange;

    /**
     * 大涨日板块5日涨幅区段
     */
    private BigDecimal blockRaiseDay5Rate;

    /**
     * 股票在板块大涨日涨幅区段
     */
    private BigDecimal stockRaiseRate;

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