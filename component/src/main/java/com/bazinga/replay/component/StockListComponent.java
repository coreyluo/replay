package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.CirculateDetailDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.*;
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
public class StockListComponent {

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
    @Autowired
    private CommonComponent commonComponent;

    private static final ExecutorService AVGLINE_POOL = ThreadPoolUtils.create(8, 16, 512, "calAvgLinePool");
    public List<CirculateInfo> getCirculateInfo(Date date){
        Date preTradeDate = commonComponent.preTradeDate(date);
        List<CirculateDetailDTO> stocks = getStocks();
        Map<String, CirculateDetailDTO> marketInfoMap = getMarketInfo(DateUtil.format(preTradeDate, DateUtil.yyyyMMdd));
        List<CirculateInfo> circulateInfos = Lists.newArrayList();
        for (CirculateDetailDTO circulateDetailDTO:stocks){
            CirculateDetailDTO marketInfo = marketInfoMap.get(circulateDetailDTO.getStockCode());
            CirculateInfo circulateInfo = new CirculateInfo();
            circulateInfo.setStockCode(circulateDetailDTO.getStockCode());
            circulateInfo.setStockName(circulateDetailDTO.getStockName());
            circulateInfo.setCreateTime(new Date());
            Integer marketCode = MarketUtil.getMarketCode(circulateInfo.getStockCode());
            circulateInfo.setMarketType(marketCode);
            if(marketInfo!=null) {
                circulateInfo.setCirculate(marketInfo.getCirculate());
                circulateInfo.setCirculateZ(marketInfo.getCirculateZ());
                circulateInfo.setStockType(CommonUtil.getStockType(marketInfo.getCirculateZ().longValue()));
            }else{
                circulateInfo.setCirculate(100000000l);
                circulateInfo.setCirculateZ(100000000l);
                circulateInfo.setStockType(CommonUtil.getStockType(circulateInfo.getCirculateZ().longValue()));
            }
            circulateInfo.setCreateTime(new Date());
            circulateInfos.add(circulateInfo);
        }
        List<CirculateInfo> alls = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, CirculateInfo> map = new HashMap<>();
        for (CirculateInfo all:alls){
            map.put(all.getStockCode(),all);
        }
        Map<String, CirculateInfo> tushareStockMap = new HashMap<>();
        int i = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            tushareStockMap.put(circulateInfo.getStockCode(),circulateInfo);
            CirculateInfo all = map.get(circulateInfo.getStockCode());
            if(all==null){
                circulateInfoService.save(circulateInfo);
            }
        }
        for (CirculateInfo circulateInfo:alls){
            CirculateInfo tuShareStock = tushareStockMap.get(circulateInfo.getStockCode());
            if(tuShareStock==null){
                circulateInfoService.deleteById(circulateInfo.getId());
                System.out.println(circulateInfo.getStockCode()+"=========");
            }
        }
        System.out.println(i);
        return circulateInfos;
    }
    public  List<CirculateDetailDTO> getStocks() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "stock_basic");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        paramMap.put("list_status", "L");
       /* paramMap.put("params", paramsdate);
        paramMap.put("fields", "trade_date,adj_factor");*/
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
                List<CirculateDetailDTO> circulateInfos = convertToCirculate(fields);
                List<CirculateDetailDTO> list = filterCirculate(circulateInfos);
                return list;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}",  e);
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

    public  Map<String, CirculateDetailDTO> getMarketInfo(String tradeDate) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "daily_basic");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        //String tsStockCode = stockCode.startsWith("6") ? stockCode + ".SH" : stockCode + ".SZ";
        //paramsdate.put("ts_code", tsStockCode);
        paramsdate.put("trade_date",tradeDate);
        paramMap.put("params", paramsdate);
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
                Map<String, CirculateDetailDTO> marketInfoMap = convertToMarketInfo(fields);
                return marketInfoMap;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}", times, e);
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



    public List<CirculateDetailDTO> filterCirculate(List<CirculateDetailDTO> circulateInfos){
        Date tradeDateStart = getTradeDateStart();
        List<CirculateDetailDTO> list = Lists.newArrayList();
        for (CirculateDetailDTO circulateInfo:circulateInfos){
            if(MarketUtil.isAStocks(circulateInfo.getStockCode())){
                if(circulateInfo.getStockName().contains("ST")||circulateInfo.getStockName().endsWith("退")||circulateInfo.getStockName().startsWith("退")){
                   // System.out.println(circulateInfo.getStockCode()+"==="+circulateInfo.getStockName());
                    continue;
                }
                List<String> disableStocks = Lists.newArrayList("601099", "600777");
                if(disableStocks.contains(circulateInfo.getStockCode())){
                    continue;
                }
                if(MarketUtil.isChuangYe(circulateInfo.getStockCode())){
                    if(circulateInfo.getStockName().startsWith("C")) {
                        Date marketDate = DateUtil.parseDate(circulateInfo.getMarketDate(), DateUtil.yyyyMMdd);
                        if (marketDate.after(tradeDateStart)) {
                            continue;
                        }
                    }
                    if(circulateInfo.getStockName().startsWith("N")) {
                        continue;
                    }

                }
                list.add(circulateInfo);
            }
        }
        return list;
    }


    public Date getTradeDateStart(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        query.setTradeDateTo(new Date());
        query.setLimit(5);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        TradeDatePool tradeDatePool = tradeDatePools.get(4);
        return tradeDatePool.getTradeDate();
    }


    public Map<String,CirculateDetailDTO> convertToMarketInfo(JSONArray jsonArray){
        Map<String,CirculateDetailDTO> map = new HashMap<>();
        for(int i = 0;i<jsonArray.size();i++){
            JSONArray circulateArray = jsonArray.getJSONArray(i);
            String stockCode = circulateArray.get(0).toString();
            stockCode = stockCode.substring(0, 6);
            long totalCirculate = new BigDecimal(circulateArray.get(13).toString()).multiply(new BigDecimal(10000)).longValue();
            long circulateZ = new BigDecimal(circulateArray.get(15).toString()).multiply(new BigDecimal(10000)).longValue();
            CirculateDetailDTO circulateInfo = new CirculateDetailDTO();
            circulateInfo.setStockCode(stockCode);
            circulateInfo.setCirculate(totalCirculate);
            circulateInfo.setCirculateZ(circulateZ);
            map.put(circulateInfo.getStockCode(),circulateInfo);
        }
        return map;
    }

    public List<CirculateDetailDTO> convertToCirculate(JSONArray jsonArray){
        List<CirculateDetailDTO> circulates = Lists.newArrayList();
        for(int i = 0;i<jsonArray.size();i++){
            JSONArray circulateArray = jsonArray.getJSONArray(i);
            String stockCode = circulateArray.get(1).toString();
            String stockName = circulateArray.get(2).toString();
            String marketDate = circulateArray.get(6).toString();
            CirculateDetailDTO circulateInfo = new CirculateDetailDTO();
            circulateInfo.setStockCode(stockCode);
            circulateInfo.setStockName(stockName);
            circulateInfo.setMarketDate(marketDate);
            circulates.add(circulateInfo);
        }
        return circulates;
    }


}
