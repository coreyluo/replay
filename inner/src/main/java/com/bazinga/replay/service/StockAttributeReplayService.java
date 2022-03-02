package com.bazinga.replay.service;

import com.bazinga.replay.model.StockAttributeReplay;
import com.bazinga.replay.query.StockAttributeReplayQuery;

import java.util.List;

/**
 * 〈StockAttributeReplay Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
public interface StockAttributeReplayService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockAttributeReplay save(StockAttributeReplay record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockAttributeReplay getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockAttributeReplay record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockAttributeReplay> listByCondition(StockAttributeReplayQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockAttributeReplayQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockAttributeReplay getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockAttributeReplay record);
}