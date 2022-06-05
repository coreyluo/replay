package com.bazinga.test;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.component.*;
import com.bazinga.dto.BlockCompeteDTO;
import com.bazinga.dto.OpenCompeteDTO;
import com.bazinga.util.ThreadPoolUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sun.text.resources.no.JavaTimeSupplementary_no;

import java.sql.Time;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ReplayTest extends BaseTestCase {

    private ExecutorService THREAD_POOL = ThreadPoolUtils.create(4,8,12,"replay");

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

    @Autowired
    private LowTrendReplayComponent lowTrendReplayComponent;

    @Autowired
    private BackSealReplayComponent backSealReplayComponent;

    @Autowired
    private PlankQuantityDivideComponent plankQuantityDivideComponent;

    @Autowired
    private ManyCannonReplayComponent manyCannonReplayComponent;

    @Autowired
    private BollingReplayComponent bollingReplayComponent;

    @Autowired
    private BlockOpenReplayComponent blockOpenReplayComponent;

    @Autowired
    private StockReplayByGroupComponent stockReplayByGroupComponent;

    @Autowired
    private One2TwoReplayComponent one2TwoReplayComponent;

    @Autowired
    private BlockIndustryReplayComponent blockIndustryReplayComponent;

    @Autowired
    private IndexKbarComponent indexKbarComponent;

    @Autowired
    private ShIndexReplayComponent shIndexReplayComponent;


    @Test
    public void test(){
        //blockHeadReplayComponent.invokeStrategy();
       // sellReplayComponent.replay300();
       // indexKbarComponent.replay("999999");
       // zuangReplayComponent.replayLowAmount();
        sellReplayComponent.addProperty();
    }

    @Test
    public void test2(){
        middlePlankReplayComponent.invoke();
       // middlePlankReplayComponent.prePlankGroupBy();
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
      //  stockReplayByGroupComponent.replayDayPlank();
       // middlePlankReplayComponent.invokejiaQuan();
        one2TwoReplayComponent.replay();

    }

    @Test
    public void test6(){
        positionBlockReplayComponent.replay();
    }

    @Test
    public void test7(){
        //sellReplayComponent.replayMarket();
       // selfExcelReplayComponent.replayPosition();
       // commonReplayComponent.replay();
     /*    THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220301","20220413");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220201","20220301");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220101","20220201");
        });*/

        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220101","20220201");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220201","20220301");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20220301","20220413");
        });
       /* THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20211001","20220101");
        });*/
       /* THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20190101","20190401");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20190401","20190701");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20190701","20191001");
        });
        THREAD_POOL.execute(()->{
            stockReplayByGroupComponent.replay("20191001","20200101");
        });*/
        try {
            TimeUnit.HOURS.sleep(12);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
       // rotPlankReplayComponent.replay(4);
       // rotPlankReplayComponent.replay(5);

       /* Map<String, BlockCompeteDTO> blockRateMap = blockReplayComponent.getBlockRateMap();
        System.out.println(JSONObject.toJSONString(blockRateMap));*/
      // month2RateReplayComponent.szNeeddle();
        THREAD_POOL.execute(()->{
            blockOpenReplayComponent.replay("20200101","20200401");
        });
        THREAD_POOL.execute(()->{
            blockOpenReplayComponent.replay("20200401","20200701");
        });
        THREAD_POOL.execute(()->{
            blockOpenReplayComponent.replay("20200701","20201001");
        });
        THREAD_POOL.execute(()->{
            blockOpenReplayComponent.replay("20201001","20210101");
        });
   /*     THREAD_POOL.execute(()->{
            blockOpenReplayComponent.replay("20201001","20210101");
        });*/

        try {
            TimeUnit.HOURS.sleep(12);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //  blockOpenReplayComponent.getOpenAmountRank("20220301","20220413");
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
      /*  for (int i = 21; i < 22; i++) {
            final String from = "" + i;
            int toInt = i+1;
            final String to = "" + toInt;
            for (int j = 8; j < 12; j++) {
                final String jString = j<10 ? "0"+j:""+j;
                final String jAdd1String = j+1<10 ? "0"+(j+1):""+(j+1);
                THREAD_POOL.execute(()->{
                    zz500RepalyComponent.replay("20"+from+ jString + "01","20"+from+ jAdd1String+ "01");
                });
            }
        }*/
          zz500RepalyComponent.replay("20210101","20210201");
          zz500RepalyComponent.replay("20211201","20220101");

     /*   try {
            TimeUnit.HOURS.sleep(24);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
//        zz500RepalyComponent.replay("20220101","20230120");
    //    lowTrendReplayComponent.replay("20171001","20230120");

      // index500Component.getIndex500RateMap();

      //  plankFirstSealReplayComponent.replay();
      //  bankerStockReplayComponent.replay();

    }

    @Test
    public void test12(){
        //backSealReplayComponent.replay();
      //  plankQuantityDivideComponent.replay();

       // zuangReplayComponent.replay20220320();
      //  manyCannonReplayComponent.replay();
        try {
            bollingReplayComponent.replay2();
        } catch (Exception e) {
            e.printStackTrace();
        }
      /*  THREAD_POOL.execute(()->{
            blockIndustryReplayComponent.replay("20180101","20181230");
        });*/
      /*  THREAD_POOL.execute(()->{
            blockIndustryReplayComponent.replay("20190101","20191230");
        });
        THREAD_POOL.execute(()->{
            blockIndustryReplayComponent.replay("20200101","20201230");
        });
        THREAD_POOL.execute(()->{
            blockIndustryReplayComponent.replay("20210101","20211230");
        });*/

        /*    try {
            TimeUnit.HOURS.sleep(24);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

      // blockIndustryReplayComponent.printShRateRelaticeFixTime();

    }

    @Test
    public void test13(){
        shIndexReplayComponent.replayUpDown();
    }
}
