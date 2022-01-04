package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BondConvertPriceAdjustDAO;
import com.bazinga.replay.model.BondConvertPriceAdjust;
import com.bazinga.replay.query.BondConvertPriceAdjustQuery;
import com.bazinga.replay.service.BondConvertPriceAdjustService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BondConvertPriceAdjust Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
@Service
public class BondConvertPriceAdjustServiceImpl implements BondConvertPriceAdjustService {

    @Autowired
    private BondConvertPriceAdjustDAO bondConvertPriceAdjustDAO;

    @Override
    public BondConvertPriceAdjust save(BondConvertPriceAdjust record) {
        Assert.notNull(record, "待插入记录不能为空");
        bondConvertPriceAdjustDAO.insert(record);
        return record;
    }

    @Override
    public BondConvertPriceAdjust getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return bondConvertPriceAdjustDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BondConvertPriceAdjust record) {
        Assert.notNull(record, "待更新记录不能为空");
        return bondConvertPriceAdjustDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BondConvertPriceAdjust> listByCondition(BondConvertPriceAdjustQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondConvertPriceAdjustDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BondConvertPriceAdjustQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondConvertPriceAdjustDAO.countByCondition(query);
    }
}