package com.bazinga.test;

import com.bazinga.base.Sort;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.replay.component.*;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
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
    @Autowired
    private BadPlankComponent badPlankComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private BlockKbarSelfComponent blockKbarSelfComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
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
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        badPlankComponent.badPlankJudge(new Date());
        stockPlankDailyComponent.superFactor(new Date());
        blockKbarComponent.thsBlockKbar(DateTimeUtils.getDate000000(new Date()));
        hotBlockDropInfoComponent.thsBlockKbar(DateTimeUtils.getDate000000(new Date()));
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
    public void test5() {
        synInfoComponent.synTbondInfo();
    }


    @Test
    public void test3() {
        //stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        //stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
       //stockPlankDailyComponent.superFactor(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
        //stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        /*List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNew("605588", 3, 50);
        System.out.println(kbars);*/
        //blockKbarComponent.thsBlockKbar(DateUtil.parseDate("20211018",DateUtil.yyyyMMdd));
        //hotBlockDropInfoComponent.thsBlockKbar(DateUtil.parseDate("20211018",DateUtil.yyyyMMdd));
        //hotBlockDropInfoComponent.getAvgPrice();
        //badPlankComponent.badPlankJudge(new Date());
        /*DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, "128094", 0,100);
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("123048", "20211102");
        System.out.println(data);*/
        //blockKbarSelfComponent.initBlockKbarSelf();
        //stockPlankDailyComponent.stockPlankDailyStatistic(new Date());
        /*TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate("20211122",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        for (TradeDatePool tradeDatePool:tradeDatePools) {
            System.out.println(tradeDatePool);
            //blockKbarComponent.thsBlockKbar(tradeDatePool.getTradeDate());
        }*/
        blockKbarComponent.thsBlockKbarGatherQuantity();

    }


}
