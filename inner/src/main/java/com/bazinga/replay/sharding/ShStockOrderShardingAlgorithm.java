package com.bazinga.replay.sharding;

import com.bazinga.util.DateUtil;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.stereotype.Service;


import java.util.Collection;
import java.util.Date;


@Service
public class ShStockOrderShardingAlgorithm implements PreciseShardingAlgorithm<Date> {


    @Override
    public String doSharding(Collection<String> tableNames, PreciseShardingValue<Date> preciseShardingValue) {
        String day = DateUtil.format(preciseShardingValue.getValue(), DateUtil.MMdd);
        String year = DateUtil.format(preciseShardingValue.getValue(), "yyyy");
        String dateStr = year+"_"+day;
        for (String tableName : tableNames) {
            if(tableName.endsWith(dateStr)){
                return tableName;
            }
        }
        return null;
    }
}
