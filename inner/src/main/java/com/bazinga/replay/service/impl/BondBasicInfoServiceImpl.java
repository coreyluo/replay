package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BondBasicInfoDAO;
import com.bazinga.replay.model.BondBasicInfo;
import com.bazinga.replay.query.BondBasicInfoQuery;
import com.bazinga.replay.service.BondBasicInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BondBasicInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-01-04
 */
@Service
public class BondBasicInfoServiceImpl implements BondBasicInfoService {

    @Autowired
    private BondBasicInfoDAO bondBasicInfoDAO;

    @Override
    public BondBasicInfo save(BondBasicInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        bondBasicInfoDAO.insert(record);
        return record;
    }

    @Override
    public BondBasicInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return bondBasicInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BondBasicInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return bondBasicInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BondBasicInfo> listByCondition(BondBasicInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondBasicInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BondBasicInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bondBasicInfoDAO.countByCondition(query);
    }
}