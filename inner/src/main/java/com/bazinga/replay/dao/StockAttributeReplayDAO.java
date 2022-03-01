package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockAttributeReplay;
import com.bazinga.replay.query.StockAttributeReplayQuery;

import java.util.List;

/**
 * 〈StockAttributeReplay DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
public interface StockAttributeReplayDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockAttributeReplay record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockAttributeReplay selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockAttributeReplay record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockAttributeReplay> selectByCondition(StockAttributeReplayQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockAttributeReplayQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockAttributeReplay selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockAttributeReplay record);

}