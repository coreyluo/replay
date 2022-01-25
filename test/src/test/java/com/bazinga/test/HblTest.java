package com.bazinga.test;


import com.bazinga.component.*;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.util.DateUtil;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.AbstractCollection;
import java.util.List;

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
    @Autowired
    private ChoaDieComponent choaDieComponent;
    @Autowired
    private FastRaiseComponent fastRaiseComponent;
    @Autowired
    private FastRateBankComponent fastRateBankComponent;
    @Autowired
    private LowExchangePercentComponent lowExchangePercentComponent;
    @Autowired
    private BlockChoaDieComponent blockChoaDieComponent;
    @Autowired
    private OneMinutePlankComponent oneMinutePlankComponent;
    @Autowired
    private BadChungYePlankInfoComponent badChungYePlankInfoComponent;
    @Autowired
    private HighExchangeChungYePlankInfoComponent highExchangeChungYePlankInfoComponent;
    @Autowired
    private ChungYePlankReturnInfoComponent chungYePlankReturnInfoComponent;
    @Autowired
    private ChungYePlankFirstInfoComponent chungYePlankFirstInfoComponent;
    @Autowired
    private ThsDataUtilComponent thsDataUtilComponent;
    @Autowired
    private BlockDropOpenHighComponent blockDropOpenHighComponent;
    @Autowired
    private BlockDropNextOpenHighComponent blockDropNextOpenHighComponent;
    @Autowired
    private RaiseDropComponent raiseDropComponent;
    @Autowired
    private ThsZhuanZaiChenWeiComponent thsZhuanZaiChenWeiComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private TwoToThreePlankInfoComponent twoToThreePlankInfoComponent;
    @Autowired
    private BlockHighProfitInfoComponent blockHighProfitInfoComponent;
    @Autowired
    private EastTherProvinceComponent eastTherProvinceComponent;
    @Autowired
    private BigBuyComponent bigBuyComponent;
    @Autowired
    private HighPlankBuyInfoComponent highPlankBuyInfoComponent;
    @Autowired
    private BlockControlInfoComponent blockControlInfoComponent;
    @Autowired
    private FeiDaoComponent feiDaoComponent;
    @Autowired
    private BadPeopleComponent badPeopleComponent;
    @Autowired
    private BlockHighBuyComponent blockHighBuyComponent;
    @Test
    public void test(){
        //zhongWeiDiXiReplayComponent.middle();
        //zhongWeiDiXiReplayComponent.middleRateInfo("20210903",null);
       /*synExcelComponent.otherStockBuy();
        synExcelComponent.ziDongHuaBuy();
        synExcelComponent.graphBuy();*/
        /*BlockLevelDTO preBlockLevel = otherBuyStockComponent.getPreBlockLevel("600476", "20210830");
        System.out.println(preBlockLevel);*/
        //synExcelComponent.zhuanZaiBuy();
        //blockChoaDieComponent.chaoDie();

        //twoToThreePlankInfoComponent.badPlankInfo();
        //twoToThreePlankInfoComponent.badPlankInfo();

        //blockHighProfitInfoComponent.badPlankInfo();
        //eastTherProvinceComponent.dongBeiInfo();
        //bigBuyComponent.dongBeiInfo();
        //highPlankBuyInfoComponent.badPlankInfo();
        //blockControlInfoComponent.badPlankInfo();
        //feiDaoComponent.jieFeiDaoInfo();
        //zhongZheng500Component.zz500Buy();
        // badPeopleComponent.jieFeiDaoInfo();
        blockHighBuyComponent.jieFeiDaoInfo();


    }
    @Test
    public void test2(){
        //realBuyOrSellComponent.test();
        //realBuyOrSellComponent.realBuyOrSell("",DateUtil.parseDate("2021-09-03",DateUtil.yyyy_MM_dd));
        //hotBlockDropBuyComponent.hotDrop();
        //synExcelComponent.hotBlockDrop();
        //hotBlockDropBuyScoreComponent.hotDrop();
        fastPlankComponent.fastPlank();
        //synExcelComponent.zhuanZaiQuoteInfo();
        //thsDataUtilComponent.quoteInfo("127017","万青转债","2020-07-01");
        synExcelComponent.zhuanZaiQuoteInfo();

    }

    @Test
    public void test3(){
        //yesterdayPlankRateComponent.yesterdayPlankRate();
        //zhongZheng500Component.zz500Buy();
        /*zhongZheng500TwoComponent.zz500BuyTwo();
        choaDieComponent.chaoDie();
        fastRaiseComponent.fastRaise();*/
        //fastRateBankComponent.fastRaiseBanker();
        //lowExchangePercentComponent.lowExchangeAvg();
        //oneMinutePlankComponent.firstMinutePlankInfo();
        //badChungYePlankInfoComponent.badPlankInfo();
        highExchangeChungYePlankInfoComponent.badPlankInfo();
        /*chungYePlankReturnInfoComponent.chuangYePlankTwo();
        chungYePlankFirstInfoComponent.chuangYePlankFirst();*/
       /* synExcelComponent.zhuanZaiBugInfo();*/
        //synExcelComponent.zhuanZaiChenWeiInfo();
        //blockDropOpenHighComponent.chaoDie();
        //blockDropNextOpenHighComponent.chaoDie();
        //raiseDropComponent.raiseDrop();
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("123116", "20211220");



    }
}
