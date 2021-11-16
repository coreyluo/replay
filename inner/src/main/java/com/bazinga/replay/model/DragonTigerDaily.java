package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈DragonTigerDaily〉<p>
 *
 * @author
 * @date 2021-11-04
 */
@lombok.Data
@lombok.ToString
public class DragonTigerDaily implements Serializable {

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
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 上榜日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    private String kbarDate;

    /**
     * 买几
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer rank;

    /**
     * 席位
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String chair;

    /**
     * 异常类型
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String abnormalCode;

    /**
     * 上榜理由
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String reason;

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