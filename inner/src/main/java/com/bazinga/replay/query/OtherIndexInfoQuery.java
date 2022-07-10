package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈OtherIndexInfo 查询参数〉<p>
 *
 * @author
 * @date 2022-06-05
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class OtherIndexInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指数代码
     */
    private String indexCode;

    /**
     * 指数名称
     */
    private String indexName;

    /**
     * 日期
     */
    private String tradeDate;

    /**
     * 时间节点
     */
    private String timeStamp;

    /**
     * 指标值
     */
    private BigDecimal indexValue;

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