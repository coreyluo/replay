package com.bazinga.replay.dao.impl;

import com.bazinga.base.PagingResult;
import com.bazinga.replay.dao.ShStockOrderDAO;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.query.ShStockOrderQuery;

import com.bazinga.util.ShardingQueryUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;

import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 /**
  * 〈ShStockOrder DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-04-26
  */
@Repository
public class ShStockOrderDAOImpl extends SqlSessionDaoSupport implements ShStockOrderDAO {

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

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ShStockOrderDAO";

    @Override
    public int insert(ShStockOrder record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    
    @Override
    public List<ShStockOrder> selectByDateTrade(Date dateTrade) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dateTrade", dateTrade);
        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByDateTrade", params);
    }

    @Override
    public int updateByDateTrade(ShStockOrder record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByDateTrade", record);
    }

    @Override
    public List<ShStockOrder> selectByCondition(ShStockOrderQuery query) {
       // Assert.isTrue(query.checkIndex, "请至少使用一个索引条件查询");
        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ShStockOrderQuery query) {
        Assert.isTrue(query.checkIndex, "请至少使用一个索引条件查询");
        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public PagingResult<ShStockOrderQuery, ShStockOrder> selectByConditionWithTable(ShStockOrderQuery query) {
       // Assert.isTrue(query.checkIndex, "请至少使用一个索引条件查询");
        return ShardingQueryUtils.query(query, tableSuffixes,
                () -> getSqlSession().selectList( MAPPER_NAME + ".selectByConditionWithTable", query));
    }
}