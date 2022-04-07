package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockAttributeReplay〉<p>
 *
 * @author
 * @date 2022-02-19
 */
@lombok.Data
@lombok.ToString
public class StockAttributeReplay implements Serializable {

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
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    private String stockCode;

    /**
     * 
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 时间
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String kbarDate;

    /**
     * 唯一索引
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   uk_unique_key
     */
    private String uniqueKey;

    /**
     * 10日内平均振幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal avgRangeDay10;

    private BigDecimal avgAmountDay5;

    /**
     * 5日涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal rateDay5;

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

    private Integer closePlanksDay10;

    private BigDecimal highRate;
    private BigDecimal upperShadowRate;
    private BigDecimal avgRate5;
    private String highTime;

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