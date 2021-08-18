package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.StockCommonReplayQuery;

import java.util.List;

/**
 * 〈StockCommonReplay DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-08-12
 */
public interface StockCommonReplayDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockCommonReplay record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockCommonReplay selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockCommonReplay record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockCommonReplay> selectByCondition(StockCommonReplayQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockCommonReplayQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockCommonReplay selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockCommonReplay record);

}