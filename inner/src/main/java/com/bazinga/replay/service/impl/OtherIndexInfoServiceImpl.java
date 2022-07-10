package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.OtherIndexInfoDAO;
import com.bazinga.replay.model.OtherIndexInfo;
import com.bazinga.replay.query.OtherIndexInfoQuery;
import com.bazinga.replay.service.OtherIndexInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈OtherIndexInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-06-05
 */
@Service
public class OtherIndexInfoServiceImpl implements OtherIndexInfoService {

    @Autowired
    private OtherIndexInfoDAO otherIndexInfoDAO;

    @Override
    public OtherIndexInfo save(OtherIndexInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        otherIndexInfoDAO.insert(record);
        return record;
    }

    @Override
    public OtherIndexInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return otherIndexInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(OtherIndexInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return otherIndexInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<OtherIndexInfo> listByCondition(OtherIndexInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return otherIndexInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(OtherIndexInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return otherIndexInfoDAO.countByCondition(query);
    }
}