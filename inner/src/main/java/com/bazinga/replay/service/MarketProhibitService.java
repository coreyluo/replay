package com.bazinga.replay.service;

import com.bazinga.replay.model.MarketProhibit;
import com.bazinga.replay.query.MarketProhibitQuery;

import java.util.List;

/**
 * 〈MarketProhibit Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-21
 */
public interface MarketProhibitService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    MarketProhibit save(MarketProhibit record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    MarketProhibit getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(MarketProhibit record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<MarketProhibit> listByCondition(MarketProhibitQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(MarketProhibitQuery query);
}