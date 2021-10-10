package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsBlockKbar;
import com.bazinga.replay.query.ThsBlockKbarQuery;

import java.util.List;

/**
 * 〈ThsBlockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
public interface ThsBlockKbarService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsBlockKbar save(ThsBlockKbar record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsBlockKbar getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsBlockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockKbar> listByCondition(ThsBlockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsBlockKbarQuery query);
}