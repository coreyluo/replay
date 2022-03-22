package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈IndexDetail〉<p>
 *
 * @author
 * @date 2022-03-03
 */
@lombok.Data
@lombok.ToString
public class IndexDetail implements Serializable {

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
     * 指数代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String indexCode;

    /**
     * 指数名称
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String blockName;

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
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 日期
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String kbarDate;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   YES
     */
    private Date createTime;


}