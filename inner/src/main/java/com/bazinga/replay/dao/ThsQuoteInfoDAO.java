package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.query.ThsQuoteInfoQuery;

import java.util.List;

/**
 * 〈ThsQuoteInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-11
 */
public interface ThsQuoteInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsQuoteInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsQuoteInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsQuoteInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsQuoteInfo> selectByCondition(ThsQuoteInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsQuoteInfoQuery query);

}