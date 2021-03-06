package com.bazinga.test;

import com.bazinga.replay.component.StockBollingComponent;
import com.bazinga.replay.component.StockCommonReplayComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StockKbarComponentTest extends BaseTestCase {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;

    @Autowired
    private StockBollingComponent stockBollingComponent;

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
        //stockBollingComponent.batchInitBoll();
      //  stockKbarComponent.calCurrentDayAvgLine(new Date());
       /* try {
            TimeUnit.HOURS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        stockBollingComponent.calCurrentDayBoll(new Date());
    }

    @Test
    public void test4(){

        stockBollingComponent.initBoll("000537");

    }
}
