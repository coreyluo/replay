package com.bazinga.replay.service;

import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.query.StockIndexQuery;

import java.util.List;

/**
 * 〈StockIndex Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-23
 */
public interface StockIndexService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockIndex save(StockIndex record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockIndex getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockIndex record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockIndex> listByCondition(StockIndexQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockIndexQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockIndex getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockIndex record);
}