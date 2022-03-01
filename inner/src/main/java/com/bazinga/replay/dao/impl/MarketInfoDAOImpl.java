package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.MarketInfoDAO;
import com.bazinga.replay.model.MarketInfo;
import com.bazinga.replay.query.MarketInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈MarketInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-02-19
  */
@Repository
public class MarketInfoDAOImpl extends SqlSessionDaoSupport implements MarketInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.MarketInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(MarketInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public MarketInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(MarketInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<MarketInfo> selectByCondition(MarketInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(MarketInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}