package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsBlockIndustryDetailDAO;
import com.bazinga.replay.model.ThsBlockIndustryDetail;
import com.bazinga.replay.query.ThsBlockIndustryDetailQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsBlockIndustryDetail DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-05-15
  */
@Repository
public class ThsBlockIndustryDetailDAOImpl extends SqlSessionDaoSupport implements ThsBlockIndustryDetailDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsBlockIndustryDetailDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsBlockIndustryDetail record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsBlockIndustryDetail selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsBlockIndustryDetail record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsBlockIndustryDetail> selectByCondition(ThsBlockIndustryDetailQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsBlockIndustryDetailQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}