package com.bazinga.test;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.component.*;
import com.bazinga.dto.BlockCompeteDTO;
import com.bazinga.dto.OpenCompeteDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sun.text.resources.no.JavaTimeSupplementary_no;

import java.util.Map;

public class ReplayTest extends BaseTestCase {

    @Autowired
    private BlockHeadReplayComponent blockHeadReplayComponent;

    @Autowired
    private MiddlePlankReplayComponent middlePlankReplayComponent;

    @Autowired
    private MainBlockReplayComponent mainBlockReplayComponent;

    @Autowired
    private PositionOwnReplayComponent positionOwnReplayComponent;

    @Autowired
    private MarketPlankReplayComponent marketPlankReplayComponent;

    @Autowired
    private PositionBlockReplayComponent positionBlockReplayComponent;

    @Autowired
    private SellReplayComponent sellReplayComponent;

    @Autowired
    private BlockKbarReplayComponent blockKbarReplayComponent;

    @Autowired
    private BestAvgLineReplayComponent bestAvgLineReplayComponent;

    @Autowired
    private RotPlankReplayComponent rotPlankReplayComponent;

    @Autowired
    private SelfExcelReplayComponent selfExcelReplayComponent;

    @Autowired
    private BlockReplayComponent blockReplayComponent;

    @Autowired
    private Month2RateReplayComponent month2RateReplayComponent;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    @Autowired
    private Plank3to4Component plank3to4Component;

    @Autowired
    private SuddenAbsortComponent suddenAbsortComponent;

    @Autowired
    private PrePlankAbsortComponent prePlankAbsortComponent;

    @Autowired
    private HighPlankReplayComponent highPlankReplayComponent;

    @Autowired
    private MiddlePlankUpdateReplayComponent middlePlankUpdateReplayComponent;

    @Autowired
    private PlankFirstSealReplayComponent plankFirstSealReplayComponent;

    @Autowired
    private BankerStockReplayComponent bankerStockReplayComponent;

    @Autowired
    private ZongziReplayComponent zongziReplayComponent;

    @Autowired
    private ZuangReplayComponent zuangReplayComponent;

    @Autowired
    private Zz500RepalyComponent zz500RepalyComponent;

    @Autowired
    private Index500Component index500Component;
    @Test
    public void test(){
        blockHeadReplayComponent.invokeStrategy();
    }

    @Test
    public void test2(){
        middlePlankReplayComponent.invoke();
       // middlePlankReplayComponent.invokeSecond();
    }

    @Test
    public void test3(){
        mainBlockReplayComponent.invokeStrategy();
    }

    @Test
    public void test4(){
        positionOwnReplayComponent.positionOwnAdd();
    }

    @Test
    public void test5(){
        marketPlankReplayComponent.replay();
    }

    @Test
    public void test6(){
        positionBlockReplayComponent.replay();
    }

    @Test
    public void test7(){
        //sellReplayComponent.replayMarket();
        selfExcelReplayComponent.replayPosition();
       // selfExcelReplayComponent.zhuanzhai();
    }

    @Test
    public void test8(){
        blockKbarReplayComponent.replay(14);
   /*     for (int i = 24; i <= 60; i++) {
         a   blockKbarReplayComponent.replay(i);
        }*/
      //  blockKbarRaeplayComponent.anaysisBest();
    }

    @Test
    public void test9(){
        bestAvgLineReplayComponent.invokeStrategy();
    }

    @Test
    public void test10(){
       // rotPlankReplayComponent.replay(2);
       // rotPlankReplayComponent.replay(3);
        rotPlankReplayComponent.replay(4);
       // rotPlankReplayComponent.replay(5);

       /* Map<String, BlockCompeteDTO> blockRateMap = blockReplayComponent.getBlockRateMap();
        System.out.println(JSONObject.toJSONString(blockRateMap));*/
       month2RateReplayComponent.szNeeddle();
    }

    @Test
    public void test11(){
        //Map<String, OpenCompeteDTO> competeInfo = commonReplayComponent.get300CompeteInfo();
     //   plank3to4Component.replay();
     //   suddenAbsortComponent.replay();
     //   prePlankAbsortComponent.replay();
      //  middlePlankUpdateReplayComponent.invoke();
     //   highPlankReplayComponent.replay();
     //   zongziReplayComponent.replay();
     //   commonReplayComponent.replay();
     //   zuangReplayComponent.replay();
     //   zz500RepalyComponent.replay("20180101","20190120");
      //  zz500RepalyComponent.replay("20190101","20200120");
     //   zz500RepalyComponent.replay("20200101","20210120");
        zz500RepalyComponent.replay("20210101","20220120");
        zz500RepalyComponent.replay("20220101","20230120");

      // index500Component.getIndex500RateMap();

      //  plankFirstSealReplayComponent.replay();
      //  bankerStockReplayComponent.replay();

    }
}
