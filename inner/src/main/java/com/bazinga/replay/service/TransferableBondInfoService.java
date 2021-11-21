package com.bazinga.replay.service;

import com.bazinga.replay.model.TransferableBondInfo;
import com.bazinga.replay.query.TransferableBondInfoQuery;

import java.util.List;

/**
 * 〈TransferableBondInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-21
 */
public interface TransferableBondInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    TransferableBondInfo save(TransferableBondInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    TransferableBondInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(TransferableBondInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<TransferableBondInfo> listByCondition(TransferableBondInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(TransferableBondInfoQuery query);
}