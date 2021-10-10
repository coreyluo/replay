package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈HotCirculateInfo〉<p>
 *
 * @author
 * @date 2021-09-15
 */
@lombok.Data
@lombok.ToString
public class HotCirculateInfo implements Serializable {

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
     * 总流通量
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long circulate;

    /**
     * 类型  0 一亿一下  1 1亿到3亿   2 3-5.5亿  3  5.5-8亿以上  4 8-11亿  5 11亿以上
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer stockType;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer marketType;

    /**
     * 流通量z
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long circulateZ;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;


}