package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.TransferableBondInfoDAO;
import com.bazinga.replay.model.TransferableBondInfo;
import com.bazinga.replay.query.TransferableBondInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈TransferableBondInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-11-21
  */
@Repository
public class TransferableBondInfoDAOImpl extends SqlSessionDaoSupport implements TransferableBondInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.TransferableBondInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(TransferableBondInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public TransferableBondInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(TransferableBondInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<TransferableBondInfo> selectByCondition(TransferableBondInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(TransferableBondInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}