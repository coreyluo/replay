package com.bazinga.replay.dao;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.model.StockOrder;
import com.bazinga.replay.query.StockOrderQuery;

import java.util.Date;
import java.util.List;

/**
 * 〈StockOrder DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-15
 */
public interface StockOrderDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockOrder record);

    
    /**
     * 根据分表键查询
     *
     * @param dateTrade 
     */
    StockOrder selectByDateTrade(Date dateTrade);

    /**
     * 根据分表键更新数据
     *
     * @param record 更新参数
     */
    int updateByDateTrade(StockOrder record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockOrder> selectByCondition(StockOrderQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
     Integer countByCondition(StockOrderQuery query);


    /**
    * 根据查询参数和分表序号查询数据
    *
    * @param query 查询参数
    */
    PagingResult<StockOrderQuery, StockOrder> selectByConditionWithTable(StockOrderQuery query);
}