package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.HistoryBlockStocksDAO;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.service.HistoryBlockStocksService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈HistoryBlockStocks Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
@Service
public class HistoryBlockStocksServiceImpl implements HistoryBlockStocksService {

    @Autowired
    private HistoryBlockStocksDAO historyBlockStocksDAO;

    @Override
    public HistoryBlockStocks save(HistoryBlockStocks record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        historyBlockStocksDAO.insert(record);
        return record;
    }

    @Override
    public HistoryBlockStocks getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return historyBlockStocksDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(HistoryBlockStocks record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return historyBlockStocksDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<HistoryBlockStocks> listByCondition(HistoryBlockStocksQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return historyBlockStocksDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(HistoryBlockStocksQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return historyBlockStocksDAO.countByCondition(query);
    }
}