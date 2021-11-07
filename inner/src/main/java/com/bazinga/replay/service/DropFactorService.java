package com.bazinga.replay.service;

import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.query.DropFactorQuery;

import java.util.List;

/**
 * 〈DropFactor Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
public interface DropFactorService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    DropFactor save(DropFactor record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    DropFactor getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(DropFactor record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<DropFactor> listByCondition(DropFactorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(DropFactorQuery query);
}