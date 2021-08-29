package com.bazinga.test;

import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockCommonReplayComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class StockKbarComponentTest extends BaseTestCase {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Test
    public void test(){

        stockKbarComponent.batchKbarDataInit();
       // stockCommonReplayComponent.saveCommonReplay(new Date());
    }

    @Test
    public void test2(){
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("300827", "20210827");
        System.out.println(data);
    }
}
