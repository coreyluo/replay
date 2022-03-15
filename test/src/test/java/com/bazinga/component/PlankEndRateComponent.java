package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.ShadowKbarDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class PlankEndRateComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private BlockLevelReplayComponent blockLevelReplayComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private StockAverageLineService stockAverageLineService;

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void plankRates(){
        Map<String, Map<String, Integer>> map = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(String tradeDate:map.keySet()){
            Map<String, Integer> valueMap = map.get(tradeDate);
            List<Object> list = new ArrayList<>();
            list.add(tradeDate);
            list.add(tradeDate);
            list.add(valueMap.get("endUpper"));
            list.add(valueMap.get("highUpper"));
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","tradeDate","尾盘封住数量","触碰过涨停数量"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("封板率",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("封板率");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public Map<String, Map<String,Integer>> getStockUpperShowInfo(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, Map<String, Integer>> rateMap = getStartTime(circulateInfos);
       /* TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20200101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        TradeDatePool preTradeDatePool = null;
        for(TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
            if(preTradeDatePool!=null) {
                Map<String, Integer> upperMap = getStockKbarByDate(circulateInfos, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd), DateUtil.format(preTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),startTimeMap);
                map.put(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd),upperMap);
            }
            preTradeDatePool = tradeDatePool;
        }*/
        return rateMap;
    }



    public Map<String, Integer> getStockKbarByDate(List<CirculateInfo> circulateInfos,String dateStr,String preDateStr,Map<String,String> startTimeMap){
        Map<String, Integer> map = new HashMap<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(dateStr);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbarQuery preKbarQuery = new StockKbarQuery();
        preKbarQuery.setKbarDate(preDateStr);
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preKbarQuery);

        Date tradeDate = DateUtil.parseDate(dateStr, DateUtil.yyyyMMdd);
        Map<String, StockKbar> tradeDateMap = new HashMap<>();
        Map<String, StockKbar> preTradeDateMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            String starTime = startTimeMap.get(stockKbar.getStockCode());
            if(starTime==null){
                continue;
            }
            Date startDate = DateUtil.parseDate(starTime, DateUtil.yyyyMMdd);
            if(!tradeDate.after(startDate)){
                continue;
            }
            if(stockKbar.getTradeQuantity()>=100) {
                tradeDateMap.put(stockKbar.getStockCode(), stockKbar);
            }
        }
        for (StockKbar stockKbar:preStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                preTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        for(CirculateInfo circulateInfo:circulateInfos){
            StockKbar stockKbar = tradeDateMap.get(circulateInfo.getStockCode());
            StockKbar preStockKbar = preTradeDateMap.get(circulateInfo.getStockCode());
            if(stockKbar==null||preStockKbar==null){
                continue;
            }
            boolean endUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
            boolean highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
            if(endUpper){
                Integer count = map.get("endUpper");
                if(count==null){
                    count = 0;
                }
                count = count+1;
                map.put("endUpper",count);

                Integer countHigh = map.get("highUpper");
                if (countHigh == null) {
                    countHigh = 0;
                }
                countHigh = countHigh + 1;
                map.put("highUpper", countHigh);
            }else {
                if (highUpper) {
                    //boolean flag = havePlank(circulateInfo.getStockCode(), stockKbar.getKbarDate(), preStockKbar.getClosePrice());
                    boolean flag = true;
                    if(flag) {
                        Integer count = map.get("highUpper");
                        if (count == null) {
                            count = 0;
                        }
                        count = count + 1;
                        map.put("highUpper", count);
                    }
                }
            }
        }
        return map;
    }
    public boolean havePlank(String stockCode,String kbarDate,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, kbarDate);
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            boolean endUpper = PriceUtil.isHistoryUpperPrice(stockCode, data.getTradePrice(), preEndPrice, kbarDate);
            if(data.getTradeType()==1&&endUpper){
                return true;
            }
        }
        return false;
    }
    public Map<String, Map<String,Integer>> getStartTime(List<CirculateInfo> circulateInfos){
        Map<String, Map<String,Integer>> map = new HashMap<>();
        int i = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(i);
            List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKBars)){
                continue;
            }
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKBars){
                if(preStockKbar!=null){
                    boolean endUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(endUpper){
                        Map<String, Integer> rateMap = map.get(stockKbar.getKbarDate());
                        if(rateMap==null){
                            rateMap = new HashMap<>();
                            map.put(stockKbar.getKbarDate(),rateMap);
                        }
                        Integer endCount = rateMap.get("endUpper");
                        if(endCount==null){
                            endCount = 0;
                        }
                        endCount = endCount+1;
                        rateMap.put("endUpper",endCount);

                        Integer highCount = rateMap.get("highUpper");
                        if (highCount == null) {
                            highCount = 0;
                        }
                        highCount = highCount + 1;
                        rateMap.put("highUpper", highCount);
                    }else {
                        if (highUpper) {
                            //boolean flag = havePlank(circulateInfo.getStockCode(), stockKbar.getKbarDate(), preStockKbar.getClosePrice());
                            boolean flag = true;
                            if(flag) {
                                Map<String, Integer> rateMap = map.get(stockKbar.getKbarDate());
                                if(rateMap==null){
                                    rateMap = new HashMap<>();
                                    map.put(stockKbar.getKbarDate(),rateMap);
                                }
                                Integer highCount = rateMap.get("highUpper");
                                if (highCount == null) {
                                    highCount = 0;
                                }
                                highCount = highCount + 1;
                                rateMap.put("highUpper", highCount);
                            }
                        }
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return map;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            //query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            stockKbars= deleteNewStockTimes(stockKbars, 2000);
            List<StockKbar> list = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>=100){
                    list.add(stockKbar);
                }
            }
            return list;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<StockKbar> deleteNewStockTimes(List<StockKbar> list,int size){
        List<StockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        StockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }

    public static void main(String[] args) {
        ArrayList<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);

    }


}
