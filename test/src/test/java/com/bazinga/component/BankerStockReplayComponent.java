package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BankerStockReplayComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());


        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20211101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item->item.getTradeQuantity()!=0).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<20){
                continue;
            }

            for (int i = 10; i < stockKbarList.size(); i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                List<StockKbar> pre10List = stockKbarList.subList(i - 11, i);

                getAmountMap(pre10List);





            }



        }


    }

    private Map<String, BigDecimal> getAmountMap(List<StockKbar> stockKbarList) {
        Map<String, BigDecimal> tradeAmountMap = new HashMap<>();

        for (int i = 1; i < stockKbarList.size(); i++) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i-1);
            if(StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                continue;
            }

            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());

            for (int j = 0; j < list.size(); j++) {


            }



        }



        return tradeAmountMap;

    }
}
