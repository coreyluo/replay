package com.bazinga.replay.dao;

import com.bazinga.replay.model.MarketInfo;
import com.bazinga.replay.query.MarketInfoQuery;

import java.util.List;

/**
 * 〈MarketInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
public interface MarketInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(MarketInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    MarketInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(MarketInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<MarketInfo> selectByCondition(MarketInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(MarketInfoQuery query);

}