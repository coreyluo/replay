package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈HotBlockDropStock 查询参数〉<p>
 *
 * @author
 * @date 2021-09-29
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class HotBlockDropStockQuery extends PagingQuery implements Serializable {

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
     * 板块代码
     */
    private String blockCode;

    /**
     * 板块名称
     */
    private String blockName;

    /**
     * 交易日期
     */
    private String tradeDate;

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