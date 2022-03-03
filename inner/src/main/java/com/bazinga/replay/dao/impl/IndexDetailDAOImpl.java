package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.IndexDetailDAO;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.query.IndexDetailQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈IndexDetail DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-03-03
  */
@Repository
public class IndexDetailDAOImpl extends SqlSessionDaoSupport implements IndexDetailDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.IndexDetailDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(IndexDetail record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public IndexDetail selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(IndexDetail record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<IndexDetail> selectByCondition(IndexDetailQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(IndexDetailQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}