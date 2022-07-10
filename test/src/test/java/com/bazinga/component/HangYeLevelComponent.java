package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.HangYePlankBuyDTO;
import com.bazinga.dto.PlankTimePairDTO;
import com.bazinga.dto.QuanShangHighDTO;
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
import com.bazinga.util.PriceUtil;
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
public class HangYeLevelComponent {
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

    public static final ExecutorService THREAD_POOL_QUOTE_HANGYE = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");



    public void hangyeAmount(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<HangYePlankBuyDTO> buys = getQuanShangHigh(circulateInfos);

        List<Object[]> datas = Lists.newArrayList();
        for (HangYePlankBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getStart());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getPlanks());
            list.add(dto.getOpenRate());
            list.add(dto.getOpenAmount());
            list.add(dto.getRateDay1());
            list.add(dto.getRateDay3());
            list.add(dto.getRateDay5());
            list.add(dto.getRateDay10());
            list.add(dto.getBlockAmountMinute5());
            list.add(dto.getBlockAmountBuy());
            list.add(dto.getBlockAmountDay1());
            list.add(dto.getBlockAmountDay2());
            list.add(dto.getBlockAmountDay3());
            list.add(dto.getBlockAmountDay4());
            list.add(dto.getBlockAmountDay5());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","时间","行业代码","行业名称","连板","开盘涨幅","开盘成交额","1日涨幅","3日涨幅","5日涨幅","10日涨幅",
                "板块前5min成家额","买入前1min成交额","买入前1日成交额","买入前2日成交额","买入前3日成交额","买入前4日成交额","买入前5日成交额","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("行业成交额",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("行业成交额");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<HangYePlankBuyDTO> getQuanShangHigh(List<CirculateInfo> circulateInfos){
        List<HangYePlankBuyDTO> list = new ArrayList<>();
        for (CirculateInfo circulateInfo:circulateInfos){

            System.out.println(circulateInfo.getStockCode());
            if(circulateInfo==null){
                continue;
            }
           /* if (!circulateInfo.getStockCode().equals("002703")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(!DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd))){
                    if(preKbar!=null) {
                        boolean historyUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                        BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                        if (historyUpper) {
                            HangYePlankBuyDTO buyDTO = new HangYePlankBuyDTO();
                            buyDTO.setStockCode(circulateInfo.getStockCode());
                            buyDTO.setStockName(circulateInfo.getStockName());
                            buyDTO.setTradeDate(stockKbar.getKbarDate());
                            int planks = calPlanks(limitQueue);
                            buyDTO.setPlanks(planks);
                            buyDTO.setOpenRate(openRate);
                            getBeforeRate(stockKbars,stockKbar,buyDTO);
                            Long buyTime = getBuyTime(stockKbar, preKbar.getClosePrice(),buyDTO);
                            HistoryBlockStocks historyBlockStocks = stockCodeToBlockCode(circulateInfo.getStockCode(), stockKbar.getKbarDate());
                            if(buyTime!=null&&historyBlockStocks!=null) {
                                buyDTO.setStart(buyTime);
                                buyDTO.setBlockCode(historyBlockStocks.getBlockCode());
                                buyDTO.setBlockName(historyBlockStocks.getBlockName());
                                blockAmountInfo(buyDTO);
                                BigDecimal profit = calProfit(stockKbars, stockKbar);
                                buyDTO.setProfit(profit);
                                list.add(buyDTO);
                               /* if(list.size()>=100){
                                    return list;
                                }*/
                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return list;
    }


