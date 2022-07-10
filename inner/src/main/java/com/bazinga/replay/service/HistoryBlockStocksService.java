package com.bazinga.replay.service;

import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.query.HistoryBlockStocksQuery;

import java.util.List;

/**
 * 〈HistoryBlockStocks Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
public interface HistoryBlockStocksService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    HistoryBlockStocks save(HistoryBlockStocks record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    HistoryBlockStocks getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(HistoryBlockStocks record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HistoryBlockStocks> listByCondition(HistoryBlockStocksQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(HistoryBlockStocksQuery query);
}