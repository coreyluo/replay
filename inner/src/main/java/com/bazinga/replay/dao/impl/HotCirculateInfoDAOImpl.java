package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.HotCirculateInfoDAO;
import com.bazinga.replay.model.HotCirculateInfo;
import com.bazinga.replay.query.HotCirculateInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈HotCirculateInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-09-15
  */
@Repository
public class HotCirculateInfoDAOImpl extends SqlSessionDaoSupport implements HotCirculateInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.HotCirculateInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(HotCirculateInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public HotCirculateInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(HotCirculateInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<HotCirculateInfo> selectByCondition(HotCirculateInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(HotCirculateInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}