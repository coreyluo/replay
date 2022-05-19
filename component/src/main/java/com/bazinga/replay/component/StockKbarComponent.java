package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateFormatUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StockKbarComponent {

    private static final int  LOOP_TIMES =3;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockAverageLineService stockAverageLineService;

    private static final ExecutorService AVGLINE_POOL = ThreadPoolUtils.create(8, 16, 512, "calAvgLinePool");

    public Map<String,Long> getCybMinQuantity(){
        Map<String,Long> resultMap = Maps.newHashMap();
        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20200821",DateUtil.yyyyMMdd));
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData("399006",  tradeDatePool.getTradeDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tradeDatePool.getTradeDate());
            calendar.set(Calendar.HOUR,9);
            calendar.set(Calendar.MINUTE,30);
            Date time = calendar.getTime();
            for (int i = 0; i < 70; i++) {
                Date date = DateUtil.addMinutes(time, i);
                String fixTime = DateUtil.format(date, "HH:mm");
                List<ThirdSecondTransactionDataDTO> resultList = historyTransactionDataComponent.getFixTimeData(list,fixTime);
                int sum = resultList.stream().mapToInt(ThirdSecondTransactionDataDTO::getTradeQuantity).sum();
                resultMap.put(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd)+fixTime,sum*100L);
            }
        }
        return resultMap;

    }

    public void batchInitAndSaveKbarDate(String stockCode,String stockName,int days){
        int onceDays = 400;
        int index = (days / onceDays)+1;
        List<StockKbar> revers = Lists.newArrayList();
        for (int i=0;i<index;i++){
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, onceDays*i, onceDays);
            List<StockKbar> stockKbarList = StockKbarConvert.convert(dataTable, stockCode, stockName);
            if(!CollectionUtils.isEmpty(stockKbarList)){
                List<StockKbar> kbars = Lists.reverse(stockKbarList);
                revers.addAll(kbars);
            }
        }
        if(CollectionUtils.isEmpty(revers)){
            return;
        }
        List<StockKbar> stockKbarList = Lists.reverse(revers);
        Map<String, AdjFactorDTO> adjFactorMap = getAdjFactorMap(stockCode, null);
        if (adjFactorMap == null || adjFactorMap.size() == 0) {
            log.info("stockCode ={} http方式获取复权因子失败", stockCode);
            return;
        }
        if(adjFactorMap.get(stockKbarList.get(stockKbarList.size() - 1).getKbarDate())==null){
            return;
        }
        BigDecimal maxAdjFactor = adjFactorMap.get(stockKbarList.get(stockKbarList.size() - 1).getKbarDate()).getAdjFactor();
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            try {
                stockKbarList.forEach(item -> {
                    BigDecimal adjFactor = adjFactorMap.get(item.getKbarDate()).getAdjFactor();
                    item.setAdjFactor(adjFactor);
                    BigDecimal preFactor = adjFactor.divide(maxAdjFactor, 10, BigDecimal.ROUND_HALF_UP);
                    item.setAdjOpenPrice(item.getOpenPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjClosePrice(item.getClosePrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjHighPrice(item.getHighPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjLowPrice(item.getLowPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    stockKbarService.save(item);
                });
            } catch (Exception e) {
                e.printStackTrace();
                status.setRollbackOnly();
                log.info("rollback transaction: " + status);
            }
            return null;
        });


    }


    public void initAndSaveKbarData(String stockCode, String stockName, int days) {
        DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, days);
        List<StockKbar> stockKbarList = StockKbarConvert.convert(dataTable, stockCode, stockName);
        if(CollectionUtils.isEmpty(stockKbarList)){
            return;
        }
        Map<String, AdjFactorDTO> adjFactorMap = getAdjFactorMap(stockCode, null);
        if (adjFactorMap == null || adjFactorMap.size() == 0) {
            log.info("stockCode ={} http方式获取复权因子失败", stockCode);
            return;
        }
        if(adjFactorMap.get(stockKbarList.get(stockKbarList.size() - 1).getKbarDate())==null){
            return;
        }
        BigDecimal maxAdjFactor = adjFactorMap.get(stockKbarList.get(stockKbarList.size() - 1).getKbarDate()).getAdjFactor();
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            try {
                stockKbarList.forEach(item -> {
                    BigDecimal adjFactor = adjFactorMap.get(item.getKbarDate()).getAdjFactor();
                    item.setAdjFactor(adjFactor);
                    BigDecimal preFactor = adjFactor.divide(maxAdjFactor, 10, BigDecimal.ROUND_HALF_UP);
                    item.setAdjOpenPrice(item.getOpenPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjClosePrice(item.getClosePrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjHighPrice(item.getHighPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    item.setAdjLowPrice(item.getLowPrice().multiply(preFactor).setScale(2, RoundingMode.HALF_UP));
                    if(item.getTradeQuantity()>0){
                        stockKbarService.save(item);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                status.setRollbackOnly();
                log.info("rollback transaction: " + status);
            }
            return null;
        });

    }

    public void initSpecialStockAndSaveKbarData(String stockCode, String stockName, int days) {
        try {
            for(int i=0;i<days;i++) {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, i, 1);
                List<StockKbar> stockKbarList = StockKbarConvert.convertSpecial(dataTable, stockCode, stockName);
                if (CollectionUtils.isEmpty(stockKbarList)) {
                    return;
                }
                for (StockKbar stockKbar : stockKbarList) {
                    stockKbar.setAdjClosePrice(stockKbar.getClosePrice());
                    stockKbar.setAdjOpenPrice(stockKbar.getOpenPrice());
                    stockKbar.setAdjHighPrice(stockKbar.getHighPrice());
                    stockKbar.setAdjLowPrice(stockKbar.getLowPrice());
                    StockKbar byUniqueKey = stockKbarService.getByUniqueKey(stockKbar.getUniqueKey());
                    if (byUniqueKey == null) {
                        stockKbarService.save(stockKbar);
                    }

                }
            }
        }catch (Exception e){
            log.info("跑昨日涨停数据异常",e);
        }
    }


    public void updateKbarDataDaily(String stockCode, String stockName) {

        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        query.setLimit(1);
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
        if (CollectionUtils.isEmpty(stockKbarList)) {
            initAndSaveKbarData(stockCode, stockName, 50);
        } else {
            String kbarDate = stockKbarList.get(0).getKbarDate();
            TradeDatePoolQuery trateDateQuery = new TradeDatePoolQuery();
            trateDateQuery.setTradeDateTo(new Date());
            trateDateQuery.addOrderBy("trade_date", Sort.SortType.DESC);
            trateDateQuery.setLimit(1);
            List<TradeDatePool> lastestDayList = tradeDatePoolService.listByCondition(trateDateQuery);
            String todayDate = DateFormatUtils.format(lastestDayList.get(0).getTradeDate(), DateUtil.yyyyMMdd);
            if (todayDate.equals(kbarDate)) {
                return;
            } else {
                Map<String, AdjFactorDTO> adjFactorMap = getAdjFactorMap(stockCode, "");
                if (adjFactorMap == null || adjFactorMap.size() == 0) {
                    log.info("stockCode ={} 更新K线数据时http方式获取复权因子失败", stockCode);
                    return;
                }
                AdjFactorDTO adjFactorDTO = adjFactorMap.get(todayDate);
                if (adjFactorDTO==null||adjFactorDTO.getAdjFactor() == null) {
                    log.info("当前日不是交易日 today ={}", todayDate);
                    return;
                }
                if (stockKbarList.get(0).getAdjFactor().compareTo(adjFactorDTO.getAdjFactor()) == 0) {
                    TradeDatePoolQuery tradeDatequery = new TradeDatePoolQuery();
                    tradeDatequery.setTradeDateFrom(DateUtil.addDays(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd), 1));
                    tradeDatequery.setTradeDateTo(new Date());
                    List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatequery);
                    if (CollectionUtils.isEmpty(tradeDatePools)) {
                        log.info("没有需要更新的交易日数据 stockCode ={}", stockCode);
                        return;
                    }
                    List<String> updateTradeDateList = tradeDatePools.stream().map(TradeDatePool::getTradeDate).
                            map(item -> DateFormatUtils.format(item, DateUtil.yyyyMMdd)).
                            collect(Collectors.toList());
                    DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 60);
                    List<StockKbar> tdxStockKbarList = StockKbarConvert.convert(dataTable, stockCode, stockName);
                    transactionTemplate.execute((TransactionCallback<Void>) status -> {
                        try {
                            tdxStockKbarList.stream().filter(item -> updateTradeDateList.contains(item.getKbarDate()))
                                    .forEach(item -> {
                                        BigDecimal adjFactor = adjFactorMap.get(item.getKbarDate()).getAdjFactor();
                                        item.setAdjFactor(adjFactor);
                                        item.setAdjOpenPrice(item.getOpenPrice());
                                        item.setAdjClosePrice(item.getClosePrice());
                                        item.setAdjHighPrice(item.getHighPrice());
                                        item.setAdjLowPrice(item.getLowPrice());
                                        if(item.getTradeQuantity()>0){
                                            stockKbarService.save(item);
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                            status.setRollbackOnly();
                            log.info("rollback transaction: " + status);
                        }
                        return null;
                    });
                    log.info("更新K线数据 完成 stockCode ={}", stockCode);
                } else {
                    log.info("复权因子发生变更 stockCode ={}", stockCode);
                    stockKbarService.deleteByStockCode(stockCode);
                    initAndSaveKbarData(stockCode, stockName, 500);
                }
            }
        }

    }

    public void batchUpdateDaily() {
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        //circulateInfoQuery.setMarketType(MarketTypeEnum.GENERAL.getCode());
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        circulateInfos.forEach(item -> {
            log.info("更新K线数据开始 stockCode ={}", item.getStockCode());
            try {
                updateKbarDataDaily(item.getStockCode(), item.getStockName());
            }catch (Exception e){
                log.info(e.getMessage());
            }
        }); }

    public void batchKbarDataInit() {
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        circulateInfos.forEach(item -> {
            System.out.println(item.getStockCode());
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(item.getStockCode());
            int count = stockKbarService.countByCondition(query);
            if (count == 0) {
                batchInitAndSaveKbarDate(item.getStockCode(), item.getStockName(), 1100);
            }
        });
    }

    public void batchKbarDataInitToStock(String stockCode,String stockName) {
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        int count = stockKbarService.countByCondition(query);
        if (count == 0) {
            initAndSaveKbarData(stockCode, stockName, 105);
        }
    }

    public static void main(String[] args) {
        int i = 15/20;
        System.out.println(i);
        //Map<String, AdjFactorDTO> adjFactorList = getAdjFactorMap("600095", "20210522");
      //  Map<String, String> notices = getNotices("600095", "20210525");
        //System.out.println(JSONObject.toJSONString(adjFactorList));
    }

    public static Map<String,String> getNotices(String stockCode, String kbarDate ){
        Map<String,String> resultMap = Maps.newHashMap();
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("api_name", "adj_factor");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        String tsStockCode = stockCode.startsWith("6") ? stockCode + ".SH" : stockCode + ".SZ";
        paramsdate.put("ts_code", tsStockCode);
        if (StringUtils.isNotBlank(kbarDate)) {
            paramsdate.put("date", tsStockCode);
        }
        paramMap.put("params", paramsdate);
        paramMap.put("fields", "title,type,url");
        int times = 1;
        while (times <= LOOP_TIMES){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if (CollectionUtils.isEmpty(fields)) {
                    return null;
                }
                return resultMap;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}", times,stockCode, e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public static Map<String, AdjFactorDTO> getAdjFactorMap(String stockCode, String kbarDateFrom) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "adj_factor");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        String tsStockCode = stockCode.startsWith("6") ? stockCode + ".SH" : stockCode + ".SZ";
        paramsdate.put("ts_code", tsStockCode);
        if (StringUtils.isNotBlank(kbarDateFrom)) {
            paramsdate.put("start_date", kbarDateFrom);
        }
        paramMap.put("params", paramsdate);
        paramMap.put("fields", "trade_date,adj_factor");
        int times = 1;
        while (times <= LOOP_TIMES){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if (CollectionUtils.isEmpty(fields)) {
                    return null;
                }
                Map<String, AdjFactorDTO> resultMap = Maps.newHashMap();
                for (int i = 0; i < fields.size(); i++) {
                    String kbarDate = fields.getJSONArray(i).getString(0);
                    resultMap.put(kbarDate, new AdjFactorDTO(stockCode, kbarDate, new BigDecimal(fields.getJSONArray(i).getString(1))));
                }
                return resultMap;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}", times,stockCode, e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public Double calDaysAvg(String stockCode, String kbarDate, int days) {
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDateTo(kbarDate);
        query.setStockCode(stockCode);
        query.setLimit(days);
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

        if (CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size() < days) {
            return null;
        }
        if (!kbarDate.equals(stockKbarList.get(0).getKbarDate())) {
            //log.info("没有该交易日的K线数据 stockCode ={}, kbarDate ={}", stockCode, kbarDate);
            return null;
        }
        return stockKbarList.stream().map(StockKbar::getAdjClosePrice).mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
    }

    public void calAvgLine(String stockCode, String stockName, int days) {
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(new TradeDatePoolQuery());
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateFormatUtils.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            Double avgPrice = calDaysAvg(stockCode, kbarDate, days);
            if (avgPrice != null) {
                StockAverageLine stockAverageLine = new StockAverageLine();
                stockAverageLine.setAveragePrice(new BigDecimal(avgPrice).setScale(2, RoundingMode.HALF_UP));
                stockAverageLine.setDayType(days);
                stockAverageLine.setKbarDate(kbarDate);
                stockAverageLine.setStockName(stockName);
                stockAverageLine.setUniqueKey(stockCode + SymbolConstants.UNDERLINE + kbarDate + SymbolConstants.UNDERLINE + days);
                stockAverageLine.setStockCode(stockCode);
                stockAverageLineService.save(stockAverageLine);
            }
        }

    }

    public void calOneDayAvgLine(String stockCode, String stockName, int days,Date date) {
        String kbarDate = DateFormatUtils.format(date, DateUtil.yyyyMMdd);
        Double avgPrice = calDaysAvg(stockCode, kbarDate, days);
        if (avgPrice != null) {
            StockAverageLine stockAverageLine = new StockAverageLine();
            stockAverageLine.setAveragePrice(new BigDecimal(avgPrice).setScale(2, RoundingMode.HALF_UP));
            stockAverageLine.setDayType(days);
            stockAverageLine.setKbarDate(kbarDate);
            stockAverageLine.setStockName(stockName);
            stockAverageLine.setUniqueKey(stockCode + SymbolConstants.UNDERLINE + kbarDate + SymbolConstants.UNDERLINE + days);
            stockAverageLine.setStockCode(stockCode);
            stockAverageLineService.save(stockAverageLine);
        }
    }


    public void batchcalAvgLine() {
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        int index = 0;
        for (CirculateInfo item:circulateInfos) {
            index = index + 1;
            System.out.println(index + "=======================");
            AVGLINE_POOL.execute(() -> {
                calStockAvgLineOneTimes(item.getStockCode(),item.getStockName(),5);
                System.out.println(item.getStockCode()+"结束");
            });
        }
    }

    public void calCurrentDayAvgLine(Date date) {
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        int index = 0;
        for (CirculateInfo item:circulateInfos) {
            index = index + 1;
            System.out.println(index + "=======================");
            //AVGLINE_POOL.execute(() -> {
                StockAverageLine avgLine = stockAverageLineService.getByUniqueKey(item.getStockCode() + "" + DateUtil.format(date, DateUtil.yyyyMMdd)+ "" + 5);
                if (avgLine == null) {
                    calOneDayAvgLine(item.getStockCode(), item.getStockName(), 5,date);
                }
                System.out.println(item.getStockCode()+"结束");
           // });
        }
    }

    public void calStockAvgLineOneTimes(String stockCode,String stockName,int days){
        System.out.println(stockCode+"开始");
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
        if (CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size() < days) {
            return;
        }
        StockAverageLineQuery stockAverageLineQuery = new StockAverageLineQuery();
        stockAverageLineQuery.setStockCode(stockCode);
        int count = stockAverageLineService.countByCondition(stockAverageLineQuery);
        if (count > 0) {
            return;
        }
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(days);
        for (StockKbar stockKbar:stockKbarList){
            limitQueue.offer(stockKbar);
            if(limitQueue.size()<days){
                continue;
            }
            ArrayList<StockKbar> list = Lists.newArrayList();
            list.addAll(limitQueue);
            Double avgPrice = list.stream().map(StockKbar::getAdjClosePrice).mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
            if (avgPrice != null) {
                StockAverageLine stockAverageLine = new StockAverageLine();
                stockAverageLine.setAveragePrice(new BigDecimal(avgPrice).setScale(2, RoundingMode.HALF_UP));
                stockAverageLine.setDayType(days);
                stockAverageLine.setKbarDate(stockKbar.getKbarDate());
                stockAverageLine.setStockName(stockName);
                stockAverageLine.setUniqueKey(stockCode + SymbolConstants.UNDERLINE + stockKbar.getKbarDate() + SymbolConstants.UNDERLINE + days);
                stockAverageLine.setStockCode(stockCode);
                stockAverageLineService.save(stockAverageLine);
            }
        }
    }

    public Double avgLineResult(String stockCode, String kbarDate) {
        StockAverageLineQuery query = new StockAverageLineQuery();
        query.setKbarDateTo(kbarDate);
        query.setStockCode(stockCode);
        query.setDayType(60);
        query.setLimit(10);
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        List<StockAverageLine> stockAverageLines = stockAverageLineService.listByCondition(query);
        if (stockAverageLines.size() == 10 && kbarDate.equals(stockAverageLines.get(0).getKbarDate())) {
            int moreNum = 0;
            int lessNum = 0;
            for (int i = 0; i < stockAverageLines.size() - 1; i++) {
                for (int j = i + 1; j < stockAverageLines.size(); j++) {
                    if (stockAverageLines.get(i).getAveragePrice().compareTo(stockAverageLines.get(j).getAveragePrice()) >= 0) {
                        moreNum = moreNum + 1;
                    } else {
                        lessNum = lessNum + 1;
                    }
                }
            }
            log.info("stockCode ={}, kbarDate ={}, moreNum ={} ,lessNum = {} percent ={}", stockCode, kbarDate, moreNum, lessNum, Math.round(moreNum * 10000 / (moreNum + lessNum)) * 0.01);
            return Math.round(moreNum * 10000 / (moreNum + lessNum)) * 0.01;
        }
        return null;
    }


    public void histotyTradeData(){
        DataTable dataTable = TdxHqUtil.getHistoryTransactionData("000001", 20200114,0,200);
        System.out.println(dataTable);

    }
    //最多500条
    public List<StockKbar> getStockKBarRemoveNew(String stockCode,int size,int days) {
        List<StockKbar> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.setStockCode(stockCode);
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
        stockKbarQuery.setLimit(size);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        if(CollectionUtils.isEmpty(stockKbars)){
            return list;
        }
        List<StockKbar> kbars = Lists.reverse(stockKbars);
        if(kbars.size()==size){
            List<StockKbar> result = kbars.subList(days, kbars.size());
            return result;
        }
        BigDecimal preEndPrice = null;
        StockKbar firstKbar = null;
        boolean flag = false;
        for (StockKbar kbar:kbars){
            if(preEndPrice!=null){
                boolean upperPrice = PriceUtil.isUpperPrice(kbar.getStockCode(), kbar.getHighPrice(), preEndPrice);
                if(!flag) {
                    if (kbar.getLowPrice().compareTo(kbar.getHighPrice()) != 0 || !upperPrice) {
                        list.add(firstKbar);
                        flag = true;
                    }
                }
                if(flag){
                    list.add(kbar);
                }
            }
            preEndPrice = kbar.getClosePrice();
            firstKbar = kbar;
        }
        if(list.size()>days){
            List<StockKbar> result = list.subList(list.size() - days, list.size());
            return result;
        }
        return list;
    }

    //最多500条
    public List<StockKbar> getStockKBarRemoveNewDays(String stockCode,int size,int days) {
        List<StockKbar> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.setStockCode(stockCode);
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
        stockKbarQuery.setLimit(size);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        if(CollectionUtils.isEmpty(stockKbars)){
            return list;
        }
        List<StockKbar> kbars = Lists.reverse(stockKbars);
        if(kbars.size()==size){
            List<StockKbar> result = kbars.subList(kbars.size()-days, kbars.size());
            return result;
        }
        BigDecimal preEndPrice = null;
        StockKbar firstKbar = null;
        boolean flag = false;
        for (StockKbar kbar:kbars){
            if(preEndPrice!=null){
                boolean upperPrice = PriceUtil.isUpperPrice(kbar.getStockCode(), kbar.getHighPrice(), preEndPrice);
                if(!flag) {
                    if (kbar.getLowPrice().compareTo(kbar.getHighPrice()) != 0 || !upperPrice) {
                        list.add(firstKbar);
                        flag = true;
                    }
                }
                if(flag){
                    list.add(kbar);
                }
            }
            preEndPrice = kbar.getClosePrice();
            firstKbar = kbar;
        }
        if(list.size()>days){
            List<StockKbar> result = list.subList(list.size() - days, list.size());
            return result;
        }
        return list;
    }


}
