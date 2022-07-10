package com.bazinga.replay.dao;

import com.bazinga.replay.model.OtherIndexInfo;
import com.bazinga.replay.query.OtherIndexInfoQuery;

import java.util.List;

/**
 * 〈OtherIndexInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-05
 */
public interface OtherIndexInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(OtherIndexInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    OtherIndexInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(OtherIndexInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<OtherIndexInfo> selectByCondition(OtherIndexInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(OtherIndexInfoQuery query);

}