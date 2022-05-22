package com.bazinga.test;

import com.bazinga.replay.component.StockBollingComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockCommonReplayComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.util.DateUtil;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",500);
       // stockKbarComponent.initAndSaveIndexGlobal();
    }

    @Test
    public void test2(){
      //  stockKbarComponent.calCurrentDayAvgLine(new Date());
       /* try {
            TimeUnit.HOURS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
       // stockBollingComponent.calCurrentDayBoll(new Date());
        stockKbarComponent.initSpecialStockAndSaveMin5KbarData("999999","上证指数",500);

        // stockKbarComponent.calCurrentDayAvgLine(new Date());
        //stockBollingComponent.batchInitBoll();
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
      /*  Double avgPrice = stockKbarComponent.calDaysAvg("600860", "20211230", 20);
        System.out.println(avgPrice);*/
        stockBollingComponent.initBoll("600519");

    }
}
