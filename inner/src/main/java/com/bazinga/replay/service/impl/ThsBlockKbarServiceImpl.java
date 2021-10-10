package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsBlockKbarDAO;
import com.bazinga.replay.model.ThsBlockKbar;
import com.bazinga.replay.query.ThsBlockKbarQuery;
import com.bazinga.replay.service.ThsBlockKbarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsBlockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-29
 */
@Service
public class ThsBlockKbarServiceImpl implements ThsBlockKbarService {

    @Autowired
    private ThsBlockKbarDAO thsBlockKbarDAO;

    @Override
    public ThsBlockKbar save(ThsBlockKbar record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsBlockKbarDAO.insert(record);
        return record;
    }

    @Override
    public ThsBlockKbar getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsBlockKbarDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsBlockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsBlockKbarDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsBlockKbar> listByCondition(ThsBlockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockKbarDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsBlockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockKbarDAO.countByCondition(query);
    }
}