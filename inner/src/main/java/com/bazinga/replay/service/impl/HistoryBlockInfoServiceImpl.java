package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.HistoryBlockInfoDAO;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.service.HistoryBlockInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈HistoryBlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-19
 */
@Service
public class HistoryBlockInfoServiceImpl implements HistoryBlockInfoService {

    @Autowired
    private HistoryBlockInfoDAO historyBlockInfoDAO;

    @Override
    public HistoryBlockInfo save(HistoryBlockInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        historyBlockInfoDAO.insert(record);
        return record;
    }

    @Override
    public HistoryBlockInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return historyBlockInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public void deleteById(Long id) {
        Assert.notNull(id, "主键不能为空");
        historyBlockInfoDAO.deleteByPrimaryKey(id);
    }

    @Override
    public int updateById(HistoryBlockInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return historyBlockInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<HistoryBlockInfo> listByCondition(HistoryBlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return historyBlockInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(HistoryBlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return historyBlockInfoDAO.countByCondition(query);
    }
}