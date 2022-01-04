package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BondBasicInfoDAO;
import com.bazinga.replay.model.BondBasicInfo;
import com.bazinga.replay.query.BondBasicInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BondBasicInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-01-04
  */
@Repository
public class BondBasicInfoDAOImpl extends SqlSessionDaoSupport implements BondBasicInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BondBasicInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BondBasicInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BondBasicInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BondBasicInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BondBasicInfo> selectByCondition(BondBasicInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BondBasicInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}