package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈TransferableBondInfo〉<p>
 *
 * @author
 * @date 2021-11-21
 */
@lombok.Data
@lombok.ToString
public class TransferableBondInfo implements Serializable {

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
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 正股代码
     *
     * @最大长度   10
     * @允许为空   YES
     * @是否索引   NO
     */
    private String mainCode;

    /**
     * 正股名称
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String mainName;

    /**
     * 市值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long marketValue;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;


}