package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.MarketInfoDAO;
import com.bazinga.replay.model.MarketInfo;
import com.bazinga.replay.query.MarketInfoQuery;
import com.bazinga.replay.service.MarketInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈MarketInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-02-19
 */
@Service
public class MarketInfoServiceImpl implements MarketInfoService {

    @Autowired
    private MarketInfoDAO marketInfoDAO;

    @Override
    public MarketInfo save(MarketInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        marketInfoDAO.insert(record);
        return record;
    }

    @Override
    public MarketInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return marketInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(MarketInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return marketInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<MarketInfo> listByCondition(MarketInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return marketInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(MarketInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return marketInfoDAO.countByCondition(query);
    }
}