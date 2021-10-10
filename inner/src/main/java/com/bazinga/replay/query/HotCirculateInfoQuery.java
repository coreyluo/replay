package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈HotCirculateInfo 查询参数〉<p>
 *
 * @author
 * @date 2021-09-15
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class HotCirculateInfoQuery extends PagingQuery implements Serializable {

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
     * 总流通量
     */
    private Long circulate;

    /**
     * 类型  0 一亿一下  1 1亿到3亿   2 3-5.5亿  3  5.5-8亿以上  4 8-11亿  5 11亿以上
     */
    private Integer stockType;

    /**
     * 
     */
    private Integer marketType;

    /**
     * 流通量z
     */
    private Long circulateZ;

    /**
     * 创建时间 开始
     */
    private Date createTimeFrom;

    /**
     * 创建时间 结束
     */
    private Date createTimeTo;


}