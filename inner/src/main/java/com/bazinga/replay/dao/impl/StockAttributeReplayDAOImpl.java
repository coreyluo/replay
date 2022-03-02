package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockAttributeReplayDAO;
import com.bazinga.replay.model.StockAttributeReplay;
import com.bazinga.replay.query.StockAttributeReplayQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockAttributeReplay DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-02-19
  */
@Repository
public class StockAttributeReplayDAOImpl extends SqlSessionDaoSupport implements StockAttributeReplayDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockAttributeReplayDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockAttributeReplay record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockAttributeReplay selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockAttributeReplay record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockAttributeReplay> selectByCondition(StockAttributeReplayQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockAttributeReplayQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public StockAttributeReplay selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockAttributeReplay record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}