package com.bazinga.replay.service;

import com.bazinga.replay.model.HotBlockDropStock;
import com.bazinga.replay.query.HotBlockDropStockQuery;

import java.util.List;

/**
 * 〈HotBlockDropStock Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
public interface HotBlockDropStockService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    HotBlockDropStock save(HotBlockDropStock record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    HotBlockDropStock getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(HotBlockDropStock record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HotBlockDropStock> listByCondition(HotBlockDropStockQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(HotBlockDropStockQuery query);
}