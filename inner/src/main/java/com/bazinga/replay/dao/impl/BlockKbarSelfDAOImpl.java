package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BlockKbarSelfDAO;
import com.bazinga.replay.model.BlockKbarSelf;
import com.bazinga.replay.query.BlockKbarSelfQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BlockKbarSelf DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-11-04
  */
@Repository
public class BlockKbarSelfDAOImpl extends SqlSessionDaoSupport implements BlockKbarSelfDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BlockKbarSelfDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BlockKbarSelf record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BlockKbarSelf selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BlockKbarSelf record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BlockKbarSelf> selectByCondition(BlockKbarSelfQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BlockKbarSelfQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public BlockKbarSelf selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(BlockKbarSelf record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}