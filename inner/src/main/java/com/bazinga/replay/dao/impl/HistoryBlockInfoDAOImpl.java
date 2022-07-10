package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.HistoryBlockInfoDAO;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.query.HistoryBlockInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈HistoryBlockInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-05-19
  */
@Repository
public class HistoryBlockInfoDAOImpl extends SqlSessionDaoSupport implements HistoryBlockInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.HistoryBlockInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(HistoryBlockInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public HistoryBlockInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }
     @Override
     public void deleteByPrimaryKey(Long id) {
         this.getSqlSession().delete(MAPPER_NAME + ".deleteByPrimaryKey", id);
     }
    @Override
    public int updateByPrimaryKeySelective(HistoryBlockInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<HistoryBlockInfo> selectByCondition(HistoryBlockInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(HistoryBlockInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}