package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockCommonReplayDAO;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.StockCommonReplayQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockCommonReplay DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-08-12
  */
@Repository
public class StockCommonReplayDAOImpl extends SqlSessionDaoSupport implements StockCommonReplayDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockCommonReplayDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockCommonReplay record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockCommonReplay selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockCommonReplay record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockCommonReplay> selectByCondition(StockCommonReplayQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockCommonReplayQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public StockCommonReplay selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockCommonReplay record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}