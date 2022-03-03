package com.bazinga.replay.service;

import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.query.IndexDetailQuery;

import java.util.List;

/**
 * 〈IndexDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-03
 */
public interface IndexDetailService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    IndexDetail save(IndexDetail record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    IndexDetail getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(IndexDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<IndexDetail> listByCondition(IndexDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(IndexDetailQuery query);
}