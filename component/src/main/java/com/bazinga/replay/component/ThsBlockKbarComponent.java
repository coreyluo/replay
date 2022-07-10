package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
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
import sun.misc.Cache;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsBlockKbarComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private StockIndexService stockIndexService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public List<HistoryBlockStocks> initHistoryBlockStocks(String blockCode,String blockName){
        int ret = thsLogin();
        List<BlockStockDTO> blockStockDTOS = getBlockStocks(blockCode);
        thsLoginOut();
        List<HistoryBlockStocks> historys = converToHistoryBlockStocks(blockCode, blockName, blockStockDTOS);
        return historys;

    }
    public List<HistoryBlockStocks> converToHistoryBlockStocks(String blockCode,String blockName,List<BlockStockDTO> list){
        List<HistoryBlockStocks> historys = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return historys;
        }
        Map<String,List<String>> inMap = new HashMap<>();
        Map<String,List<String>> outMap = new HashMap<>();
        for (BlockStockDTO blockStockDTO:list){
            if(blockStockDTO.getStatus()==0){
                List<String> stocks = inMap.get(blockStockDTO.getMarketDate());
                if(stocks==null){
                    stocks = new ArrayList<>();
                    inMap.put(blockStockDTO.getMarketDate(),stocks);
                }
                stocks.add(blockStockDTO.getStockCode());
            }
            if(blockStockDTO.getStatus()==1){
                List<String> stocks = outMap.get(blockStockDTO.getMarketDate());
                if(stocks==null){
                    stocks = new ArrayList<>();
                    outMap.put(blockStockDTO.getMarketDate(),stocks);
                }
                stocks.add(blockStockDTO.getStockCode());
            }
        }
        Date fisrtDate = DateUtil.parseDate("20170101", DateUtil.yyyyMMdd);
        Date endDate = DateTimeUtils.getDate000000(new Date());
        Date date = DateUtil.parseDate("19910101", DateUtil.yyyyMMdd);
        List<String> reals = Lists.newArrayList();
        for (int i=0; i<=20000;i++){
            date = DateUtil.addDays(date,1);
            String dateStr = DateUtil.format(date, DateUtil.yyyyMMdd);
            List<String> ins = inMap.get(dateStr);
            List<String> outs = outMap.get(dateStr);
            if(!CollectionUtils.isEmpty(ins)) {
                for (String inStock : ins) {
                    if (!reals.contains(inStock)) {
                        reals.add(inStock);
                    }
                }
            }
            if(!CollectionUtils.isEmpty(outs)) {
                for (String outStock : outs) {
                    if (reals.contains(outStock)) {
                        reals.remove(outStock);
                    }

                }
            }
            String str = null;
            if(reals.size()>0){
                for (String stockCode:reals){
                    if(str == null){
                        str = stockCode;
                    }else{
                        str = str+","+stockCode;
                    }
                }
            }
            if(str!=null){
                HistoryBlockStocks historyBlockStocks = new HistoryBlockStocks();
                historyBlockStocks.setBlockCode(blockCode);
                historyBlockStocks.setBlockName(blockName);
                historyBlockStocks.setCreateTime(new Date());
                historyBlockStocks.setStocks(str);
                historyBlockStocks.setTradeDate(dateStr);
                if(date.before(fisrtDate)||date.after(endDate)) {
                    continue;
                }else {
                    historys.add(historyBlockStocks);
                }
            }
        }
        return historys;
    }

    public List<BlockStockDTO> getBlockStocks(String blockCode){
        ArrayList<BlockStockDTO> list = Lists.newArrayList();
        String quote_str = JDIBridge.THS_DataPool("indexConsRecord",blockCode+".TI;1990-02-01;2022-05-19;全部","date:Y,thscode:Y,securityName:Y,status:Y");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return list;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            JSONArray dateJson = tableInfo.getJSONArray("date");
            if(dateJson==null||dateJson.size()==0){
                return list;
            }
            List<String> dates = tableInfo.getJSONArray("date").toJavaList(String.class);
            List<String> stockCodes = tableInfo.getJSONArray("thscode").toJavaList(String.class);
            List<String> names = tableInfo.getJSONArray("securityName").toJavaList(String.class);
            List<String> types = tableInfo.getJSONArray("status").toJavaList(String.class);
            int i = 0;
            for (String time:dates){
                String stockCode = MarketUtil.thsToGeneralStock(stockCodes.get(i));
                BlockStockDTO blockStock = new BlockStockDTO();
                blockStock.setMarketDate(time);
                blockStock.setStockCode(stockCode);
                blockStock.setStockName(names.get(i));
                if(types.get(i).equals("剔除")) {
                    blockStock.setStatus(1);
                }else{
                    blockStock.setStatus(0);
                }
                list.add(blockStock);
                i++;
            }
        }
        return list;
    }

    public void initHistoryBlockKbars(){
        int ret = thsLogin();
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            getBlockKbar(historyBlockInfo.getBlockCode(),historyBlockInfo.getBlockName());
        }
        thsLoginOut();
    }

    public void getBlockKbar(String blockCode,String blockName){
        System.out.println(blockCode);
        String quote_str = JDIBridge.THS_HistoryQuotes(blockCode+".TI","open,high,low,close","","2017-06-01","2022-06-09");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONArray timeArray = tableJson.getJSONArray("time");
            if(timeArray==null||timeArray.size()==0){
                return;
            }
            List<String> times = timeArray.toJavaList(String.class);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> opens = tableInfo.getJSONArray("open").toJavaList(BigDecimal.class);
            List<BigDecimal> highs = tableInfo.getJSONArray("high").toJavaList(BigDecimal.class);
            List<BigDecimal> lows = tableInfo.getJSONArray("low").toJavaList(BigDecimal.class);
            List<BigDecimal> closes = tableInfo.getJSONArray("close").toJavaList(BigDecimal.class);
            int i = 0;
            for (String time:times){
                Date timeDate = DateUtil.parseDate(time, DateUtil.yyyy_MM_dd);
                StockKbar stockKbar = new StockKbar();
                stockKbar.setStockCode(blockCode);
                stockKbar.setStockName(blockName);
                stockKbar.setKbarDate(DateUtil.format(timeDate, DateUtil.yyyyMMdd));
                stockKbar.setUniqueKey(stockKbar.getStockCode() + "_" + stockKbar.getKbarDate());
                stockKbar.setOpenPrice(opens.get(i));
                stockKbar.setClosePrice(closes.get(i));
                stockKbar.setHighPrice(highs.get(i));
                stockKbar.setLowPrice(lows.get(i));
                stockKbar.setTradeAmount(BigDecimal.ZERO);
                stockKbar.setTradeQuantity(0l);
                stockKbarService.save(stockKbar);
                i++;
            }
        }

    }

    public void initHistoryBlockMinKbar() {
        int ret = thsLogin();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            if(!historyBlockInfo.getBlockCode().startsWith("881124")){
                continue;
            }
            //THREAD_POOL_QUOTE.execute(() ->{
                /*StockKbarQuery kbarQuery = new StockKbarQuery();
                kbarQuery.setStockCode(historyBlockInfo.getBlockCode());
                List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
                if (!CollectionUtils.isEmpty(stockKbars)) {
                    continue;
                }*/
                String marketDateStr = historyBlockInfo.getMarketDate();
                Date marketDate = DateUtil.parseDate(marketDateStr, DateUtil.yyyyMMdd);
                for (TradeDatePool tradeDatePool:tradeDatePools){
                    Date tradeDate = DateTimeUtils.getDate000000(tradeDatePool.getTradeDate());
                    if (tradeDate.before(marketDate)||tradeDate.before(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd))) {
                        continue;
                    }
                    String tradeDateyyyy_MM_dd = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
                    if(tradeDateyyyy_MM_dd.equals("2022-01-10")) {
                        try {
                            getBlockMinKbar(historyBlockInfo.getBlockCode(), historyBlockInfo.getBlockName(), tradeDateyyyy_MM_dd);
                        } catch (Exception e) {
                            log.info(e.getMessage(), e);
                        }
                    }
                }
            //});
        }

        /*try {
            Thread.sleep(10000000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        thsLoginOut();
    }


    public void getBlockMinKbar(String blockCode,String blockName,String tradeDate){
        System.out.println(blockCode+"===="+tradeDate);
        String quote_str = JDIBridge.THS_HighFrequenceSequence(blockCode+".TI","open;high;low;close;volume;amount","Fill:Original",tradeDate+" 09:15:00",tradeDate+" 15:00:00");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONArray timeArray = tableJson.getJSONArray("time");
            if(timeArray==null||timeArray.size()==0){
                return;
            }
            List<String> times = timeArray.toJavaList(String.class);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> opens = tableInfo.getJSONArray("open").toJavaList(BigDecimal.class);
            List<BigDecimal> highs = tableInfo.getJSONArray("high").toJavaList(BigDecimal.class);
            List<BigDecimal> lows = tableInfo.getJSONArray("low").toJavaList(BigDecimal.class);
            List<BigDecimal> closes = tableInfo.getJSONArray("close").toJavaList(BigDecimal.class);
            List<BigDecimal> amounts = tableInfo.getJSONArray("amount").toJavaList(BigDecimal.class);
            List<Long> volumes = tableInfo.getJSONArray("volume").toJavaList(Long.class);

            int i = 0;
            for (String time:times){
                Date timeDate = DateUtil.parseDate(time, DateUtil.noSecondFormat);
                StockKbar stockKbar = new StockKbar();
                stockKbar.setStockCode(blockCode);
                stockKbar.setStockName(blockName);
                stockKbar.setKbarDate(DateUtil.format(timeDate, DateUtil.yyyyMMddHHmmss));
                stockKbar.setUniqueKey(stockKbar.getStockCode() + "_" + stockKbar.getKbarDate());
                stockKbar.setOpenPrice(opens.get(i));
                stockKbar.setClosePrice(closes.get(i));
                stockKbar.setHighPrice(highs.get(i));
                stockKbar.setLowPrice(lows.get(i));
                stockKbar.setTradeAmount(amounts.get(i));
                stockKbar.setTradeQuantity(volumes.get(i));
                StockKbar byUniqueKey = stockKbarService.getByUniqueKey(stockKbar.getUniqueKey());
                if(byUniqueKey==null) {
                    stockKbarService.save(stockKbar);
                }
                i++;
            }
        }

    }
    public void initHistoryBlockIndex() {
        int ret = thsLogin();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            THREAD_POOL_QUOTE.execute(() ->{
                String marketDateStr = historyBlockInfo.getMarketDate();
                Date marketDate = DateUtil.parseDate(marketDateStr, DateUtil.yyyyMMdd);
                for (TradeDatePool tradeDatePool:tradeDatePools){
                    Date tradeDate = DateTimeUtils.getDate000000(tradeDatePool.getTradeDate());
                    if (tradeDate.before(marketDate)||tradeDate.before(DateUtil.parseDate("20171201",DateUtil.yyyyMMdd))) {
                        continue;
                    }
                    String tradeDateyyyyMMdd = DateUtil.format(tradeDate, DateUtil.yyyyMMdd);
                    String tradeDateyyyy_MM_dd = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
                    String uk = historyBlockInfo.getBlockCode()+"_"+tradeDateyyyyMMdd;
                    StockIndex byUniqueKey = stockIndexService.getByUniqueKey(uk);
                    if (byUniqueKey != null) {
                        continue;
                    }
                    try {
                        StockIndex stockIndex = new StockIndex();
                        stockIndex.setStockCode(historyBlockInfo.getBlockCode());
                        stockIndex.setStockName(historyBlockInfo.getBlockName());
                        stockIndex.setKbarDate(tradeDateyyyyMMdd);
                        stockIndex.setUniqueKey(stockIndex.getStockCode() + "_" + stockIndex.getKbarDate());
                        getStockIndex(historyBlockInfo.getBlockCode(), historyBlockInfo.getBlockName(), tradeDateyyyy_MM_dd, stockIndex);
                        stockIndexService.save(stockIndex);
                    } catch (Exception e) {
                        log.info(e.getMessage(), e);
                    }
                }
            });
        }

        try {
            Thread.sleep(10000000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thsLoginOut();
    }

    public void getStockIndex(String blockCode,String blockName,String tradeDate,StockIndex stockIndex){
        System.out.println(blockCode+"_"+blockName+"_"+tradeDate);
        String quote_str = JDIBridge.THS_BasicData(blockCode+".TI", "ths_bias_index;ths_macd_index", tradeDate+",6,100;"+tradeDate+",26,12,9,102,100");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            List<BigDecimal> macds = tableInfo.getJSONArray("ths_macd_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            BigDecimal macd = macds.get(0);
            stockIndex.setBias6(bias);
            stockIndex.setMacd(macd);
        }
        String quote_str1 = JDIBridge.THS_BasicData(blockCode+".TI", "ths_bias_index", tradeDate+",12,100");
        if(!StringUtils.isEmpty(quote_str1)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str1);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            stockIndex.setBias12(bias);
        }

        String quote_str2 = JDIBridge.THS_BasicData(blockCode+".TI", "ths_bias_index", tradeDate+",24,100");
        if(!StringUtils.isEmpty(quote_str2)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str2);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            stockIndex.setBias24(bias);
        }

    }


    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz198", "307435");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz198", "307435");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }


    public static void main(String[] args) {
        //new ThsDataComponent().quoteInfo("600207","","");
        System.out.println(System.getProperty("java.library.path"));
        System.load("E://iFinDJava.dll");

        int ret = -1;
        if (args.length > 0) {
            System.out.println("login with cn account");
        }
        else {
            System.out.println("login with en account");
        }

        int a = 0;
        if (ret != 1) {
            while(true)
            {
               // System.out.print(++a);
                ret = JDIBridge.THS_iFinDLogin("ylz203", "182883");
                //System.out.println("THS_iFinDLogin ==> ");



                /*String strResultDataSerious = JDIBridge.THS_DateSerial("002233.SZ","ths_open_price_stock;ths_high_price_stock;ths_low_stock;ths_close_price_stock;ths_avg_price_stock;ths_vol_stock;ths_trans_num_stock;ths_amt_stock;ths_macd_stock;ths_kdj_stock;ths_vstd_stock;ths_boll_stock;ths_rsi_stock;ths_ma_stock;ths_sar_stock;ths_wr_stock;ths_cci_stock;ths_obv_stock;ths_vol_w_stock;ths_vol_m_stock","100;100;100;100;100;100;;;26,12,9,100,100,100;9,3,3,100,100,100;10,100;26,2,100,100,100;6,100,100;10,100,100;4,100,100;14,100,100;14,100,100;100,100,100;;","Days:Tradedays,Fill:Previous,Interval:D","2018-05-31","2018-06-15");
                System.out.println("THS_iFinDhis ==> " + strResultDataSerious );*/

               /* String change = JDIBridge.THS_HistoryQuotes("113528.SH", "change", "PriceType:1", "2021-10-29", "2021-11-09");
                System.out.println("quote ==>"+change);*/

                String s    = JDIBridge.THS_HistoryQuotes("000001.SZ","open,high,low,close","","2022-05-18","2022-05-19");
                System.out.println(s);

                JDIBridge.THS_iFinDLogout();
                System.out.println("THS_iFinDLogout ==> ");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.out.println("Login failed == > " + ret);
        }
    }

}
