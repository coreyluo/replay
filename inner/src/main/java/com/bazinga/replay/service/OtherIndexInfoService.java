package com.bazinga.replay.service;

import com.bazinga.replay.model.OtherIndexInfo;
import com.bazinga.replay.query.OtherIndexInfoQuery;

import java.util.List;

/**
 * 〈OtherIndexInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-05
 */
public interface OtherIndexInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    OtherIndexInfo save(OtherIndexInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    OtherIndexInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(OtherIndexInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<OtherIndexInfo> listByCondition(OtherIndexInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(OtherIndexInfoQuery query);
}