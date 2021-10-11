package com.bazinga.replay.dao;

import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.query.DropFactorQuery;

import java.util.List;

/**
 * 〈DropFactor DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
public interface DropFactorDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(DropFactor record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    DropFactor selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(DropFactor record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<DropFactor> selectByCondition(DropFactorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(DropFactorQuery query);

}