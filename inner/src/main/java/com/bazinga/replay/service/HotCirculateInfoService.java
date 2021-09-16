package com.bazinga.replay.service;

import com.bazinga.replay.model.HotCirculateInfo;
import com.bazinga.replay.query.HotCirculateInfoQuery;

import java.util.List;

/**
 * 〈HotCirculateInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-15
 */
public interface HotCirculateInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    HotCirculateInfo save(HotCirculateInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    HotCirculateInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(HotCirculateInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HotCirculateInfo> listByCondition(HotCirculateInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(HotCirculateInfoQuery query);
}