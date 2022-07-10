package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.StockIndexQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.HistoryBlockInfoService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsStockIndexComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    /**
     * 上证macd diff dea
     */
    public void shMACDIndex(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate("20220523",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            list.add(tradeDateStr);
        }
        List<StockIndex> szIndexs = thsDataComponent.initStockIndex("000001.SH", "上证指数", list);
        for (StockIndex stockIndex:szIndexs){
            stockIndexService.save(stockIndex);
        }
    }

    /**
     * 板块macd diff dea
     */
    public void blockMACDIndex(){
        Date date = new Date();
        String endDate = DateUtil.format(date, DateUtil.yyyyMMdd);
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20170101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate(endDate,DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            list.add(tradeDateStr);
        }
        HistoryBlockInfoQuery historyBlockInfoQuery = new HistoryBlockInfoQuery();
        historyBlockInfoQuery.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(historyBlockInfoQuery);
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            System.out.println(blockInfo.getBlockCode());
            String blockCode = blockInfo.getBlockCode();
            String thsBlockCode = MarketUtil.generalToThsStock(blockCode);
            Date marketDate = DateUtil.parseDate(blockInfo.getMarketDate(), DateUtil.yyyyMMdd);
            StockIndexQuery stockIndexQuery = new StockIndexQuery();
            stockIndexQuery.setStockCode(thsBlockCode);
            List<String> initeds = Lists.newArrayList();
            List<StockIndex> stockIndices = stockIndexService.listByCondition(stockIndexQuery);
            if(stockIndices!=null) {
                for (StockIndex stockIndex : stockIndices) {
                    initeds.add(stockIndex.getKbarDate());
                }
            }
            List<String> dates = Lists.newArrayList();
            for (String tradeDate:list){
                Date listDate = DateUtil.parseDate(tradeDate, DateUtil.yyyy_MM_dd);
                String yyyymmdd = tradeDate.replace("-", "");
                if((!listDate.before(marketDate))&&(!initeds.contains(yyyymmdd))){
                    dates.add(tradeDate);
                }
            }
            List<StockIndex> szIndexs = thsDataComponent.initStockIndex(thsBlockCode, blockInfo.getBlockName(), dates);
            for (StockIndex stockIndex:szIndexs){
                stockIndexService.save(stockIndex);
            }
        }

    }


}
