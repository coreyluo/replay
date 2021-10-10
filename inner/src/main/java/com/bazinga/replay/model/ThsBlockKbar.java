package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsBlockKbar〉<p>
 *
 * @author
 * @date 2021-09-29
 */
@lombok.Data
@lombok.ToString
public class ThsBlockKbar implements Serializable {

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
     * @是否索引   NO
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
    private String tradeDate;

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
     * 开盘涨幅
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal openRate;

    /**
     * 收盘涨幅
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal closeRate;

    /**
     * 板块5日涨幅
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal closeRateDay5;

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