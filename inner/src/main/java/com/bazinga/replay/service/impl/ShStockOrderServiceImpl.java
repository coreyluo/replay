package com.bazinga.replay.service.impl;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.dao.ShStockOrderDAO;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.query.ShStockOrderQuery;
import com.bazinga.replay.service.ShStockOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ShStockOrder Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-04-26
 */
@Service
public class ShStockOrderServiceImpl implements ShStockOrderService {

    @Autowired
    private ShStockOrderDAO shStockOrderDAO;

    @Override
    public ShStockOrder save(ShStockOrder record) {
        Assert.notNull(record, "待插入记录不能为空");
        shStockOrderDAO.insert(record);
        return record;
    }

    
    @Override
    public List<ShStockOrder> getByDateTrade(Date dateTrade) {
        Assert.notNull(dateTrade, "分表键'dateTrade'不能为空");
        return shStockOrderDAO.selectByDateTrade(dateTrade);
    }

    @Override
    public int updateByDateTrade(ShStockOrder record) {
        Assert.notNull(record, "待更新记录不能为空");
        return shStockOrderDAO.updateByDateTrade(record);
    }

    @Override
    public List<ShStockOrder> listByCondition(ShStockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return shStockOrderDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ShStockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return shStockOrderDAO.countByCondition(query);
    }

    @Override
    public PagingResult<ShStockOrderQuery, ShStockOrder> listWithTable(ShStockOrderQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return shStockOrderDAO.selectByConditionWithTable(query);
    }
}