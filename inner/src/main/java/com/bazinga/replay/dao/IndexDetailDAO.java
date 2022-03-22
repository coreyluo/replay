package com.bazinga.replay.dao;

import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.query.IndexDetailQuery;

import java.util.List;

/**
 * 〈IndexDetail DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-03
 */
public interface IndexDetailDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(IndexDetail record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    IndexDetail selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(IndexDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<IndexDetail> selectByCondition(IndexDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(IndexDetailQuery query);

}