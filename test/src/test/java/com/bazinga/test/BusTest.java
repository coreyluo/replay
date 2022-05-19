package com.bazinga.test;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.PagingResult;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.DragonTigerDailyComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ShStockOrderQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.ShStockOrderService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class BusTest extends BaseTestCase {

    @Autowired
    private DragonTigerDailyComponent dragonTigerDailyComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private ShStockOrderService shStockOrderService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Test
    public void test(){
       // stockKbarComponent.batchUpdateDaily();

      //  dragonTigerDailyComponent.save2Db();


       ShStockOrderQuery query= new ShStockOrderQuery();
        query.setDateTrade(DateUtil.parseDate("20220511",DateUtil.yyyyMMdd));
       // query.setThscode("");
        List<ShStockOrder> shStockOrders = shStockOrderService.listByCondition(query);
        System.out.println(JSONObject.toJSONString(shStockOrders));


        /*
        PagingResult<ShStockOrderQuery, ShStockOrder> pagingResult= shStockOrderService.listWithTable(query);
        JSONObject.toJSONString(pagingResult);*/
      /*  List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("113615", "20211103");
        System.out.println(JSONObject.toJSONString(data));*/

     /*   List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(new TradeDatePoolQuery());

        String resultString = "";

        for (TradeDatePool tradeDatePool : tradeDatePools) {

            resultString = resultString + "sh_stock_order_" + DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM) +SymbolConstants.COMMA;


        }*/

    }
}
