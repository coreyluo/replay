package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class QuanShangHighComponent {
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
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private BlockStockDetailService blockStockDetailService;
    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");



    public void quanShangYiDong(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, CirculateInfo> circulateInfoMap = getCirculateInfoMap(circulateInfos);
        List<BlockStockDetail> blockStocks = getBlockStocks();
        Map<String, List<PlankTimePairDTO>> pairMap = getQuanShangHigh(blockStocks, circulateInfoMap);
        List<QuanShangHighDTO> buys = getHighDay(pairMap);

        List<Object[]> datas = Lists.newArrayList();
        for (QuanShangHighDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getHighTime());
            list.add(dto.getStockCode());
            list.add(circulateInfoMap.get(dto.getStockCode()).getStockName());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","交易日期","时间","股票代码","股票名称"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("券商异动",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("券商异动");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<QuanShangHighDTO> getHighDay(Map<String,List<PlankTimePairDTO>> pairMap){
        List<QuanShangHighDTO> list = Lists.newArrayList();
        for (String tradeDate:pairMap.keySet()){
            List<PlankTimePairDTO> plankTimePairDTOS = pairMap.get(tradeDate);
            if(CollectionUtils.isEmpty(plankTimePairDTOS)){
                continue;
            }
            PlankTimePairDTO first = null;
            for (PlankTimePairDTO dto:plankTimePairDTOS){
                if(first==null||dto.getStart()<first.getStart()){
                    first = dto;
                }
            }
            if(first!=null){
                QuanShangHighDTO highDTO = new QuanShangHighDTO();
                highDTO.setStockCode(first.getStockCode());
                highDTO.setTradeDate(tradeDate);
                highDTO.setHighTime(String.valueOf(first.getStart()));
                list.add(highDTO);
            }
        }
        return list;
    }


    public Map<String, List<PlankTimePairDTO>> getQuanShangHigh(List<BlockStockDetail> details,Map<String,CirculateInfo> circulateInfoMap){
        Map<String, List<PlankTimePairDTO>> map = new HashMap<>();
        for (BlockStockDetail detail:details){
            System.out.println(detail.getStockCode());
            CirculateInfo circulateInfo = circulateInfoMap.get(detail.getStockCode());
            if(circulateInfo==null){
                continue;
            }
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(detail.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(!DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))){
                    BigDecimal marketValue = new BigDecimal(circulateInfo.getCirculate()).multiply(stockKbar.getHighPrice());
                    if(preKbar!=null&&marketValue.compareTo(new BigDecimal(100000000000l))>=0) {
                        BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                        if (highRate.compareTo(new BigDecimal(5)) >= 0) {
                            PlankTimePairDTO highPair = getHighPair(stockKbar, preKbar.getClosePrice());
                            if (highPair != null) {
                                List<PlankTimePairDTO> pairs = map.get(stockKbar.getKbarDate());
                                if (pairs == null) {
                                    pairs = Lists.newArrayList();
                                    map.put(stockKbar.getKbarDate(), pairs);
                                }
                                pairs.add(highPair);
                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }





    public static void main(String[] args) {

    }



    public Map<String,CirculateInfo> getCirculateInfoMap(List<CirculateInfo> circulateInfos){
        Map<String, CirculateInfo> circulateInfoMap= new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            circulateInfoMap.put(circulateInfo.getStockCode(),circulateInfo);
        }
        return circulateInfoMap;
    }

    public List<TradeDatePool> getTradeDates(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        /*query.setTradeDateTo(DateUtil.parseDate("20220501",DateUtil.yyyyMMdd));*/
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }





    public List<BlockStockDetail> getBlockStocks(){
        BlockStockDetailQuery query = new BlockStockDetailQuery();
        query.setBlockCode("880472");
        List<BlockStockDetail> details = blockStockDetailService.listByCondition(query);
        return details;
    }


    public List<StockKbar> limitQueueToList(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        return list;
    }


    public PlankTimePairDTO getHighPair(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        int i = 0;
        int index = 0;
        String preMin = "09:25";
        for (ThirdSecondTransactionDataDTO data:datas) {
            if (data.getTradeTime().equals(preMin)) {
                index++;
            } else {
                preMin = data.getTradeTime();
                index = 0;
            }
            BigDecimal tradePrice = data.getTradePrice();
            BigDecimal rate = PriceUtil.getPricePercentRate(tradePrice.subtract(preEndPrice), preEndPrice);
            if (rate.compareTo(new BigDecimal(5)) >= 0) {
                PlankTimePairDTO pairDTO = new PlankTimePairDTO();
                pairDTO.setStockCode(stockKbar.getStockCode());
                if (data.getTradeTime().equals("09:25")) {
                    pairDTO.setStart(92500l);
                } else {
                    long start = timeToLong(data.getTradeTime(), index);
                    pairDTO.setStart(start);

                }
                return pairDTO;
            }
        }
        return null;
    }

    public static long timeToLong(String time,int index){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        int second = index * 3;
        if(second<10) {
            timeStr = timeStr + "0" +second;
        }else{
            timeStr = timeStr+second;
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }

    public static long timeToLong(String time){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }


    public void calProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                /*if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }*/
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjClosePrice()), buyStockKbar.getAdjClosePrice());
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }




    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            /*query.setKbarDateFrom("");
            query.setKbarDateTo("");*/
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
           /*if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            stockKbars = stockKbars.subList(20, stockKbars.size());*/
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<StockKbar> best = commonComponent.deleteNewStockTimes(stockKbars, 2000);
            return best;
        }catch (Exception e){
            return null;
        }
    }


    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,StockKbar kbar){
        BigDecimal reason = null;
        if(!(kbar.getClosePrice().equals(kbar.getAdjClosePrice()))&&!(kbar.getOpenPrice().equals(kbar.getAdjOpenPrice()))){
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }
    }

    public int calPlanks(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)){
            return 1;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 1;
        int i=0;
        StockKbar nextStockKbar = null;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i>=3){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextStockKbar.getClosePrice(), stockKbar.getClosePrice(), nextStockKbar.getKbarDate());
                if(endUpper){
                    planks++;
                }else {
                    return planks;
                }
            }
            nextStockKbar = stockKbar;
        }
        return planks;
    }






}
