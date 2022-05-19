package com.bazinga.replay.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.model.StockKbar;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TuShareUtil {

    private static final int  LOOP_TIMES =3;


    public static List<IndexDetail> getHistoryIndexDetail(String stockCode, String kbarDateFrom) {
        List<IndexDetail> resultList = Lists.newArrayList();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "index_weight");
        paramMap.put("token", "f9d25f4ab3f0abe5e04fdf76c32e8c8a5cc94e384774da025098ec6e");
        Map<String, String> paramsdate = new HashMap<>();
        String tsStockCode = stockCode + ".SH";
        paramsdate.put("index_code", tsStockCode);
        paramsdate.put("limit", "50000");
        if (StringUtils.isNotBlank(kbarDateFrom)) {
            paramsdate.put("start_date", kbarDateFrom);
        }
        paramMap.put("params", paramsdate);
        paramMap.put("fields", "con_code,trade_date");
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
                    String resultStockCode = fields.getJSONArray(i).getString(0);
                    String tradeDate = fields.getJSONArray(i).getString(1);
                    IndexDetail indexDetail = new IndexDetail();
                    indexDetail.setIndexCode(stockCode);
                    indexDetail.setBlockName("中证500");
                    indexDetail.setStockCode(resultStockCode.substring(0,6));
                    indexDetail.setKbarDate(tradeDate);
                    resultList.add(indexDetail);
                }
                return resultList;
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

        return resultList;
    }


    public static List<StockKbar> getGlobalIndexKbar(String tsStockCode, String indexName, String kbarDateFrom) {
        List<StockKbar> resultList = Lists.newArrayList();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "index_global");
        paramMap.put("token", "f9d25f4ab3f0abe5e04fdf76c32e8c8a5cc94e384774da025098ec6e");
        Map<String, String> paramsdate = new HashMap<>();
        paramsdate.put("ts_code", tsStockCode);
        paramsdate.put("limit", "50000");
        if (StringUtils.isNotBlank(kbarDateFrom)) {
            paramsdate.put("start_date", kbarDateFrom);
        }
        paramMap.put("params", paramsdate);
        paramMap.put("fields", "ts_code,trade_date,open,close,high,low,vol,amount");
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
                for (int i = 0; i < fields.size(); i++) {
                    String resultStockCode = fields.getJSONArray(i).getString(0);
                    String tradeDate = fields.getJSONArray(i).getString(1);
                    StockKbar stockKbar = new StockKbar();
                    stockKbar.setStockCode(tsStockCode);
                    stockKbar.setStockName(indexName);
                    stockKbar.setKbarDate(tradeDate);
                    stockKbar.setOpenPrice(new BigDecimal(fields.getJSONArray(i).getString(2)));
                    stockKbar.setClosePrice(new BigDecimal(fields.getJSONArray(i).getString(3)));
                    stockKbar.setHighPrice(new BigDecimal(fields.getJSONArray(i).getString(4)));
                    stockKbar.setLowPrice(new BigDecimal(fields.getJSONArray(i).getString(5)));
                    stockKbar.setAdjFactor(new BigDecimal("1"));
                    stockKbar.setAdjOpenPrice(stockKbar.getOpenPrice());
                    stockKbar.setAdjClosePrice(stockKbar.getClosePrice());
                    stockKbar.setAdjHighPrice(stockKbar.getHighPrice());
                    stockKbar.setAdjLowPrice(stockKbar.getLowPrice());
                    stockKbar.setUniqueKey(stockKbar.getStockCode()+ SymbolConstants.UNDERLINE + tradeDate);
                    String volStr = fields.getJSONArray(i).getString(6);
                    if(volStr==null){
                        stockKbar.setTradeQuantity(0L);
                    }else {
                        stockKbar.setTradeQuantity(Double.valueOf(volStr).longValue());
                    }
                    stockKbar.setTradeAmount(new BigDecimal("-1"));
                    resultList.add(stockKbar);
                }
                return resultList;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}", times,tsStockCode, e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return resultList;
    }

    public static void main(String[] args) {
        //getHistoryIndexDetail("000905","20171001");
        getGlobalIndexKbar("DJI","道琼斯","20220101");
    }
}
