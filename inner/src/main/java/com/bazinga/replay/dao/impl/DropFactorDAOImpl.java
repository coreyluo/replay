package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.DropFactorDAO;
import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.query.DropFactorQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈DropFactor DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-10-10
  */
@Repository
public class DropFactorDAOImpl extends SqlSessionDaoSupport implements DropFactorDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.DropFactorDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(DropFactor record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public DropFactor selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(DropFactor record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<DropFactor> selectByCondition(DropFactorQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(DropFactorQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}