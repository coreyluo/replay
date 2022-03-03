package com.bazinga.replay.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TuShareUtil {

    private static final int  LOOP_TIMES =3;


    public static Map<String, AdjFactorDTO> getHistoryIndexDetail(String stockCode, String kbarDateFrom) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "index_weight");
        paramMap.put("token", "f9d25f4ab3f0abe5e04fdf76c32e8c8a5cc94e384774da025098ec6e");
        Map<String, String> paramsdate = new HashMap<>();
        String tsStockCode = stockCode + ".SH";
        paramsdate.put("index_code", tsStockCode);
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

    public static void main(String[] args) {
        getHistoryIndexDetail("000905","20171001");
    }
}
