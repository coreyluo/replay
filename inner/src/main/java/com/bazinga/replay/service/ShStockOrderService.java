package com.bazinga.replay.service;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.query.ShStockOrderQuery;

import java.util.Date;
import java.util.List;

/**
 * 〈ShStockOrder Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-04-26
 */
public interface ShStockOrderService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ShStockOrder save(ShStockOrder record);

    
    /**
     * 根据分表键查询
     *
     * @param dateTrade 
     */
    List<ShStockOrder> getByDateTrade(Date dateTrade);

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
    List<ShStockOrder> listByCondition(ShStockOrderQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ShStockOrderQuery query);

    /**
    * 根据查询参数和表序号查询数据
    *
    * @param query 查询参数
    */
    PagingResult<ShStockOrderQuery, ShStockOrder> listWithTable(ShStockOrderQuery query);
}