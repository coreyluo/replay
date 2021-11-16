package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.DragonTigerDailyDAO;
import com.bazinga.replay.model.DragonTigerDaily;
import com.bazinga.replay.query.DragonTigerDailyQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈DragonTigerDaily DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-11-04
  */
@Repository
public class DragonTigerDailyDAOImpl extends SqlSessionDaoSupport implements DragonTigerDailyDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.DragonTigerDailyDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(DragonTigerDaily record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public DragonTigerDaily selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(DragonTigerDaily record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<DragonTigerDaily> selectByCondition(DragonTigerDailyQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(DragonTigerDailyQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}