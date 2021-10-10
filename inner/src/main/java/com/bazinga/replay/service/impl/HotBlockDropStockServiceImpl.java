package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.HotBlockDropStockDAO;
import com.bazinga.replay.model.HotBlockDropStock;
import com.bazinga.replay.query.HotBlockDropStockQuery;
import com.bazinga.replay.service.HotBlockDropStockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈HotBlockDropStock Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
@Service
public class HotBlockDropStockServiceImpl implements HotBlockDropStockService {

    @Autowired
    private HotBlockDropStockDAO hotBlockDropStockDAO;

    @Override
    public HotBlockDropStock save(HotBlockDropStock record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        hotBlockDropStockDAO.insert(record);
        return record;
    }

    @Override
    public HotBlockDropStock getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return hotBlockDropStockDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(HotBlockDropStock record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return hotBlockDropStockDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<HotBlockDropStock> listByCondition(HotBlockDropStockQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return hotBlockDropStockDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(HotBlockDropStockQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return hotBlockDropStockDAO.countByCondition(query);
    }
}