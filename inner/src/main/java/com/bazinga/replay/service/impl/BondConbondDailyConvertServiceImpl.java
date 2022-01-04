package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BondConbondDailyConvertDAO;
import com.bazinga.replay.model.BondConbondDailyConvert;
import com.bazinga.replay.query.BondConbondDailyConvertQuery;
import com.bazinga.replay.service.BondConbondDailyConvertService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BondConbondDailyConvert Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
@Service
public class BondConbondDailyConvertServiceImpl implements BondConbondDailyConvertService {

    @Autowired
    private BondConbondDailyConvertDAO bondConbondDailyConvertDAO;

    @Override
    public BondConbondDailyConvert save(BondConbondDailyConvert record) {
        Assert.notNull(record, "待插入记录不能为空");
        bondConbondDailyConvertDAO.insert(record);
        return record;
    }

    @Override
    public BondConbondDailyConvert getById(Long  id) {
        Assert.notNull(id, "主键不能为空");
        return bondConbondDailyConvertDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BondConbondDailyConvert record) {
        Assert.notNull(record, "待更新记录不能为空");
        return bondConbondDailyConvertDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BondConbondDailyConvert> listByCondition(BondConbondDailyConvertQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondConbondDailyConvertDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BondConbondDailyConvertQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondConbondDailyConvertDAO.countByCondition(query);
    }
}