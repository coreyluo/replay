package com.bazinga.test;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.component.DragonTigerDailyComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BusTest extends BaseTestCase {

    @Autowired
    private DragonTigerDailyComponent dragonTigerDailyComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Test
    public void test(){
       // stockKbarComponent.batchUpdateDaily();

        dragonTigerDailyComponent.save2Db();
      /*  List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("113615", "20211103");
        System.out.println(JSONObject.toJSONString(data));*/
    }
}
