package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockBolling;
import com.bazinga.replay.query.StockBollingQuery;

import java.util.List;

/**
 * 〈StockBolling DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-27
 */
public interface StockBollingDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockBolling record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockBolling selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockBolling record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockBolling> selectByCondition(StockBollingQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockBollingQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockBolling selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockBolling record);

}