package com.bazinga.replay.dao;

import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.query.RedisMoniorQuery;

import java.util.List;

/**
 * 〈RedisMonior DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-01
 */
public interface RedisMoniorDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(RedisMonior record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    RedisMonior selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(RedisMonior record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<RedisMonior> selectByCondition(RedisMoniorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(RedisMoniorQuery query);

    /**
     * 唯一键redisKey 查询
     *
     * @param redisKey 查询参数
     */
    RedisMonior selectByRedisKey(String redisKey);

    /**
     * 唯一键redisKey 更新
     *
     * @param record 更新参数
     */
    int updateByRedisKey(RedisMonior record);

}