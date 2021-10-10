package com.bazinga.replay.dao;

import com.bazinga.replay.model.HotBlockDropStock;
import com.bazinga.replay.query.HotBlockDropStockQuery;

import java.util.List;

/**
 * 〈HotBlockDropStock DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
public interface HotBlockDropStockDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(HotBlockDropStock record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    HotBlockDropStock selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(HotBlockDropStock record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HotBlockDropStock> selectByCondition(HotBlockDropStockQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(HotBlockDropStockQuery query);

}