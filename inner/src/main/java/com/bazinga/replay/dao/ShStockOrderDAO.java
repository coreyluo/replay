package com.bazinga.replay.dao;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.query.ShStockOrderQuery;

import java.util.Date;
import java.util.List;

/**
 * 〈ShStockOrder DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-04-26
 */
public interface ShStockOrderDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ShStockOrder record);

    
    /**
     * 根据分表键查询
     *
     * @param dateTrade 
     */
    List<ShStockOrder> selectByDateTrade(Date dateTrade);

    /**
     * 根据分表键更新数据
     *
     * @param record 更新参数
     */
    int updateByDateTrade(ShStockOrder record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ShStockOrder> selectByCondition(ShStockOrderQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
     Integer countByCondition(ShStockOrderQuery query);


    /**
    * 根据查询参数和分表序号查询数据
    *
    * @param query 查询参数
    */
    PagingResult<ShStockOrderQuery, ShStockOrder> selectByConditionWithTable(ShStockOrderQuery query);
}