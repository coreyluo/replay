package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;

import java.io.Serializable;

/**
 * 〈RedisMonior 查询参数〉<p>
 *
 * @author
 * @date 2022-03-01
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class RedisMoniorQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * key
     */
    private String redisKey;

    /**
     * value
     */
    private String redisValue;

    /**
     * 创建时间 开始
     */
    private Date createTimeFrom;

    /**
     * 创建时间 结束
     */
    private Date createTimeTo;


}