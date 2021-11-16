package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.OpenCompeteDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
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
    public Map<String, OpenCompeteDTO> get300CompeteInfo(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->item.getStockCode().startsWith("3")).collect(Collectors.toList());


        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        Map<String ,List<OpenCompeteDTO>> tempMap = new HashMap<>();

        for (CirculateInfo circulateInfo : circulateInfos) {
           /* if(!"300945".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210501");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            for (int i = 1; i < kbarList.size()-1; i++) {
                StockKbar buyStockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar sellStockKbar = kbarList.get(i+1);
                if(!StockKbarUtil.isUpperPrice(buyStockKbar,preStockKbar)){
                    continue;
                }

                OpenCompeteDTO openCompeteDTO = new OpenCompeteDTO();
                openCompeteDTO.setStockCode(buyStockKbar.getStockCode());
                openCompeteDTO.setRate(PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(buyStockKbar.getClosePrice()),buyStockKbar.getClosePrice()));

                List<OpenCompeteDTO> list = tempMap.get(buyStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    list = new ArrayList<>();
                    tempMap.put(buyStockKbar.getKbarDate(),list);
                }
                list.add(openCompeteDTO);
            }
        }
        Map<String,OpenCompeteDTO> resultMap = new HashMap<>();
        for (Map.Entry<String, List<OpenCompeteDTO>> entry : tempMap.entrySet()) {
            String kbarDate = entry.getKey();
            List<OpenCompeteDTO> list = entry.getValue();
            List<OpenCompeteDTO> sortedList = list.stream().sorted(Comparator.comparing(OpenCompeteDTO::getRate)).collect(Collectors.toList());
            sortedList = Lists.reverse(sortedList);
            for (int i = 0; i < sortedList.size(); i++) {
                OpenCompeteDTO openCompeteDTO = sortedList.get(i);
                openCompeteDTO.setCompeteNum(i+1);
                resultMap.put(openCompeteDTO.getStockCode() + SymbolConstants.UNDERLINE + kbarDate,openCompeteDTO);
            }

        }



        return resultMap;
    }

}
