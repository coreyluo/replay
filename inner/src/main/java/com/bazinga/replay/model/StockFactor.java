package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockFactor〉<p>
 *
 * @author
 * @date 2022-07-06
 */
@lombok.Data
@lombok.ToString
public class StockFactor implements Serializable {

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
     * 交易时间
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
     * 因子1
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index1;

    /**
     * 因子2a
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index2a;

    /**
     * 因子2b
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index2b;

    /**
     * 因子2c
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index2c;

    /**
     * 因子3
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index3;

    /**
     * 因子4
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index4;

    /**
     * 因子5
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index5;

    /**
     * 因子6
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index6;

    /**
     * 因子7
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal index7;

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