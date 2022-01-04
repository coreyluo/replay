package com.bazinga.replay.service;

import com.bazinga.replay.model.BondConbondDailyConvert;
import com.bazinga.replay.query.BondConbondDailyConvertQuery;

import java.util.List;

/**
 * 〈BondConbondDailyConvert Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
public interface BondConbondDailyConvertService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BondConbondDailyConvert save(BondConbondDailyConvert record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BondConbondDailyConvert getById(Long  id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BondConbondDailyConvert record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BondConbondDailyConvert> listByCondition(BondConbondDailyConvertQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BondConbondDailyConvertQuery query);
}