package com.bazinga.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.ZhuanZaiExcelDTO;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.SortUtil;
import com.bazinga.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsDataComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void zhuanZaiStocks(){
        int ret = thsLogin();
        quoteInfo();
        thsLoginOut();

    }

    public void quoteInfo(){
        String quote_str = JDIBridge.THS_Snapshot("301088.SZ","bid1;bid2;ask1;bidSize1;bidSize2;askSize1;amt;tradeTime;tradeDate;latest","","2022-04-21 09:15:00","2022-04-21 09:29:00");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            System.out.println(JSONObject.toJSONString(jsonObject));
        }
    }
    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz200", "620865");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz200", "620865");
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
