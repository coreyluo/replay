package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.HotBlockDropStockDAO;
import com.bazinga.replay.model.HotBlockDropStock;
import com.bazinga.replay.query.HotBlockDropStockQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈HotBlockDropStock DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-09-29
  */
@Repository
public class HotBlockDropStockDAOImpl extends SqlSessionDaoSupport implements HotBlockDropStockDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.HotBlockDropStockDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(HotBlockDropStock record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public HotBlockDropStock selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(HotBlockDropStock record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<HotBlockDropStock> selectByCondition(HotBlockDropStockQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(HotBlockDropStockQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}