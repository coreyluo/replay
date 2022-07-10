package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈MarketProhibit〉<p>
 *
 * @author
 * @date 2022-06-21
 */
@lombok.Data
@lombok.ToString
public class MarketProhibit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
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
     * @唯一索引   uk_stock_code
     */
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   30
     * @允许为空   YES
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 0 主观禁止 1 华鑫禁止
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer operateStatus;

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