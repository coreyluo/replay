package com.bazinga.replay.service.impl;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.dao.StockOrderDAO;
import com.bazinga.replay.model.StockOrder;
import com.bazinga.replay.query.StockOrderQuery;
import com.bazinga.replay.service.StockOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockOrder Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-15
 */
@Service
public class StockOrderServiceImpl implements StockOrderService {

    @Autowired
    private StockOrderDAO stockOrderDAO;

    @Override
    public StockOrder save(StockOrder record) {
        Assert.notNull(record, "待插入记录不能为空");
        stockOrderDAO.insert(record);
        return record;
    }

    
    @Override
    public StockOrder getByDateTrade(Date dateTrade) {
        Assert.notNull(dateTrade, "分表键'dateTrade'不能为空");
        return stockOrderDAO.selectByDateTrade(dateTrade);
    }

    @Override
    public int updateByDateTrade(StockOrder record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockOrderDAO.updateByDateTrade(record);
    }

    @Override
    public List<StockOrder> listByCondition(StockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockOrderDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockOrderDAO.countByCondition(query);
    }

    @Override
    public PagingResult<StockOrderQuery, StockOrder> listWithTable(StockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockOrderDAO.selectByConditionWithTable(query);
    }
}