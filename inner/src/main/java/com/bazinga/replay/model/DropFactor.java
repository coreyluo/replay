package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈DropFactor〉<p>
 *
 * @author
 * @date 2021-10-10
 */
@lombok.Data
@lombok.ToString
public class DropFactor implements Serializable {

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
     * 因子类型
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer factorType;

    /**
     * 板块大跌幅度区段
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal blockDropRate;

    /**
     * 板块大涨幅度区段
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal blockRaiseRate;

    /**
     * 股票大跌日换手区段
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long stockDropDayExchange;

    /**
     * 大涨日板块5日涨幅区段
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal blockRaiseDay5Rate;

    /**
     * 股票在板块大涨日涨幅区段
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal stockRaiseRate;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;

    /**
     * 更新时间
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date updateTime;


}