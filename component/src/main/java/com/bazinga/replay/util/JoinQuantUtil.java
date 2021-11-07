package com.bazinga.replay.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinQuantUtil {

    public static String getToken() throws IOException {


        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("method","get_token");
        // 获取当前token
        paramMap.put("mob","13588205347");
        // 标的类型
        paramMap.put("pwd","Lily5200!");


        String result = Jsoup.connect("https://dataapi.joinquant.com/apis").ignoreContentType(true)
                .header("Content-Type", "application/json")
                .requestBody(JSONObject.toJSONString(paramMap)).post().text();

       return result;
    }

    public static String getAllSecurities(String token) throws IOException {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("method","get_all_securities");
        paramMap.put("token",token);
        paramMap.put("date","2021-11-04");
        paramMap.put("code","stock");
        return Jsoup.connect("https://dataapi.joinquant.com/apis").ignoreContentType(true)
                .header("Content-Type", "application/json")
                .requestBody(JSONObject.toJSONString(paramMap)).post().text();
    }

    public static String getDragonTiger(String code, String date,String token) throws IOException {

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("method","get_billboard_list");
        paramMap.put("token",token);
        paramMap.put("code",code);
        paramMap.put("date",date);
       // paramMap.put("end_date","2021-11-04");
        return Jsoup.connect("https://dataapi.joinquant.com/apis").ignoreContentType(true)
                .header("Content-Type", "application/json")
                .requestBody(JSONObject.toJSONString(paramMap)).post().text();
    }

}
