package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.query.ThsQuoteInfoQuery;

import java.util.List;

/**
 * 〈ThsQuoteInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-11
 */
public interface ThsQuoteInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsQuoteInfo save(ThsQuoteInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsQuoteInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsQuoteInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsQuoteInfo> listByCondition(ThsQuoteInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsQuoteInfoQuery query);
}