package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommonReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CirculateInfoService circulateInfoService;
    public Map<String, BigDecimal> get300CompeteInfo(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->item.getStockCode().startsWith("3")).collect(Collectors.toList());


        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (CirculateInfo circulateInfo : circulateInfos) {

        }



        for (int i = 1; i < tradeDatePools.size(); i++) {
            TradeDatePool current = tradeDatePools.get(i);
            String kbarDate = DateUtil.format(current.getTradeDate(),DateUtil.yyyyMMdd);
            TradeDatePool previous = tradeDatePools.get(i-1);
            String preKbarDate = DateUtil.format(previous.getTradeDate(),DateUtil.yyyyMMdd);
            for (CirculateInfo circulateInfo : circulateInfos) {
                String uniqueKey = circulateInfo.getStockCode()+ SymbolConstants.UNDERLINE + kbarDate;
                String preUniqueKey = circulateInfo.getStockCode()+ SymbolConstants.UNDERLINE + preKbarDate;
                StockKbar currentKbar = stockKbarService.getByUniqueKey(uniqueKey);

            }
        }

        return null;
    }

}
