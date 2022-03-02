package com.bazinga.replay.service;

import com.bazinga.replay.model.MarketInfo;
import com.bazinga.replay.query.MarketInfoQuery;

import java.util.List;

/**
 * 〈MarketInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
public interface MarketInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    MarketInfo save(MarketInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    MarketInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(MarketInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<MarketInfo> listByCondition(MarketInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(MarketInfoQuery query);
}