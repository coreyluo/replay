package com.bazinga.replay.dao;

import com.bazinga.replay.model.TransferableBondInfo;
import com.bazinga.replay.query.TransferableBondInfoQuery;

import java.util.List;

/**
 * 〈TransferableBondInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-21
 */
public interface TransferableBondInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(TransferableBondInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    TransferableBondInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(TransferableBondInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<TransferableBondInfo> selectByCondition(TransferableBondInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(TransferableBondInfoQuery query);

}