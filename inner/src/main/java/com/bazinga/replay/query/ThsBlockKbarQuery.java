package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsBlockKbar 查询参数〉<p>
 *
 * @author
 * @date 2021-09-29
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ThsBlockKbarQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 股票代码
     */
    private String blockCode;

    /**
     * 股票名称
     */
    private String blockName;

    /**
     * 交易时间
     */
    private String tradeDate;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 开盘涨幅
     */
    private BigDecimal openRate;

    /**
     * 收盘涨幅
     */
    private BigDecimal closeRate;

    private BigDecimal tradeAmount;
    private BigDecimal gatherAmount;

    /**
     * 板块5日涨幅
     */
    private BigDecimal closeRateDay5;

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