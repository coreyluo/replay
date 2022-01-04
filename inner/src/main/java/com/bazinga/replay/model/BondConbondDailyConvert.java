package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈BondConbondDailyConvert〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.ToString
public class BondConbondDailyConvert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long id;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date date;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String code;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String name;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String exchangeCode;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long issueNumber;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double convertPrice;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double dailyConvertNumber;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double accConvertNumber;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double accConvertRatio;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double convertPremium;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double convertPremiumRate;


}