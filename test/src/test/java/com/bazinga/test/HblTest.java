package com.bazinga.test;


import com.bazinga.component.*;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HblTest extends BaseTestCase {

    @Autowired
    private SynExcelComponent synExcelComponent;
    @Autowired
    private OtherBuyStockComponent otherBuyStockComponent;
    @Autowired
    private RealBuyOrSellComponent realBuyOrSellComponent;
    @Autowired
    private ZhongWeiDiXiReplayComponent zhongWeiDiXiReplayComponent;
    @Autowired
    private HotBlockDropBuyComponent hotBlockDropBuyComponent;
    @Autowired
    private FastPlankComponent fastPlankComponent;
    @Autowired
    private HotBlockDropBuyScoreComponent hotBlockDropBuyScoreComponent;
    @Autowired
    private YesterdayPlankRateComponent yesterdayPlankRateComponent;
    @Autowired
    private ZhongZheng500Component zhongZheng500Component;
    @Autowired
    private ZhongZheng500TwoComponent zhongZheng500TwoComponent;
    @Test
    public void test(){
        zhongWeiDiXiReplayComponent.middle();
        //zhongWeiDiXiReplayComponent.middleRateInfo("20210903",null);
       synExcelComponent.otherStockBuy();
        synExcelComponent.ziDongHuaBuy();
        synExcelComponent.graphBuy();
        /*BlockLevelDTO preBlockLevel = otherBuyStockComponent.getPreBlockLevel("600476", "20210830");
        System.out.println(preBlockLevel);*/
    }
    @Test
    public void test2(){
        //realBuyOrSellComponent.test();
        //realBuyOrSellComponent.realBuyOrSell("",DateUtil.parseDate("2021-09-03",DateUtil.yyyy_MM_dd));
        //hotBlockDropBuyComponent.hotDrop();
        synExcelComponent.hotBlockDrop();
        //hotBlockDropBuyScoreComponent.hotDrop();
        //fastPlankComponent.fastPlank();

    }

    @Test
    public void test3(){
        //yesterdayPlankRateComponent.yesterdayPlankRate();
        //zhongZheng500Component.zz500Buy();
        zhongZheng500TwoComponent.zz500BuyTwo();

    }
}
