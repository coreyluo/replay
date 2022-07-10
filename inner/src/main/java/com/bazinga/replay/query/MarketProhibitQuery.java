package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈MarketProhibit 查询参数〉<p>
 *
 * @author
 * @date 2022-06-21
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class MarketProhibitQuery extends PagingQuery implements Serializable {

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
     * 0 主观禁止 1 华鑫禁止
     */
    private Integer operateStatus;

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