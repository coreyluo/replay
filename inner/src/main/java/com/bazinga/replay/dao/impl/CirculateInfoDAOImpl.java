package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.CirculateInfoDAO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.query.CirculateInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈CirculateInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-05-10
  */
@Repository
public class CirculateInfoDAOImpl extends SqlSessionDaoSupport implements CirculateInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.CirculateInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(CirculateInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public CirculateInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }
     @Override
     public void deleteByPrimaryKey(Long id) {
         this.getSqlSession().delete( MAPPER_NAME + ".deleteByPrimaryKey", id);
     }

    @Override
    public int updateByPrimaryKeySelective(CirculateInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<CirculateInfo> selectByCondition(CirculateInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(CirculateInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}
