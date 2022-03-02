package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈MarketInfo 查询参数〉<p>
 *
 * @author
 * @date 2022-02-19
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class MarketInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 市场代码
     */
    private String marketCode;

    /**
     * 市场名称
     */
    private String marketName;

    /**
     * 股票名称
     */
    private String stockCode;

    /**
     * 创建时间 开始
     */
    private Date createTimeFrom;

    /**
     * 创建时间 结束
     */
    private Date createTimeTo;


}