    public void calHangYeKbarToRedis(){
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            if(!historyBlockInfo.getBlockCode().startsWith("881")){
                continue;
            }
            THREAD_POOL_QUOTE_HANGYE.execute(() ->{
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setStockCode(historyBlockInfo.getBlockCode());
                stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
                stockKbarQuery.setLimit(100000);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
                if(!CollectionUtils.isEmpty(stockKbars)) {
                    String tradeDate = null;
                    BigDecimal totalAmount = null;
                    for (StockKbar stockKbar : stockKbars) {
                        String substring = stockKbar.getKbarDate().substring(0, 8);
                        if (tradeDate == null || !tradeDate.equals(substring)) {
                            if (tradeDate != null) {
                                System.out.println(historyBlockInfo.getBlockCode() + "===" + substring);
                                RedisMonior redisMonior = new RedisMonior();
                                redisMonior.setRedisKey(historyBlockInfo.getBlockCode() + "_" + tradeDate + "_" + "amount");
                                redisMonior.setRedisValue(totalAmount.toString());
                                RedisMonior inRedies = redisMoniorService.getByRedisKey(redisMonior.getRedisKey());
                                if (inRedies == null) {
                                    redisMoniorService.save(redisMonior);
                                }
                            }
                            tradeDate = substring;
                            totalAmount = BigDecimal.ZERO;
                        }
                        totalAmount = totalAmount.add(stockKbar.getTradeAmount());
                        RedisMonior redisMonior = new RedisMonior();
                        redisMonior.setRedisKey(historyBlockInfo.getBlockCode() + "_" + stockKbar.getKbarDate() + "_" + "current");
                        redisMonior.setRedisValue(totalAmount.toString());
                        RedisMonior inRedies = redisMoniorService.getByRedisKey(redisMonior.getRedisKey());
                        if (inRedies == null) {
                            redisMoniorService.save(redisMonior);
                        }
                    }
                }
            });
        }
        try {
            Thread.sleep(10000000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String str = "11132232";
        System.out.println(str.substring(0,3));
    }


    public void blockAmountInfo(HangYePlankBuyDTO buyDTO){
        String blockCode = buyDTO.getBlockCode();
        String tradeDate = buyDTO.getTradeDate();
        String buyTime = String.valueOf(buyDTO.getStart());
        if(buyTime.startsWith("9")){
            buyTime = "0"+buyTime;
        }
        String HHMM = buyTime.substring(0, 4);
        String buyTimeStamp = tradeDate+HHMM+"00";
        Date date = DateUtil.parseDate(buyTimeStamp, DateUtil.yyyyMMddHHmmss);
        Date date0930 = DateUtil.parseDate(tradeDate+"093000", DateUtil.yyyyMMddHHmmss);
        Date date1130 = DateUtil.parseDate(tradeDate+"113000", DateUtil.yyyyMMddHHmmss);
        Date date1301 = DateUtil.parseDate(tradeDate+"130100", DateUtil.yyyyMMddHHmmss);
        Date preMin = DateUtil.addMinutes(date, -1);
        if(preMin.before(date0930)){
            preMin = date0930;
        }
        if(preMin.before(date1301)&&preMin.after(date1130)){
            preMin = date1130;
        }
        String preTimeStamp = DateUtil.format(preMin, DateUtil.yyyyMMddHHmmss);
        String uk = blockCode+"_"+preTimeStamp+"_"+"current";
        RedisMonior redisKeyCurrent = redisMoniorService.getByRedisKey(uk);
        if(redisKeyCurrent!=null){
            buyDTO.setBlockAmountBuy(new BigDecimal(redisKeyCurrent.getRedisValue()));
        }
        String uk0935 = blockCode+"_"+tradeDate+"093500"+"_"+"current";
        RedisMonior redisKey0935 = redisMoniorService.getByRedisKey(uk0935);
        if(redisKey0935!=null){
            buyDTO.setBlockAmountMinute5(new BigDecimal(redisKey0935.getRedisValue()));
        }
        Date currentDay = DateUtil.parseDate(buyDTO.getTradeDate(), DateUtil.yyyyMMdd);
        Date preDay1 = commonComponent.preTradeDate(currentDay);
        Date preDay2 = commonComponent.preTradeDate(preDay1);
        Date preDay3 = commonComponent.preTradeDate(preDay2);
        Date preDay4 = commonComponent.preTradeDate(preDay3);
        Date preDay5 = commonComponent.preTradeDate(preDay4);
        String day1 = DateUtil.format(preDay1, DateUtil.yyyyMMdd);
        String day2 = DateUtil.format(preDay2, DateUtil.yyyyMMdd);
        String day3 = DateUtil.format(preDay3, DateUtil.yyyyMMdd);
        String day4 = DateUtil.format(preDay4, DateUtil.yyyyMMdd);
        String day5 = DateUtil.format(preDay5, DateUtil.yyyyMMdd);
        String ukday1 = blockCode+"_"+day1+"_"+"amount";
        String ukday2 = blockCode+"_"+day2+"_"+"amount";
        String ukday3 = blockCode+"_"+day3+"_"+"amount";
        String ukday4 = blockCode+"_"+day4+"_"+"amount";
        String ukday5 = blockCode+"_"+day5+"_"+"amount";
        RedisMonior redisKeyday1 = redisMoniorService.getByRedisKey(ukday1);
        RedisMonior redisKeyday2 = redisMoniorService.getByRedisKey(ukday2);
        RedisMonior redisKeyday3 = redisMoniorService.getByRedisKey(ukday3);
        RedisMonior redisKeyday4 = redisMoniorService.getByRedisKey(ukday4);
        RedisMonior redisKeyday5 = redisMoniorService.getByRedisKey(ukday5);
        if(redisKeyday1!=null){
            buyDTO.setBlockAmountDay1(new BigDecimal(redisKeyday1.getRedisValue()));
        }
        if(redisKeyday2!=null){
            buyDTO.setBlockAmountDay2(new BigDecimal(redisKeyday2.getRedisValue()));
        }
        if(redisKeyday3!=null){
            buyDTO.setBlockAmountDay3(new BigDecimal(redisKeyday3.getRedisValue()));
        }
        if(redisKeyday4!=null){
            buyDTO.setBlockAmountDay4(new BigDecimal(redisKeyday4.getRedisValue()));
        }
        if(redisKeyday5!=null){
            buyDTO.setBlockAmountDay5(new BigDecimal(redisKeyday5.getRedisValue()));
        }
    }

    public HistoryBlockStocks stockCodeToBlockCode(String stockCode,String tradeDate){
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> stocks = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(stocks)){
            return null;
        }
        for (HistoryBlockStocks detail:stocks){
            if(!detail.getBlockCode().startsWith("881")){
                continue;
            }
            String details = detail.getStocks();
            if(StringUtils.isBlank(details)){
                continue;
            }
            String[] split = details.split(",");
            List<String> stockList = Lists.newArrayList(split);
            if(stockList.contains(stockCode)){
                return detail;
            }
        }
        return null;
    }





