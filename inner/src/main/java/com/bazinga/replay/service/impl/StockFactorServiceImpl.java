package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockFactorDAO;
import com.bazinga.replay.model.StockFactor;
import com.bazinga.replay.query.StockFactorQuery;
import com.bazinga.replay.service.StockFactorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockFactor Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-07-06
 */
@Service
public class StockFactorServiceImpl implements StockFactorService {

    @Autowired
    private StockFactorDAO stockFactorDAO;

    @Override
    public StockFactor save(StockFactor record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockFactorDAO.insert(record);
        return record;
    }

    @Override
    public StockFactor getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockFactorDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockFactor record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockFactorDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockFactor> listByCondition(StockFactorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockFactorDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockFactorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockFactorDAO.countByCondition(query);
    }

    @Override
    public StockFactor getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockFactorDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockFactor record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockFactorDAO.updateByUniqueKey(record);
    }
}