package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockKbarReplayComponent {

    @Autowired
    private ThsBlockInfoService thsBlockInfoService;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockKbarService stockKbarService;

    public void replay(int days){

        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<String> circulateStockList = circulateInfos.stream().map(CirculateInfo::getStockCode).collect(Collectors.toList());

        Map<String,List<String>> blockDetailMap  = new HashMap<>();

        for (ThsBlockInfo thsBlockInfo : thsBlockInfos) {

            ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
            query.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(query);
            List<String> detailList = thsBlockStockDetails.stream()
                    .filter(item -> circulateStockList.contains(item.getStockCode()))
                    .map(ThsBlockStockDetail::getStockCode)
                    .collect(Collectors.toList());
            if(detailList.size()<=10){
                continue;
            }
            blockDetailMap.put(thsBlockInfo.getBlockCode(),detailList);
        }

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (TradeDatePool tradeDatePool : tradeDatePools) {
            Date tradeDate = tradeDatePool.getTradeDate();
            Date preTradeDate = commonComponent.preTradeDate(tradeDate);
            String kbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
            Date fromDate = DateUtil.addDays(preTradeDate, days);

        }


    }

}
