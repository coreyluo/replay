package com.bazinga.test;

import com.bazinga.replay.component.*;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

/**
 * 单元测试基类<p/>
 * 其他单元测试都继承本类
 *
 * @author zixiao
 * @date 2016/5/20
 */
@ContextConfiguration(locations="classpath:/META-INF/spring/replay-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseTestCase {
    @Autowired
    private StockPlankDailyComponent stockPlankDailyComponent;
    @Autowired
    private NewStockComponent newStockComponent;
    @Autowired
    private SynInfoComponent synInfoComponent;
    @Autowired
    private PlankExchangeDailyComponent plankExchangeDailyComponent;
    @Autowired
    private StockReplayDailyComponent stockReplayDailyComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;
    @Autowired
    private PlankChenJiaoEComponent plankChenJiaoEComponent;
    @Autowired
    private BlockKbarComponent blockKbarComponent;
    @Autowired
    private HotBlockDropInfoComponent hotBlockDropInfoComponent;
    @Test
    public void test1() {
        //无敌数据
        plankChenJiaoEComponent.exportData();
        stockReplayDailyComponent.stockReplayDaily(new Date());
        stockReplayDailyComponent.calPreDateAvgPrice(new Date());
        stockPlankDailyComponent.stockPlankDailyStatistic(new Date());
        newStockComponent.catchNewStock();
        plankExchangeDailyComponent.plankExchangeDaily(new Date());
        stockKbarComponent.batchUpdateDaily();
        stockPlankDailyComponent.calMax100DaysPriceForTwoPlank(new Date());
        stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(new Date());
        //stockPlankDaily添加次新标签
        stockPlankDailyComponent.calSubNewStock(new Date());
        //stockPlankDaily添加insertTime
        stockPlankDailyComponent.insertTime(new Date());

        //新版复盘
        stockCommonReplayComponent.saveCommonReplay(new Date());
        stockCommonReplayComponent.firstPlankNoBuyInfo(new Date());
        stockCommonReplayComponent.highRaiseStockInfo(new Date());
        stockCommonReplayComponent.forTwoPlankWuDi(new Date());
        stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        stockPlankDailyComponent.superFactor(new Date());
    }

    @Test
    public void test2() {
        synInfoComponent.synCirculateInfo();
    }

    @Test
    public void test4() {
        synInfoComponent.synHotCirculateInfo();
    }


    @Test
    public void test3() {
        //stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
       //stockPlankDailyComponent.superFactor(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
        //stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        /*List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNew("605588", 3, 50);
        System.out.println(kbars);*/
        //blockKbarComponent.thsBlockKbar(DateUtil.parseDate("20211008",DateUtil.yyyyMMdd));
        hotBlockDropInfoComponent.thsBlockKbar(DateUtil.parseDate("20211008",DateUtil.yyyyMMdd));
    }


}
