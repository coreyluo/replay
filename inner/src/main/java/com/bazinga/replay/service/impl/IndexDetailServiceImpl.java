package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.IndexDetailDAO;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.query.IndexDetailQuery;
import com.bazinga.replay.service.IndexDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈IndexDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-03
 */
@Service
public class IndexDetailServiceImpl implements IndexDetailService {

    @Autowired
    private IndexDetailDAO indexDetailDAO;

    @Override
    public IndexDetail save(IndexDetail record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        indexDetailDAO.insert(record);
        return record;
    }

    @Override
    public IndexDetail getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return indexDetailDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(IndexDetail record) {
        Assert.notNull(record, "待更新记录不能为空");
        return indexDetailDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<IndexDetail> listByCondition(IndexDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return indexDetailDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(IndexDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return indexDetailDAO.countByCondition(query);
    }
}