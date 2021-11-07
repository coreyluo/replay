package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.DragonTigerDailyDAO;
import com.bazinga.replay.model.DragonTigerDaily;
import com.bazinga.replay.query.DragonTigerDailyQuery;
import com.bazinga.replay.service.DragonTigerDailyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈DragonTigerDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-11-04
 */
@Service
public class DragonTigerDailyServiceImpl implements DragonTigerDailyService {

    @Autowired
    private DragonTigerDailyDAO dragonTigerDailyDAO;

    @Override
    public DragonTigerDaily save(DragonTigerDaily record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        dragonTigerDailyDAO.insert(record);
        return record;
    }

    @Override
    public DragonTigerDaily getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return dragonTigerDailyDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(DragonTigerDaily record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return dragonTigerDailyDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<DragonTigerDaily> listByCondition(DragonTigerDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return dragonTigerDailyDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(DragonTigerDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return dragonTigerDailyDAO.countByCondition(query);
    }
}