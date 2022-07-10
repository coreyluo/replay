package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.HistoryBlockStocksDAO;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.query.HistoryBlockStocksQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈HistoryBlockStocks DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-05-19
  */
@Repository
public class HistoryBlockStocksDAOImpl extends SqlSessionDaoSupport implements HistoryBlockStocksDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.HistoryBlockStocksDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(HistoryBlockStocks record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public HistoryBlockStocks selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(HistoryBlockStocks record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<HistoryBlockStocks> selectByCondition(HistoryBlockStocksQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(HistoryBlockStocksQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}