package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsBlockIndustryDetail;
import com.bazinga.replay.query.ThsBlockIndustryDetailQuery;

import java.util.List;

/**
 * 〈ThsBlockIndustryDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-15
 */
public interface ThsBlockIndustryDetailService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsBlockIndustryDetail save(ThsBlockIndustryDetail record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsBlockIndustryDetail getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsBlockIndustryDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockIndustryDetail> listByCondition(ThsBlockIndustryDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsBlockIndustryDetailQuery query);
}