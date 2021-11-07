package com.bazinga.replay.service;

import com.bazinga.replay.model.BlockKbarSelf;
import com.bazinga.replay.query.BlockKbarSelfQuery;

import java.util.List;

/**
 * 〈BlockKbarSelf Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
public interface BlockKbarSelfService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BlockKbarSelf save(BlockKbarSelf record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BlockKbarSelf getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BlockKbarSelf record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BlockKbarSelf> listByCondition(BlockKbarSelfQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BlockKbarSelfQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    BlockKbarSelf getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(BlockKbarSelf record);
}