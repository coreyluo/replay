package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockAttributeReplayDAO;
import com.bazinga.replay.model.StockAttributeReplay;
import com.bazinga.replay.query.StockAttributeReplayQuery;
import com.bazinga.replay.service.StockAttributeReplayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockAttributeReplay Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
@Service
public class StockAttributeReplayServiceImpl implements StockAttributeReplayService {

    @Autowired
    private StockAttributeReplayDAO stockAttributeReplayDAO;

    @Override
    public StockAttributeReplay save(StockAttributeReplay record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockAttributeReplayDAO.insert(record);
        return record;
    }

    @Override
    public StockAttributeReplay getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockAttributeReplayDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockAttributeReplay record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockAttributeReplayDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockAttributeReplay> listByCondition(StockAttributeReplayQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockAttributeReplayDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockAttributeReplayQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockAttributeReplayDAO.countByCondition(query);
    }

    @Override
    public StockAttributeReplay getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockAttributeReplayDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockAttributeReplay record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockAttributeReplayDAO.updateByUniqueKey(record);
    }
}