package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈DragonTigerDaily 查询参数〉<p>
 *
 * @author
 * @date 2021-11-04
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class DragonTigerDailyQuery extends PagingQuery implements Serializable {

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
     * 上榜日期
     */
    private String kbarDate;

    /**
     * 买几
     */
    private Integer rank;

    /**
     * 席位
     */
    private String chair;

    /**
     * 异常类型
     */
    private String abnormalCode;

    /**
     * 上榜理由
     */
    private String reason;

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