package com.bazinga.replay.dao;

import com.bazinga.replay.model.HotCirculateInfo;
import com.bazinga.replay.query.HotCirculateInfoQuery;

import java.util.List;

/**
 * 〈HotCirculateInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-15
 */
public interface HotCirculateInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(HotCirculateInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    HotCirculateInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(HotCirculateInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HotCirculateInfo> selectByCondition(HotCirculateInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(HotCirculateInfoQuery query);

}