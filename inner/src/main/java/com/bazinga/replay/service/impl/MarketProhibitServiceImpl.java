package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.MarketProhibitDAO;
import com.bazinga.replay.model.MarketProhibit;
import com.bazinga.replay.query.MarketProhibitQuery;
import com.bazinga.replay.service.MarketProhibitService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈MarketProhibit Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-21
 */
@Service
public class MarketProhibitServiceImpl implements MarketProhibitService {

    @Autowired
    private MarketProhibitDAO marketProhibitDAO;

    @Override
    public MarketProhibit save(MarketProhibit record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        marketProhibitDAO.insert(record);
        return record;
    }

    @Override
    public MarketProhibit getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return marketProhibitDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(MarketProhibit record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return marketProhibitDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<MarketProhibit> listByCondition(MarketProhibitQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return marketProhibitDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(MarketProhibitQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return marketProhibitDAO.countByCondition(query);
    }
}