package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsQuoteInfoDAO;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.query.ThsQuoteInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsQuoteInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-11-11
  */
@Repository
public class ThsQuoteInfoDAOImpl extends SqlSessionDaoSupport implements ThsQuoteInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsQuoteInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsQuoteInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsQuoteInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsQuoteInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsQuoteInfo> selectByCondition(ThsQuoteInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsQuoteInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}