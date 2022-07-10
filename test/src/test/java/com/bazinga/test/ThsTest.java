package com.bazinga.test;

import com.bazinga.base.Sort;
import com.bazinga.component.*;
import com.bazinga.replay.component.*;
import com.bazinga.replay.dto.BigExchangeTestBuyDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
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
public class ThsTest {
    @Autowired
    private BigAmountTestComponent bigAmountTestComponent;
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;
    @Autowired
    private MacdCalComponent macdCalComponent;
    @Autowired
    private IndexDValueComponent indexDValueComponent;
    @Autowired
    private NorthSouthMoneyComponent northSouthMoneyComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private BlockFollowComponent blockFollowComponent;
    @Autowired
    private ThsBlockKbarComponent thsBlockKbarComponent;
    @Autowired
    private QuanShangHighComponent quanShangHighComponent;
    @Autowired
    private HangYeLevelComponent hangYeLevelComponent;
    @Autowired
    private BlockFollowStaticComponent blockFollowStaticComponent;


    @Test
    public void test1() {
        blockFollowStaticComponent.blockFollowStaticInfo();
        //hangYeLevelComponent.calHangYeKbarToRedis();
        hangYeLevelComponent.hangyeAmount();
        //hangYeLevelComponent.calHangYeKbarToRedis();
        //quanShangHighComponent.quanShangYiDong();
       // thsBlockKbarComponent.initHistoryBlockMinKbar();
        //blockFollowComponent.relativeWithSZInfo();
        //blockFollowComponent.searchTest();
        //blockFollowComponent.getHistoryBlockInfoTest();
        //thsBlockKbarComponent.initHistoryBlockKbars();
        //thsBlockKbarComponent.initHistoryBlockIndex();
        /*BigExchangeTestBuyDTO buyDTO = new BigExchangeTestBuyDTO();
        buyDTO.setTradeDate("20220425");
        bigAmountTestComponent.getStockOrder("601975","20220425",buyDTO);*/
        //bigAmountTestComponent.getStockOrder("000548","20220511");
        //bigAmountTestComponent.plankExchangeAmountInfo();
        //bigAmountTestComponent.getChartStr();
        //thsStockIndexComponent.blockMACDIndex();
        //macdCalComponent.quoteToKbar();
        //macdCalComponent.calMacd();
        //macdCalComponent.macdExcel();
        /*indexDValueComponent.calIndexDValue();*/
        //indexDValueComponent.dvalueExcel();
        //northSouthMoneyComponent.northMoney("","北向资金");
       // indexDValueComponent.dvalueExcel();
        //northSouthMoneyComponent.northMoney("","北向资金");
        //northSouthMoneyComponent.indexPercent("999999","上证指数成交额比例");
        //northSouthMoneyComponent.calHenShenIndex();
        //northSouthMoneyComponent.calHenShenStockKbar10();
        /*List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("999999", "20220602");
        System.out.println(data);*/
    }


}
