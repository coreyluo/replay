package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockIndex〉<p>
 *
 * @author
 * @date 2022-05-23
 */
@lombok.Data
@lombok.ToString
public class StockIndex implements Serializable {

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
     * 股票代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
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
     * macd数值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal macd;

    /**
     * macd相关diff指标
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal diff;

    /**
     * macd相关dea数值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal dea;
    private BigDecimal bias6;
    private BigDecimal bias12;
    private BigDecimal bias24;

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