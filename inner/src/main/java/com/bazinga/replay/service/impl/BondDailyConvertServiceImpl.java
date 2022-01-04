package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BondDailyConvertDAO;
import com.bazinga.replay.model.BondDailyConvert;
import com.bazinga.replay.query.BondDailyConvertQuery;
import com.bazinga.replay.service.BondDailyConvertService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BondDailyConvert Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
@Service
public class BondDailyConvertServiceImpl implements BondDailyConvertService {

    @Autowired
    private BondDailyConvertDAO bondDailyConvertDAO;

    @Override
    public BondDailyConvert save(BondDailyConvert record) {
        Assert.notNull(record, "待插入记录不能为空");
        bondDailyConvertDAO.insert(record);
        return record;
    }

    @Override
    public BondDailyConvert getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return bondDailyConvertDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BondDailyConvert record) {
        Assert.notNull(record, "待更新记录不能为空");
        return bondDailyConvertDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BondDailyConvert> listByCondition(BondDailyConvertQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondDailyConvertDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BondDailyConvertQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondDailyConvertDAO.countByCondition(query);
    }
}