    public Map<String,CirculateInfo> getCirculateInfoMap(List<CirculateInfo> circulateInfos){
        Map<String, CirculateInfo> circulateInfoMap= new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            circulateInfoMap.put(circulateInfo.getStockCode(),circulateInfo);
        }
        return circulateInfoMap;
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


    public Long getBuyTime(StockKbar stockKbar,BigDecimal preEndPrice,HangYePlankBuyDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        int index = 0;
        String preMin = "09:25";
        boolean preIsPlank = true;
        for (ThirdSecondTransactionDataDTO data:datas) {
            if (data.getTradeTime().equals(preMin)) {
                index++;
            } else {
                preMin = data.getTradeTime();
                index = 0;
            }
            Integer tradeType = data.getTradeType();
            BigDecimal tradePrice = data.getTradePrice();
            String tradeTime = data.getTradeTime();
            boolean priceUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice, stockKbar.getKbarDate());
            if(tradeTime.equals("09:25")){
                buyDTO.setOpenAmount(tradePrice.multiply(new BigDecimal(data.getTradeQuantity()*100)));
                if(priceUpper) {
                    preIsPlank = true;
                }else{
                    preIsPlank = false;
                }
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            if(priceUpper&&tradeType==1){
                if(!preIsPlank){
                    long start = timeToLong(data.getTradeTime(), index);
                    return start;
                }
                preIsPlank = true;
            }else{
                preIsPlank = false;
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


    public BigDecimal calProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjHighPrice()), buyStockKbar.getAdjHighPrice());
                return profit;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }

    public void getBeforeRate(List<StockKbar> stockKbars,StockKbar buyStockKbar,HangYePlankBuyDTO hangYePlankBuyDTO){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i=0;
        StockKbar endKbar = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endKbar = stockKbar;
            }
            if(i==2){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                hangYePlankBuyDTO.setRateDay1(rate);
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                hangYePlankBuyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                hangYePlankBuyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                hangYePlankBuyDTO.setRateDay10(rate);
            }
            if(i>=12){
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
