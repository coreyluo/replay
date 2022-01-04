package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BondDailyConvertDAO;
import com.bazinga.replay.model.BondDailyConvert;
import com.bazinga.replay.query.BondDailyConvertQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BondDailyConvert DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-01-04
  */
@Repository
public class BondDailyConvertDAOImpl extends SqlSessionDaoSupport implements BondDailyConvertDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BondDailyConvertDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BondDailyConvert record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BondDailyConvert selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BondDailyConvert record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BondDailyConvert> selectByCondition(BondDailyConvertQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BondDailyConvertQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}