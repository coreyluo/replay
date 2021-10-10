package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsBlockKbarDAO;
import com.bazinga.replay.model.ThsBlockKbar;
import com.bazinga.replay.query.ThsBlockKbarQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsBlockKbar DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-09-29
  */
@Repository
public class ThsBlockKbarDAOImpl extends SqlSessionDaoSupport implements ThsBlockKbarDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsBlockKbarDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsBlockKbar record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsBlockKbar selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsBlockKbar record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsBlockKbar> selectByCondition(ThsBlockKbarQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsBlockKbarQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}