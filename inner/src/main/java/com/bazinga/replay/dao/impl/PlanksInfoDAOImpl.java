package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.PlanksInfoDAO;
import com.bazinga.replay.model.PlanksInfo;
import com.bazinga.replay.query.PlanksInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈PlanksInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-12-07
  */
@Repository
public class PlanksInfoDAOImpl extends SqlSessionDaoSupport implements PlanksInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.PlanksInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(PlanksInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public PlanksInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(PlanksInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<PlanksInfo> selectByCondition(PlanksInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(PlanksInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}