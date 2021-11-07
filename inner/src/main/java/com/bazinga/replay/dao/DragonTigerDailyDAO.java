package com.bazinga.replay.dao;

import com.bazinga.replay.model.DragonTigerDaily;
import com.bazinga.replay.query.DragonTigerDailyQuery;

import java.util.List;

/**
 * 〈DragonTigerDaily DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
public interface DragonTigerDailyDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(DragonTigerDaily record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    DragonTigerDaily selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(DragonTigerDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<DragonTigerDaily> selectByCondition(DragonTigerDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(DragonTigerDailyQuery query);

}