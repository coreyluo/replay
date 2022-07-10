package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈OtherIndexInfo〉<p>
 *
 * @author
 * @date 2022-06-05
 */
@lombok.Data
@lombok.ToString
public class OtherIndexInfo implements Serializable {

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
    private String indexName;

    /**
     * 日期
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String tradeDate;

    /**
     * 时间节点
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String timeStamp;

    /**
     * 指标值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal indexValue;

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