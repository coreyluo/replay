package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockAverageLine〉<p>
 *
 * @author
 * @date 2021-10-10
 */
@lombok.Data
@lombok.ToString
public class StockAverageLine implements Serializable {

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
     * 几日均线
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer dayType;

    /**
     * 均价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal averagePrice;

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