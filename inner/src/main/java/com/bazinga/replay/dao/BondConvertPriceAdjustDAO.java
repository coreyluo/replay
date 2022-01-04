package com.bazinga.replay.dao;

import com.bazinga.replay.model.BondConvertPriceAdjust;
import com.bazinga.replay.query.BondConvertPriceAdjustQuery;

import java.util.List;

/**
 * 〈BondConvertPriceAdjust DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
public interface BondConvertPriceAdjustDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(BondConvertPriceAdjust record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    BondConvertPriceAdjust selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(BondConvertPriceAdjust record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BondConvertPriceAdjust> selectByCondition(BondConvertPriceAdjustQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(BondConvertPriceAdjustQuery query);

}