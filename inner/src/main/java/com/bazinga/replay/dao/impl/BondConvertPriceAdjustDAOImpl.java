package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BondConvertPriceAdjustDAO;
import com.bazinga.replay.model.BondConvertPriceAdjust;
import com.bazinga.replay.query.BondConvertPriceAdjustQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BondConvertPriceAdjust DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-01-04
  */
@Repository
public class BondConvertPriceAdjustDAOImpl extends SqlSessionDaoSupport implements BondConvertPriceAdjustDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BondConvertPriceAdjustDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BondConvertPriceAdjust record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BondConvertPriceAdjust selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BondConvertPriceAdjust record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BondConvertPriceAdjust> selectByCondition(BondConvertPriceAdjustQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BondConvertPriceAdjustQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}