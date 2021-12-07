package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Plank3to4Component {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private StockPlankDailyService stockPlankDailyService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){
        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);

        for (int i = 0; i < tradeDatePools.size()-2; i++) {

            TradeDatePool tradeDatePool = tradeDatePools.get(i);
            TradeDatePool buyTradeDate = tradeDatePools.get(i+1);
            TradeDatePool sellTradeDate = tradeDatePools.get(i+2);
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);

            StockPlankDailyQuery dailyQuery = new StockPlankDailyQuery();
            dailyQuery.setTradeDateFrom(DateTimeUtils.getDate000000(tradeDatePool.getTradeDate()));
            dailyQuery.setTradeDateTo(DateTimeUtils.getDate000000(tradeDatePool.getTradeDate()));
            List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(dailyQuery);

            stockPlankDailies = stockPlankDailies.stream().filter(item->item.getPlankType()>0).filter(item->item.getPlankType()<=5).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(stockPlankDailies)){
                continue;
            }

            int maxPlankType = stockPlankDailies.stream().mapToInt(StockPlankDaily::getPlankType).max().getAsInt();
            if(maxPlankType ==3){
                List<StockPlankDaily> preBuyList = stockPlankDailies.stream().filter(item -> item.getPlankType() == 3).collect(Collectors.toList());

                for (StockPlankDaily stockPlankDaily : preBuyList) {
                    log.info("符合光头条件预选stockCode{} kbarDate{}",stockPlankDaily.getStockCode(),kbarDate);
                    StockKbar preStockKbar = stockKbarService.getByUniqueKey(stockPlankDaily.getUniqueKey());
                    String buyUniqueKey = stockPlankDaily.getStockCode() + SymbolConstants.UNDERLINE + DateUtil.format(buyTradeDate.getTradeDate(),DateUtil.yyyyMMdd);
                    String sellUniqueKey = stockPlankDaily.getStockCode() + SymbolConstants.UNDERLINE + DateUtil.format(sellTradeDate.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar buyStockKbar = stockKbarService.getByUniqueKey(buyUniqueKey);

                    if(!StockKbarUtil.isHighUpperPrice(buyStockKbar,preStockKbar)){
                        continue;
                    }
                    //historyTransactionDataComponent.getData(buyStockKbar.getStockCode(),)



                }
            }




        }

    }

}
