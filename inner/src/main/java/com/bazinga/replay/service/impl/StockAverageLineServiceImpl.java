package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockAverageLineDAO;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.service.StockAverageLineService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockAverageLine Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-10-10
 */
@Service
public class StockAverageLineServiceImpl implements StockAverageLineService {

    @Autowired
    private StockAverageLineDAO stockAverageLineDAO;

    @Override
    public StockAverageLine save(StockAverageLine record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockAverageLineDAO.insert(record);
        return record;
    }

    @Override
    public StockAverageLine getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockAverageLineDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockAverageLine record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockAverageLineDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockAverageLine> listByCondition(StockAverageLineQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockAverageLineDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockAverageLineQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockAverageLineDAO.countByCondition(query);
    }

    @Override
    public StockAverageLine getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockAverageLineDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockAverageLine record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockAverageLineDAO.updateByUniqueKey(record);
    }
}