package com.bazinga.replay.service;

import com.bazinga.replay.model.StockBolling;
import com.bazinga.replay.query.StockBollingQuery;

import java.util.List;

/**
 * 〈StockBolling Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-27
 */
public interface StockBollingService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockBolling save(StockBolling record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockBolling getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockBolling record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockBolling> listByCondition(StockBollingQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockBollingQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockBolling getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockBolling record);
}