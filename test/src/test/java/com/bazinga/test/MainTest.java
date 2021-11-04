package com.bazinga.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainTest {


    public static void main(String[] args) throws IOException {

            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("method","get_billboard_list");
            // 获取当前token
           // paramMap.put("mob","13588205347");
            // 标的类型
          //  paramMap.put("pwd","Lily5200!");
            paramMap.put("stock_list","None");
            paramMap.put("start_date","None");
            paramMap.put("end_date","2021-11-03");
            paramMap.put("count","1");

            String result = Jsoup.connect("https://dataapi.joinquant.com/apis").ignoreContentType(true)
                    .header("Content-Type", "application/json")
                    .requestBody(JSONObject.toJSONString(paramMap)).post().text();
            System.out.println(result);




    }
}
