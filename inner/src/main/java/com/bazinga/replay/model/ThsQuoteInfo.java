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
 * 〈ThsQuoteInfo〉<p>
 *
 * @author
 * @date 2021-11-11
 */
@lombok.Data
@lombok.ToString
public class ThsQuoteInfo implements Serializable {

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
    private String quoteDate;


    /**
     * 行情时间
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String quoteTime;

    /**
     * 当前价格
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal currentPrice;

    /**
     * 买一价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal bid1;

    /**
     * 买二价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal bid2;

    /**
     * 卖一价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal ask1;

    /**
     * 卖二价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal ask2;


    private Long bidSize1;

    private Long bidSize2;

    private Long askSize1;

    private Long askSize2;

    /**
     * 总卖数量
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long totalSellVolume;

    /**
     * 总卖数量
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long totalBuyVolume;

    /**
     * 买均价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal avgBuyPrice;

    /**
     * 卖均价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal avgSellPrice;

    /**
     * 成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal amt;

    /**
     * 成交量
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long vol;

    /**
     * 累计成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal amount;

    /**
     * 累计成交量
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long volume;

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