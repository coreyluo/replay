package com.bazinga.replay.service;

import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.StockCommonReplayQuery;

import java.util.List;

/**
 * 〈StockCommonReplay Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-08-15
 */
public interface StockCommonReplayService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockCommonReplay save(StockCommonReplay record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockCommonReplay getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockCommonReplay record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockCommonReplay> listByCondition(StockCommonReplayQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockCommonReplayQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockCommonReplay getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockCommonReplay record);
}