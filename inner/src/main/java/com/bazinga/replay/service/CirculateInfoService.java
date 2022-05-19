package com.bazinga.replay.service;

import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.query.CirculateInfoQuery;

import java.util.List;

/**
 * 〈CirculateInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-10
 */
public interface CirculateInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    CirculateInfo save(CirculateInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    CirculateInfo getById(Long id);

    void deleteById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(CirculateInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<CirculateInfo> listByCondition(CirculateInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(CirculateInfoQuery query);
}
