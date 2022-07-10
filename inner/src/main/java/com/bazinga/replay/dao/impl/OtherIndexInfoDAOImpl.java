package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.OtherIndexInfoDAO;
import com.bazinga.replay.model.OtherIndexInfo;
import com.bazinga.replay.query.OtherIndexInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈OtherIndexInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-06-05
  */
@Repository
public class OtherIndexInfoDAOImpl extends SqlSessionDaoSupport implements OtherIndexInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.OtherIndexInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(OtherIndexInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public OtherIndexInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(OtherIndexInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<OtherIndexInfo> selectByCondition(OtherIndexInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(OtherIndexInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}