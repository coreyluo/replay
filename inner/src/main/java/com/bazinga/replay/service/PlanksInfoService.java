package com.bazinga.replay.service;

import com.bazinga.replay.model.PlanksInfo;
import com.bazinga.replay.query.PlanksInfoQuery;

import java.util.List;

/**
 * 〈PlanksInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-12-07
 */
public interface PlanksInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    PlanksInfo save(PlanksInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    PlanksInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(PlanksInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlanksInfo> listByCondition(PlanksInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(PlanksInfoQuery query);
}