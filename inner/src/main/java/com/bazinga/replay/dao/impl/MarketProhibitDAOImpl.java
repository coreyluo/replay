package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.MarketProhibitDAO;
import com.bazinga.replay.model.MarketProhibit;
import com.bazinga.replay.query.MarketProhibitQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈MarketProhibit DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-06-21
  */
@Repository
public class MarketProhibitDAOImpl extends SqlSessionDaoSupport implements MarketProhibitDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.MarketProhibitDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(MarketProhibit record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public MarketProhibit selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(MarketProhibit record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<MarketProhibit> selectByCondition(MarketProhibitQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(MarketProhibitQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}