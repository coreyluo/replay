package com.bazinga.replay.dao.impl;

import com.bazinga.base.PagingResult;
import com.bazinga.util.ShardingQueryUtils;

import com.bazinga.replay.dao.StockOrderDAO;
import com.bazinga.replay.model.StockOrder;
import com.bazinga.replay.query.StockOrderQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 /**
  * 〈StockOrder DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-05-15
  */
@Repository
public class StockOrderDAOImpl extends SqlSessionDaoSupport implements StockOrderDAO {

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    /**
     * 分表后缀名数组
     * table_xxx_{tableSuffix}
     */
    private final String[] tableSuffixes = {"0", "1", "2", "3"};

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockOrderDAO";

    @Override
    public int insert(StockOrder record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    
    @Override
    public StockOrder selectByDateTrade(Date dateTrade) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dateTrade", dateTrade);
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByDateTrade", params);
    }

    @Override
    public int updateByDateTrade(StockOrder record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByDateTrade", record);
    }

    @Override
    public List<StockOrder> selectByCondition(StockOrderQuery query) {
        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockOrderQuery query) {
        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public PagingResult<StockOrderQuery, StockOrder> selectByConditionWithTable(StockOrderQuery query) {
        return ShardingQueryUtils.query(query, tableSuffixes,
                () -> getSqlSession().selectList( MAPPER_NAME + ".selectByConditionWithTable", query));
    }
}