package com.bazinga.test;

import com.influx.InfluxDBConnection;
import org.influxdb.dto.QueryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfluxDBTest {
    public static void main(String[] args) {
        InfluxDBConnection influxDBConnection = new InfluxDBConnection("gank", "uqptVC9LHyhdgkE", "http://47.106.98.39:8086", "history_transaction_20180508", "hour");
        QueryResult results = influxDBConnection
                .query("SELECT * FROM sh_600007  limit 10");
        //results.getResults()是同时查询多条SQL语句的返回值，此处我们只有一条SQL，所以只取第一个结果集即可。
        QueryResult.Result oneResult = results.getResults().get(0);
        if (oneResult.getSeries() != null) {
            List<List<Object>> valueList = oneResult.getSeries().stream().map(QueryResult.Series::getValues)
                    .collect(Collectors.toList()).get(0);
            if (valueList != null && valueList.size() > 0) {
                for (List<Object> value : valueList) {
                    Map<String, String> map = new HashMap<String, String>();
                    // 数据库中字段1取值 时间 2018-05-08T09:35:00Z
                    String field1 = value.get(0) == null ? null : value.get(0).toString();
                    // 数据库中字段2取值  买卖方向 0，1，2
                    String field2 = value.get(1) == null ? null : value.get(1).toString();
                    // 数据库中字段2取值  价格 14.46
                    String field3 = value.get(1) == null ? null : value.get(2).toString();

                    // TODO 用取出的字段做你自己的业务逻辑……
                    System.out.println(field1);
                    System.out.println(field2);
                    System.out.println(field3);
                }
            }
        }
    }
}
