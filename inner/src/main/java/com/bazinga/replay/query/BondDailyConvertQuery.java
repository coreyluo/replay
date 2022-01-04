package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈BondDailyConvert 查询参数〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BondDailyConvertQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  开始
     */
    private Date dateFrom;

    /**
     *  结束
     */
    private Date dateTo;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String exchangeCode;

    /**
     * 
     */
    private Long issueNumber;

    /**
     * 
     */
    private Double convertPrice;

    /**
     * 
     */
    private Double dailyConvertNumber;

    /**
     * 
     */
    private Double accConvertNumber;

    /**
     * 
     */
    private Double accConvertRatio;

    /**
     * 
     */
    private Double convertPremium;

    /**
     * 
     */
    private Double convertPremiumRate;


}