package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.query.StockAverageLineQuery;

import java.util.List;

/**
 * 〈StockAverageLine DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
public interface StockAverageLineDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockAverageLine record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockAverageLine selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockAverageLine record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockAverageLine> selectByCondition(StockAverageLineQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockAverageLineQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockAverageLine selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockAverageLine record);

}