package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockIndexDAO;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.query.StockIndexQuery;
import com.bazinga.replay.service.StockIndexService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockIndex Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-23
 */
@Service
public class StockIndexServiceImpl implements StockIndexService {

    @Autowired
    private StockIndexDAO stockIndexDAO;

    @Override
    public StockIndex save(StockIndex record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockIndexDAO.insert(record);
        return record;
    }

    @Override
    public StockIndex getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockIndexDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockIndex record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockIndexDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockIndex> listByCondition(StockIndexQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockIndexDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockIndexQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockIndexDAO.countByCondition(query);
    }

    @Override
    public StockIndex getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockIndexDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockIndex record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockIndexDAO.updateByUniqueKey(record);
    }
}