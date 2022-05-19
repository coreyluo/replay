package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsBlockIndustryDetailDAO;
import com.bazinga.replay.model.ThsBlockIndustryDetail;
import com.bazinga.replay.query.ThsBlockIndustryDetailQuery;
import com.bazinga.replay.service.ThsBlockIndustryDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsBlockIndustryDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-05-15
 */
@Service
public class ThsBlockIndustryDetailServiceImpl implements ThsBlockIndustryDetailService {

    @Autowired
    private ThsBlockIndustryDetailDAO thsBlockIndustryDetailDAO;

    @Override
    public ThsBlockIndustryDetail save(ThsBlockIndustryDetail record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsBlockIndustryDetailDAO.insert(record);
        return record;
    }

    @Override
    public ThsBlockIndustryDetail getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsBlockIndustryDetailDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsBlockIndustryDetail record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsBlockIndustryDetailDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsBlockIndustryDetail> listByCondition(ThsBlockIndustryDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockIndustryDetailDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsBlockIndustryDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockIndustryDetailDAO.countByCondition(query);
    }
}