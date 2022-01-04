package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BondConbondDailyConvertDAO;
import com.bazinga.replay.model.BondConbondDailyConvert;
import com.bazinga.replay.query.BondConbondDailyConvertQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BondConbondDailyConvert DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-01-04
  */
@Repository
public class BondConbondDailyConvertDAOImpl extends SqlSessionDaoSupport implements BondConbondDailyConvertDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BondConbondDailyConvertDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BondConbondDailyConvert record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BondConbondDailyConvert selectByPrimaryKey(Long  id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BondConbondDailyConvert record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BondConbondDailyConvert> selectByCondition(BondConbondDailyConvertQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BondConbondDailyConvertQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}