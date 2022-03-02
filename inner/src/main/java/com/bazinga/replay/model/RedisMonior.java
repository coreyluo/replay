package com.bazinga.replay.model;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈RedisMonior〉<p>
 *
 * @author
 * @date 2022-03-01
 */
@lombok.Data
@lombok.ToString
public class RedisMonior implements Serializable {

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
     * key
     *
     * @最大长度   120
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   uk_key
     */
    private String key;

    /**
     * value
     *
     * @最大长度   2147483647
     * @允许为空   NO
     * @是否索引   NO
     */
    private String value;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;


}