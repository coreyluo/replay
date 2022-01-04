package com.bazinga.replay.service;

import com.bazinga.replay.model.BondDailyConvert;
import com.bazinga.replay.query.BondDailyConvertQuery;

import java.util.List;

/**
 * 〈BondDailyConvert Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
public interface BondDailyConvertService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BondDailyConvert save(BondDailyConvert record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BondDailyConvert getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BondDailyConvert record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BondDailyConvert> listByCondition(BondDailyConvertQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BondDailyConvertQuery query);
}