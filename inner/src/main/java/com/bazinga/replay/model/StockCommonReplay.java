package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockCommonReplay〉<p>
 *
 * @author
 * @date 2021-08-15
 */
@lombok.Data
@lombok.ToString
public class StockCommonReplay implements Serializable {

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
     * k线时间
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
     * 上午1个小时均价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal avgPre1Price;

    /**
     * 上午均价涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal avgPre1Rate;

    /**
     * 2点55到收盘涨幅
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal endRaiseRate55;

    /**
     * 明天板价除以10日内最低价格
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal plankPriceThanLow10;

    /**
     * 10日内平均换手
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long avgExchange10;

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