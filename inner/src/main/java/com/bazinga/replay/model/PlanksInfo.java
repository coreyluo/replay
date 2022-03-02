package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈PlanksInfo〉<p>
 *
 * @author
 * @date 2021-12-07
 */
@lombok.Data
@lombok.ToString
public class PlanksInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   PRIMARY
     */
    private Long id;

    /**
     * 
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

    /**
     * 全部封住数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long totalPlanks;

    /**
     * 全部炸板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long totalBreaks;

    /**
     * 全部涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal totalRate;

    /**
     * 一板封住数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long onePlanks;

    /**
     * 一板炸板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long oneBreaks;

    /**
     * 一板涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal oneRate;

    /**
     * 二板封住数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long twoPlanks;

    /**
     * 二板炸板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long twoBreaks;

    /**
     * 二板涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal twoRate;

    /**
     * 三板封住数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long threePlanks;

    /**
     * 三板炸板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long threeBreaks;

    /**
     * 三板涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal threeRate;

    /**
     * 高位（4板及以上）封住数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long highPlanks;

    /**
     * 高位（4板及以上）炸板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long highBreaks;

    /**
     * 高位（4板及以上）涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal highRate;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date updateTime;


}