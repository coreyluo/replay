package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockBollingDAO;
import com.bazinga.replay.model.StockBolling;
import com.bazinga.replay.query.StockBollingQuery;
import com.bazinga.replay.service.StockBollingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockBolling Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-27
 */
@Service
public class StockBollingServiceImpl implements StockBollingService {

    @Autowired
    private StockBollingDAO stockBollingDAO;

    @Override
    public StockBolling save(StockBolling record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockBollingDAO.insert(record);
        return record;
    }

    @Override
    public StockBolling getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockBollingDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockBolling record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockBollingDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockBolling> listByCondition(StockBollingQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockBollingDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockBollingQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockBollingDAO.countByCondition(query);
    }

    @Override
    public StockBolling getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockBollingDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockBolling record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockBollingDAO.updateByUniqueKey(record);
    }
}