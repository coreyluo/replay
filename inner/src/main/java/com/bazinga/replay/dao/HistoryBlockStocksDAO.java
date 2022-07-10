package com.bazinga.replay.dao;

import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.query.HistoryBlockStocksQuery;

import java.util.List;

/**
 * 〈HistoryBlockStocks DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
public interface HistoryBlockStocksDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(HistoryBlockStocks record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    HistoryBlockStocks selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(HistoryBlockStocks record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HistoryBlockStocks> selectByCondition(HistoryBlockStocksQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(HistoryBlockStocksQuery query);

}