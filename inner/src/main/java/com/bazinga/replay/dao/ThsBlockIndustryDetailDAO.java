package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsBlockIndustryDetail;
import com.bazinga.replay.query.ThsBlockIndustryDetailQuery;

import java.util.List;

/**
 * 〈ThsBlockIndustryDetail DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-15
 */
public interface ThsBlockIndustryDetailDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsBlockIndustryDetail record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsBlockIndustryDetail selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsBlockIndustryDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockIndustryDetail> selectByCondition(ThsBlockIndustryDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsBlockIndustryDetailQuery query);

}