package com.bazinga.replay.service;

import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.query.HistoryBlockInfoQuery;

import java.util.List;

/**
 * 〈HistoryBlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
public interface HistoryBlockInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    HistoryBlockInfo save(HistoryBlockInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    HistoryBlockInfo getById(Long id);


    void deleteById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(HistoryBlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<HistoryBlockInfo> listByCondition(HistoryBlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(HistoryBlockInfoQuery query);
}