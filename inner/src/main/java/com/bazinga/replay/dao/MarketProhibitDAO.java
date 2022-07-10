package com.bazinga.replay.dao;

import com.bazinga.replay.model.MarketProhibit;
import com.bazinga.replay.query.MarketProhibitQuery;

import java.util.List;

/**
 * 〈MarketProhibit DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-21
 */
public interface MarketProhibitDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(MarketProhibit record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    MarketProhibit selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(MarketProhibit record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<MarketProhibit> selectByCondition(MarketProhibitQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(MarketProhibitQuery query);

}