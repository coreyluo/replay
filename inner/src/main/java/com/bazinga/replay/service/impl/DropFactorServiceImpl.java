package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.DropFactorDAO;
import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.query.DropFactorQuery;
import com.bazinga.replay.service.DropFactorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈DropFactor Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
@Service
public class DropFactorServiceImpl implements DropFactorService {

    @Autowired
    private DropFactorDAO dropFactorDAO;

    @Override
    public DropFactor save(DropFactor record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        dropFactorDAO.insert(record);
        return record;
    }

    @Override
    public DropFactor getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return dropFactorDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(DropFactor record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return dropFactorDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<DropFactor> listByCondition(DropFactorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return dropFactorDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(DropFactorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return dropFactorDAO.countByCondition(query);
    }
}