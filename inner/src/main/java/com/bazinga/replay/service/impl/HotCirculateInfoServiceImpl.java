package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.HotCirculateInfoDAO;
import com.bazinga.replay.model.HotCirculateInfo;
import com.bazinga.replay.query.HotCirculateInfoQuery;
import com.bazinga.replay.service.HotCirculateInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈HotCirculateInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-09-15
 */
@Service
public class HotCirculateInfoServiceImpl implements HotCirculateInfoService {

    @Autowired
    private HotCirculateInfoDAO hotCirculateInfoDAO;

    @Override
    public HotCirculateInfo save(HotCirculateInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        hotCirculateInfoDAO.insert(record);
        return record;
    }

    @Override
    public HotCirculateInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return hotCirculateInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(HotCirculateInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return hotCirculateInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<HotCirculateInfo> listByCondition(HotCirculateInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return hotCirculateInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(HotCirculateInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return hotCirculateInfoDAO.countByCondition(query);
    }
}