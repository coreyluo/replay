package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.query.StockIndexQuery;

import java.util.List;

/**
 * 〈StockIndex DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-23
 */
public interface StockIndexDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockIndex record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockIndex selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockIndex record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockIndex> selectByCondition(StockIndexQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockIndexQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockIndex selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockIndex record);

}