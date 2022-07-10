package com.bazinga.replay.service;

import com.bazinga.replay.model.StockFactor;
import com.bazinga.replay.query.StockFactorQuery;

import java.util.List;

/**
 * 〈StockFactor Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-07-06
 */
public interface StockFactorService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockFactor save(StockFactor record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockFactor getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockFactor record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockFactor> listByCondition(StockFactorQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockFactorQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockFactor getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockFactor record);
}