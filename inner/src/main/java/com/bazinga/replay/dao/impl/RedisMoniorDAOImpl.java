package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.RedisMoniorDAO;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.query.RedisMoniorQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈RedisMonior DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-03-01
  */
@Repository
public class RedisMoniorDAOImpl extends SqlSessionDaoSupport implements RedisMoniorDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.RedisMoniorDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(RedisMonior record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public RedisMonior selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(RedisMonior record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<RedisMonior> selectByCondition(RedisMoniorQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(RedisMoniorQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public RedisMonior selectByRedisKey(String redisKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByRedisKey", redisKey);
    }

    @Override
    public int updateByRedisKey(RedisMonior record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByRedisKey", record);
    }
}