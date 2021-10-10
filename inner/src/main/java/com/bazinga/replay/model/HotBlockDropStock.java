package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈HotBlockDropStock〉<p>
 *
 * @author
 * @date 2021-09-29
 */
@lombok.Data
@lombok.ToString
public class HotBlockDropStock implements Serializable {

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
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 板块代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String blockCode;

    /**
     * 板块名称
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String blockName;

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

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