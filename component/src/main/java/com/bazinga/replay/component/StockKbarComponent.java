package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.bazinga.base.Sort;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoAllService;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateFormatUtils;
import com.bazinga.util.DateUtil;
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
                if (adjFactorDTO.getAdjFactor() == null) {
                    log.info("当前日不是交易日 today ={}", todayDate);
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
                                        stockKbarService.save(item);
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
            updateKbarDataDaily(item.getStockCode(), item.getStockName());
        }); }

    public void batchKbarDataInit() {
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        circulateInfos.forEach(item -> {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(item.getStockCode());
            int count = stockKbarService.countByCondition(query);
            if (count == 0) {
                initAndSaveKbarData(item.getStockCode(), item.getStockName(), 100);
            }
        });
    }

    public static void main(String[] args) {
        Map<String, AdjFactorDTO> adjFactorList = getAdjFactorMap("600095", "20210522");
      //  Map<String, String> notices = getNotices("600095", "20210525");
        System.out.println(JSONObject.toJSONString(adjFactorList));
    }

    public static Map<String,String> getNotices(String stockCode, String kbarDate ){
        Map<String,String> resultMap = Maps.newHashMap();
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("api_name", "adj_factor");
        paramMap.put("token", "f9d25f4ab3f0abe5e04fdf76c32e8c8a5cc94e384774da025098ec6e");
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
        paramMap.put("token", "f9d25f4ab3f0abe5e04fdf76c32e8c8a5cc94e384774da025098ec6e");
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




    public void histotyTradeData(){
        DataTable dataTable = TdxHqUtil.getHistoryTransactionData("000001", 20200114,0,200);
        System.out.println(dataTable);

    }


}
