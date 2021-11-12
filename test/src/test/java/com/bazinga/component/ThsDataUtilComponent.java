package com.bazinga.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.ZhuanZaiExcelDTO;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;
import java.util.List;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsDataUtilComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public void zhuanZaiStocks(List<ZhuanZaiExcelDTO> dataList){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateUtil.parseDate("2021-11-10 15:30:30",DateUtil.DEFAULT_FORMAT));
        query.setTradeDateFrom(DateUtil.parseDate("20210901",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        for(ZhuanZaiExcelDTO zhuanZai:dataList){
            /*if(!zhuanZai.getStockCode().equals("113528")){
                continue;
            }*/
            for(TradeDatePool tradeDatePool:tradeDatePools){
                quoteInfo(zhuanZai.getStockCode(),zhuanZai.getStockName(),DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd));
            }
        }
    }

    public void quoteInfo(String stockCode,String stockName,String tradeDate){
        System.out.println(stockCode+"===="+stockName+"===="+tradeDate);
        int ret = thsLogin();
        String stockKey = getStockKey(stockCode);
        String quote_str = JDIBridge.THS_Snapshot(stockKey,"bid1;bid2;ask1;ask2;totalSellVolume;totalBuyVolume;avgBuyPrice;avgSellPrice;latest;amt;vol;amount;volume;bidSize1;bidSize2;askSize1;askSize2","",tradeDate+" 09:25:00",tradeDate+" 15:10:00");
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
            List<BigDecimal> amounts = tableInfo.getJSONArray("amount").toJavaList(BigDecimal.class);
            List<BigDecimal> amts = tableInfo.getJSONArray("amt").toJavaList(BigDecimal.class);
            List<BigDecimal> volumes = tableInfo.getJSONArray("volume").toJavaList(BigDecimal.class);
            List<BigDecimal> vols = tableInfo.getJSONArray("vol").toJavaList(BigDecimal.class);
            List<BigDecimal> totalSellVolumes = tableInfo.getJSONArray("totalSellVolume").toJavaList(BigDecimal.class);
            List<BigDecimal> totalBuyVolumes = tableInfo.getJSONArray("totalBuyVolume").toJavaList(BigDecimal.class);
            List<BigDecimal> avgSellPrices = tableInfo.getJSONArray("avgSellPrice").toJavaList(BigDecimal.class);
            List<BigDecimal> avgBuyPrices = tableInfo.getJSONArray("avgBuyPrice").toJavaList(BigDecimal.class);
            List<BigDecimal> latests = tableInfo.getJSONArray("latest").toJavaList(BigDecimal.class);
            List<BigDecimal> bid1s = tableInfo.getJSONArray("bid1").toJavaList(BigDecimal.class);
            List<BigDecimal> bid2s = tableInfo.getJSONArray("bid2").toJavaList(BigDecimal.class);
            List<BigDecimal> ask1s = tableInfo.getJSONArray("ask1").toJavaList(BigDecimal.class);
            List<BigDecimal> ask2s = tableInfo.getJSONArray("ask2").toJavaList(BigDecimal.class);
            List<BigDecimal> bidSize1s = tableInfo.getJSONArray("bidSize1").toJavaList(BigDecimal.class);
            List<BigDecimal> bidSize2s = tableInfo.getJSONArray("bidSize2").toJavaList(BigDecimal.class);
            List<BigDecimal> askSize1s = tableInfo.getJSONArray("askSize1").toJavaList(BigDecimal.class);
            List<BigDecimal> askSize2s = tableInfo.getJSONArray("askSize2").toJavaList(BigDecimal.class);
            int i = 0;
            for (String time:times){
                Date date = DateUtil.parseDate(time, DateUtil.DEFAULT_FORMAT);
                ThsQuoteInfo quote = new ThsQuoteInfo();
                quote.setStockCode(stockCode);
                quote.setStockName(stockName);
                quote.setQuoteDate(DateUtil.format(date,DateUtil.yyyyMMdd));
                quote.setQuoteTime(DateUtil.format(date,DateUtil.HHMMSS));
                quote.setCurrentPrice(latests.get(i));
                quote.setBid1(bid1s.get(i));
                quote.setBid2(bid2s.get(i));
                quote.setAsk1(ask1s.get(i));
                quote.setAsk2(ask2s.get(i));
                quote.setBidSize1(bidSize1s.get(i).longValue());
                quote.setBidSize2(bidSize2s.get(i).longValue());
                quote.setAskSize1(askSize1s.get(i).longValue());
                quote.setAskSize2(askSize2s.get(i).longValue());
                quote.setAmt(amts.get(i));
                quote.setAmount(amounts.get(i));
                quote.setVol(vols.get(i).longValue());
                quote.setVolume(volumes.get(i).longValue());
                quote.setTotalSellVolume(totalSellVolumes.get(i).longValue());
                quote.setTotalBuyVolume(totalBuyVolumes.get(i).longValue());
                quote.setAvgBuyPrice(avgBuyPrices.get(i));
                quote.setAvgSellPrice(avgSellPrices.get(i));
                thsQuoteInfoService.save(quote);
                i++;
            }
            JDIBridge.THS_iFinDLogout();
        }
    }
    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("lsyjx002", "334033");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("lsyjx002", "334033");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public static String getStockKey(String stockCode){
        boolean shMarket = MarketUtil.isSHMarket(stockCode);
        if(shMarket){
            return stockCode+".SH";
        }else {
            return stockCode+".SZ";
        }
    }

    public static void main(String[] args) {
        new ThsDataUtilComponent().quoteInfo("113528","","");
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
                ret = JDIBridge.THS_iFinDLogin("lsyjx002", "334033");
                //System.out.println("THS_iFinDLogin ==> ");



                /*String strResultDataSerious = JDIBridge.THS_DateSerial("002233.SZ","ths_open_price_stock;ths_high_price_stock;ths_low_stock;ths_close_price_stock;ths_avg_price_stock;ths_vol_stock;ths_trans_num_stock;ths_amt_stock;ths_macd_stock;ths_kdj_stock;ths_vstd_stock;ths_boll_stock;ths_rsi_stock;ths_ma_stock;ths_sar_stock;ths_wr_stock;ths_cci_stock;ths_obv_stock;ths_vol_w_stock;ths_vol_m_stock","100;100;100;100;100;100;;;26,12,9,100,100,100;9,3,3,100,100,100;10,100;26,2,100,100,100;6,100,100;10,100,100;4,100,100;14,100,100;14,100,100;100,100,100;;","Days:Tradedays,Fill:Previous,Interval:D","2018-05-31","2018-06-15");
                System.out.println("THS_iFinDhis ==> " + strResultDataSerious );*/

               /* String change = JDIBridge.THS_HistoryQuotes("113528.SH", "change", "PriceType:1", "2021-10-29", "2021-11-09");
                System.out.println("quote ==>"+change);*/

                String s    = JDIBridge.THS_Snapshot("127005.SZ","tradeDate;tradeTime;preClose;open;high;low;latest;amt;vol;amount;volume;tradeNum","","2021-11-10 09:15:00","2021-11-10 15:15:00");
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
