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
 * 〈BlockKbarSelf〉<p>
 *
 * @author
 * @date 2021-11-04
 */
@lombok.Data
@lombok.ToString
public class BlockKbarSelf implements Serializable {

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
    private String blockCode;

    /**
     * 股票名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String blockName;

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
     * 开盘价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal openPrice;

    /**
     * 最高价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal highPrice;

    /**
     * 最低价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal lowPrice;

    /**
     * 收盘价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal closePrice;

    /**
     * 成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal tradeAmount;

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