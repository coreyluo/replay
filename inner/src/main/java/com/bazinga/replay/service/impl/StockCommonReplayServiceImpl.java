package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockCommonReplayDAO;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.service.StockCommonReplayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockCommonReplay Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-08-15
 */
@Service
public class StockCommonReplayServiceImpl implements StockCommonReplayService {

    @Autowired
    private StockCommonReplayDAO stockCommonReplayDAO;

    @Override
    public StockCommonReplay save(StockCommonReplay record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockCommonReplayDAO.insert(record);
        return record;
    }

    @Override
    public StockCommonReplay getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockCommonReplayDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockCommonReplay record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockCommonReplayDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockCommonReplay> listByCondition(StockCommonReplayQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockCommonReplayDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockCommonReplayQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockCommonReplayDAO.countByCondition(query);
    }

    @Override
    public StockCommonReplay getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockCommonReplayDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockCommonReplay record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockCommonReplayDAO.updateByUniqueKey(record);
    }
}