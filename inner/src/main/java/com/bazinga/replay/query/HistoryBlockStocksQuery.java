package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈HistoryBlockStocks 查询参数〉<p>
 *
 * @author
 * @date 2022-05-19
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class HistoryBlockStocksQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 板块代码
     */
    private String blockCode;

    /**
     * 板块名称
     */
    private String blockName;

    /**
     * 板块日期
     */
    private String tradeDate;

    /**
     * 板块具体股票
     */
    private String stocks;

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