package com.bazinga.replay.service;

import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.query.RedisMoniorQuery;

import java.util.List;

/**
 * 〈RedisMonior Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-01
 */
public interface RedisMoniorService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    RedisMonior save(RedisMonior record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    RedisMonior getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(RedisMonior record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<RedisMonior> listByCondition(RedisMoniorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(RedisMoniorQuery query);

    /**
     * 唯一键key 查询
     *
     * @param key 查询参数
     */
    RedisMonior getByKey(String key);

    /**
     * 唯一键key 更新
     *
     * @param record 更新参数
     */
    int updateByKey(RedisMonior record);
}