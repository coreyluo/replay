package com.bazinga.util;

import com.bazinga.base.PagingResult;
import com.bazinga.base.QueryFunction;
import com.bazinga.base.ShardingQuery;
import com.bazinga.base.Sort;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 〈分表查询工具类〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 18/11/29
 */
public class ShardingQueryUtils {

    public static final String ID = "id";

    /**
     * 分表遍历查询
     *
     * 1、查询必须 按Id升序
     * 2、通过PagingResult.isLastPage()判断是否最后一页
     * 注意：
     * 查询可能会有空列表返回，不代表最后一页;
     * 查询条件索引和排序条件索引不一致，有filesort可能
     *
     * @param query
     * @param tableSuffixes
     * @param queryFunction
     * @return
     */
    public static <Q extends ShardingQuery, T> PagingResult<Q, T> query(Q query, String[] tableSuffixes,
                                                                        QueryFunction<T> queryFunction){
        //1、设置分表后缀
        int tableIndex = query.getTableIndex();
        Assert.notEmpty(tableSuffixes, "分表后缀列表不能为空");
        Assert.isTrue(tableIndex >= 0 && tableIndex < tableSuffixes.length, "分表序号不正确");
        query.setTableSuffix(tableSuffixes[tableIndex]);

        //2、排序条件检查：必须只有id升序一个排序条件
        if(CollectionUtils.isEmpty(query.getSorts())){
            query.addOrderBy(ID, Sort.SortType.ASC);
        }else{
            assertOnlyIdAsc(query.getSorts());
        }

        //3、数据查询
        List<T> list = queryFunction.query();

        //4、组装返回结果
        boolean isLastPage = false;
        //a、当前页数据小于页大小，准备切换下次查询的分表
        if(list.size() < query.getLimit()){
            if( tableIndex >= (tableSuffixes.length-1) ){
                //最后一张分表，即最后一页
                isLastPage = true;
            }else{
                //设置下一次查询的分表序号
                query.setTableIndex(tableIndex+1);
                query.setLastMaxId(0L);
            }
        //b、继续当前分表的查询，设置最后一条数据的Id
        }else{
            T last = list.get(list.size() - 1);
            query.setLastMaxId(getId(last));
        }
        return new PagingResult(query, list, isLastPage);
    }

    /**
     * 分表遍历查询（只读方式,通过offset递增方式实现）
     *
     * 适用于只读查询，如数据导出
     * 即查询后不修改数据的查询参数字段
     *
     * @param query
     * @param tableSuffixes
     * @param queryFunction
     */
    public static <Q extends ShardingQuery, T> PagingResult<Q, T> queryReadOnly(Q query, String[] tableSuffixes,
                                                                                QueryFunction<T> queryFunction){
        //1、设置分表后缀
        int tableIndex = query.getTableIndex();
        Assert.notEmpty(tableSuffixes, "分表后缀列表不能为空");
        Assert.isTrue(tableIndex >= 0 && tableIndex < tableSuffixes.length, "分表序号不正确");
        query.setTableSuffix(tableSuffixes[tableIndex]);

        //2、数据查询
        List<T> list = queryFunction.query();

        //3、组装返回结果
        boolean isLastPage = false;
        //a、当前页数据小于页大小，准备切换下次查询的分表
        if(list.size() < query.getLimit()){
            if( tableIndex >= (tableSuffixes.length-1) ){
                //最后一张分表，即最后一页
                isLastPage = true;
            }else{
                //设置下一次查询的分表序号, offset归零
                query.setTableIndex(tableIndex+1);
                query.setOffset(0);
            }
        //b、继续当前分表的查询，设置offset
        }else{
            query.setOffset(query.getOffset()+query.getLimit());
        }
        return new PagingResult(query, list, isLastPage);
    }

    /**
     * 必须只有id升序一个排序条件
     * @param sorts
     */
    private static void assertOnlyIdAsc(List<Sort> sorts){
        Assert.isTrue(sorts.size() == 1, "必须只有id升序一个排序条件");
        boolean isIdAsc = sorts.get(0).getColumn().toLowerCase().equals(ID)
                && sorts.get(0).getType().getValue() == Sort.SortType.ASC.getValue();
        Assert.isTrue(isIdAsc, "必须只有id升序一个排序条件");
    }

    /**
     * 获取id
     * @param obj
     * @return
     */
    private static Long getId(Object obj){
        Method method = ReflectionUtils.findMethod(obj.getClass(), "getId");
        Assert.notNull(method, "对象不存在id字段，class=" + obj.getClass().getCanonicalName());
        Object id = ReflectionUtils.invokeMethod(method, obj);
        Assert.notNull(id, "对象id不能为空");
        if(id instanceof Long){
            return (Long)id;
        }else if(id instanceof Number){
            return ((Number)id).longValue();
        }
        throw new IllegalArgumentException("对象id不是数字类型, id="+id);
    }

}
