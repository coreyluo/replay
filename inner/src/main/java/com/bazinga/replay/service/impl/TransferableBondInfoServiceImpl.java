package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.TransferableBondInfoDAO;
import com.bazinga.replay.model.TransferableBondInfo;
import com.bazinga.replay.query.TransferableBondInfoQuery;
import com.bazinga.replay.service.TransferableBondInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈TransferableBondInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-21
 */
@Service
public class TransferableBondInfoServiceImpl implements TransferableBondInfoService {

    @Autowired
    private TransferableBondInfoDAO transferableBondInfoDAO;

    @Override
    public TransferableBondInfo save(TransferableBondInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        transferableBondInfoDAO.insert(record);
        return record;
    }

    @Override
    public TransferableBondInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return transferableBondInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(TransferableBondInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return transferableBondInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<TransferableBondInfo> listByCondition(TransferableBondInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return transferableBondInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(TransferableBondInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return transferableBondInfoDAO.countByCondition(query);
    }
}