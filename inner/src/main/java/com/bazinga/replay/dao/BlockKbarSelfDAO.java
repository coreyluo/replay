package com.bazinga.replay.dao;

import com.bazinga.replay.model.BlockKbarSelf;
import com.bazinga.replay.query.BlockKbarSelfQuery;

import java.util.List;

/**
 * 〈BlockKbarSelf DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
public interface BlockKbarSelfDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(BlockKbarSelf record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    BlockKbarSelf selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(BlockKbarSelf record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BlockKbarSelf> selectByCondition(BlockKbarSelfQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(BlockKbarSelfQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    BlockKbarSelf selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(BlockKbarSelf record);

}