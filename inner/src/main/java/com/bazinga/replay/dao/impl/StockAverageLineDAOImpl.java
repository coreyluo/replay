package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockAverageLineDAO;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.query.StockAverageLineQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockAverageLine DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-10-10
  */
@Repository
public class StockAverageLineDAOImpl extends SqlSessionDaoSupport implements StockAverageLineDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockAverageLineDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockAverageLine record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockAverageLine selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockAverageLine record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockAverageLine> selectByCondition(StockAverageLineQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockAverageLineQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public StockAverageLine selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockAverageLine record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}