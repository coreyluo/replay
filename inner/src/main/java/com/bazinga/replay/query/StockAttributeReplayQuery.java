package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockAttributeReplay 查询参数〉<p>
 *
 * @author
 * @date 2022-02-19
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockAttributeReplayQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String stockCode;

    /**
     * 
     */
    private String stockName;

    /**
     * 时间
     */
    private String kbarDate;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 10日内平均振幅
     */
    private BigDecimal avgRangeDay10;
    /**
     * 5日涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal rateDay5;
    private BigDecimal rateDay3;

    /**
     * 是不是新股 180天k线
     */
    private Integer marketNew;
    /**
     * 市值
     */
    private BigDecimal marketValue;
    /**
     * 10日内涨停数量
     */
    private Integer planksDay10;

    private BigDecimal highRate;
    private BigDecimal upperShadowRate;
    private BigDecimal avgRate5;
    private String highTime;

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