package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsBlockKbar;
import com.bazinga.replay.query.ThsBlockKbarQuery;

import java.util.List;

/**
 * 〈ThsBlockKbar DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
public interface ThsBlockKbarDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsBlockKbar record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsBlockKbar selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsBlockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockKbar> selectByCondition(ThsBlockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsBlockKbarQuery query);

}