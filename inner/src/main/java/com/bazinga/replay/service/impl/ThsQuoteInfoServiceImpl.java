package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsQuoteInfoDAO;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.query.ThsQuoteInfoQuery;
import com.bazinga.replay.service.ThsQuoteInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsQuoteInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-11
 */
@Service
public class ThsQuoteInfoServiceImpl implements ThsQuoteInfoService {

    @Autowired
    private ThsQuoteInfoDAO thsQuoteInfoDAO;

    @Override
    public ThsQuoteInfo save(ThsQuoteInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsQuoteInfoDAO.insert(record);
        return record;
    }

    @Override
    public ThsQuoteInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsQuoteInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsQuoteInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsQuoteInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsQuoteInfo> listByCondition(ThsQuoteInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsQuoteInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsQuoteInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsQuoteInfoDAO.countByCondition(query);
    }
}