package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BlockKbarSelfDAO;
import com.bazinga.replay.model.BlockKbarSelf;
import com.bazinga.replay.query.BlockKbarSelfQuery;
import com.bazinga.replay.service.BlockKbarSelfService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BlockKbarSelf Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
@Service
public class BlockKbarSelfServiceImpl implements BlockKbarSelfService {

    @Autowired
    private BlockKbarSelfDAO blockKbarSelfDAO;

    @Override
    public BlockKbarSelf save(BlockKbarSelf record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        blockKbarSelfDAO.insert(record);
        return record;
    }

    @Override
    public BlockKbarSelf getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return blockKbarSelfDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BlockKbarSelf record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return blockKbarSelfDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BlockKbarSelf> listByCondition(BlockKbarSelfQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockKbarSelfDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BlockKbarSelfQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockKbarSelfDAO.countByCondition(query);
    }

    @Override
    public BlockKbarSelf getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return blockKbarSelfDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(BlockKbarSelf record) {
        Assert.notNull(record, "待更新记录不能为空");
        return blockKbarSelfDAO.updateByUniqueKey(record);
    }
}