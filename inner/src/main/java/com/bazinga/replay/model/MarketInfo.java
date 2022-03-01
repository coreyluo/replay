package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈MarketInfo〉<p>
 *
 * @author
 * @date 2022-02-19
 */
@lombok.Data
@lombok.ToString
public class MarketInfo implements Serializable {

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
     * 市场代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String marketCode;

    /**
     * 市场名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String marketName;

    /**
     * 股票名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;


}