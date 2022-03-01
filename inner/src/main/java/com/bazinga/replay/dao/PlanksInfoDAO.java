package com.bazinga.replay.dao;

import com.bazinga.replay.model.PlanksInfo;
import com.bazinga.replay.query.PlanksInfoQuery;

import java.util.List;

/**
 * 〈PlanksInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-12-07
 */
public interface PlanksInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(PlanksInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    PlanksInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(PlanksInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlanksInfo> selectByCondition(PlanksInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(PlanksInfoQuery query);

}