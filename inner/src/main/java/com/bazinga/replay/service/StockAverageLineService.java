package com.bazinga.replay.service;

import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.query.StockAverageLineQuery;

import java.util.List;

/**
 * 〈StockAverageLine Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
public interface StockAverageLineService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockAverageLine save(StockAverageLine record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockAverageLine getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockAverageLine record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockAverageLine> listByCondition(StockAverageLineQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockAverageLineQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockAverageLine getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockAverageLine record);
}