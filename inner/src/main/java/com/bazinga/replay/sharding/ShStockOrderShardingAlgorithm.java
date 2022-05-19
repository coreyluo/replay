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

        for (String tableName : tableNames) {
            if(tableName.endsWith(DateUtil.format(preciseShardingValue.getValue(),DateUtil.MMdd))){
                return tableName;
            }
        }
        return null;
    }
}
