package com.bazinga.replay.service;

import com.bazinga.replay.model.DragonTigerDaily;
import com.bazinga.replay.query.DragonTigerDailyQuery;

import java.util.List;

/**
 * 〈DragonTigerDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
public interface DragonTigerDailyService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    DragonTigerDaily save(DragonTigerDaily record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    DragonTigerDaily getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(DragonTigerDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<DragonTigerDaily> listByCondition(DragonTigerDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(DragonTigerDailyQuery query);
}