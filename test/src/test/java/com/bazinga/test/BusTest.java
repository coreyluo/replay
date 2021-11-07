package com.bazinga.test;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.component.DragonTigerDailyComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BusTest extends BaseTestCase {

    @Autowired
    private DragonTigerDailyComponent dragonTigerDailyComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Test
    public void test(){
        dragonTigerDailyComponent.save2Db();
      /*  List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("113615", "20211103");
        System.out.println(JSONObject.toJSONString(data));*/
    }
}
