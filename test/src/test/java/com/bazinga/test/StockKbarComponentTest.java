package com.bazinga.test;

import com.bazinga.replay.component.StockBollingComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockCommonReplayComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

public class StockKbarComponentTest extends BaseTestCase {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;

    @Autowired
    private StockBollingComponent stockBollingComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Test
    public void test(){

        stockKbarComponent.batchUpdateDaily();
        //stockCommonReplayComponent.saveCommonReplay(new Date());
    }

    @Test
    public void test2(){
        stockKbarComponent.batchcalAvgLine();
    }

    @Test
    public void test3(){
        stockBollingComponent.batchInitBoll();
    }

    @Test
    public void test4(){

        stockBollingComponent.initBoll("000537");

    }
}
