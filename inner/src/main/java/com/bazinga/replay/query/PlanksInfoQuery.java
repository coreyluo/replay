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
 * 〈PlanksInfo 查询参数〉<p>
 *
 * @author
 * @date 2021-12-07
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class PlanksInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String tradeDate;

    /**
     * 全部封住数
     */
    private Long totalPlanks;

    /**
     * 全部炸板数
     */
    private Long totalBreaks;

    /**
     * 全部涨幅
     */
    private BigDecimal totalRate;

    /**
     * 一板封住数
     */
    private Long onePlanks;

    /**
     * 一板炸板数
     */
    private Long oneBreaks;

    /**
     * 一板涨幅
     */
    private BigDecimal oneRate;

    /**
     * 二板封住数
     */
    private Long twoPlanks;

    /**
     * 二板炸板数
     */
    private Long twoBreaks;

    /**
     * 二板涨幅
     */
    private BigDecimal twoRate;

    /**
     * 三板封住数
     */
    private Long threePlanks;

    /**
     * 三板炸板数
     */
    private Long threeBreaks;

    /**
     * 三板涨幅
     */
    private BigDecimal threeRate;

    /**
     * 高位（4板及以上）封住数
     */
    private Long highPlanks;

    /**
     * 高位（4板及以上）炸板数
     */
    private Long highBreaks;

    /**
     * 高位（4板及以上）涨幅
     */
    private BigDecimal highRate;

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