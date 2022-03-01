package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.PlanksInfoDAO;
import com.bazinga.replay.model.PlanksInfo;
import com.bazinga.replay.query.PlanksInfoQuery;
import com.bazinga.replay.service.PlanksInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈PlanksInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-12-07
 */
@Service
public class PlanksInfoServiceImpl implements PlanksInfoService {

    @Autowired
    private PlanksInfoDAO planksInfoDAO;

    @Override
    public PlanksInfo save(PlanksInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        planksInfoDAO.insert(record);
        return record;
    }

    @Override
    public PlanksInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return planksInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(PlanksInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return planksInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<PlanksInfo> listByCondition(PlanksInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return planksInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(PlanksInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return planksInfoDAO.countByCondition(query);
    }
}