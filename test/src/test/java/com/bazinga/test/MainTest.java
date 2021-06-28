package com.bazinga.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainTest {


    public static void main(String[] args) {
        List<String> list = Lists.newArrayList();

        list.add("1:1");
        list.add("3");
        list.add("2");
        System.out.println(JSONObject.toJSONString(list));
        List<String> sortList = list.stream().sorted(Comparator.comparing(String::toString,(x,y)->{return Integer.parseInt(x) > Integer.parseInt(y)?1:-1;})).collect(Collectors.toList());

        System.out.println(JSONObject.toJSONString(sortList));



    }
}
