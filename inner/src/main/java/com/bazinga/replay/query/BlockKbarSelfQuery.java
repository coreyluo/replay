package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈BlockKbarSelf 查询参数〉<p>
 *
 * @author
 * @date 2021-11-04
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BlockKbarSelfQuery extends PagingQuery implements Serializable {

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
    private String kbarDate;

    private String kbarDateFrom;

    private String kbarDateTo;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    private BigDecimal lowPrice;

    /**
     * 收盘价
     */
    private BigDecimal closePrice;

    /**
     * 成交额
     */
    private BigDecimal tradeAmount;

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