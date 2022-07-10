package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockFactor;
import com.bazinga.replay.query.StockFactorQuery;

import java.util.List;

/**
 * 〈StockFactor DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-07-06
 */
public interface StockFactorDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockFactor record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockFactor selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockFactor record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockFactor> selectByCondition(StockFactorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockFactorQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockFactor selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockFactor record);

}