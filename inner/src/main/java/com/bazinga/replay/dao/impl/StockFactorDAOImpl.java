package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockFactorDAO;
import com.bazinga.replay.model.StockFactor;
import com.bazinga.replay.query.StockFactorQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockFactor DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-07-06
  */
@Repository
public class StockFactorDAOImpl extends SqlSessionDaoSupport implements StockFactorDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockFactorDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockFactor record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockFactor selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockFactor record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockFactor> selectByCondition(StockFactorQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockFactorQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public StockFactor selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockFactor record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}