package com.bazinga.replay.dao;

import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.query.HistoryBlockInfoQuery;

import java.util.List;

/**
 * 〈HistoryBlockInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
public interface HistoryBlockInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(HistoryBlockInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    HistoryBlockInfo selectByPrimaryKey(Long id);


    void deleteByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(HistoryBlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HistoryBlockInfo> selectByCondition(HistoryBlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(HistoryBlockInfoQuery query);

}