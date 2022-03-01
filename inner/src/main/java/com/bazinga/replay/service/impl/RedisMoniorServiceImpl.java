package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.RedisMoniorDAO;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.query.RedisMoniorQuery;
import com.bazinga.replay.service.RedisMoniorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈RedisMonior Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-03-01
 */
@Service
public class RedisMoniorServiceImpl implements RedisMoniorService {

    @Autowired
    private RedisMoniorDAO redisMoniorDAO;

    @Override
    public RedisMonior save(RedisMonior record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        redisMoniorDAO.insert(record);
        return record;
    }

    @Override
    public RedisMonior getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return redisMoniorDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(RedisMonior record) {
        Assert.notNull(record, "待更新记录不能为空");
        return redisMoniorDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<RedisMonior> listByCondition(RedisMoniorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return redisMoniorDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(RedisMoniorQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return redisMoniorDAO.countByCondition(query);
    }

    @Override
    public RedisMonior getByKey(String key) {
        Assert.notNull(key, "唯一键不能为空");
        return redisMoniorDAO.selectByKey(key);
    }

    @Override
    public int updateByKey(RedisMonior record) {
        Assert.notNull(record, "待更新记录不能为空");
        return redisMoniorDAO.updateByKey(record);
    }
}