package com.bazinga.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public class MainTest {


    public static void main(String[] args) {
    /*    List<String> list = Lists.newArrayList();

        list.add("1:1");
        list.add("3");
        list.add("2");
        System.out.println(JSONObject.toJSONString(list));
        List<String> sortList = list.stream().sorted(Comparator.comparing(String::toString,(x,y)->{return Integer.parseInt(x) > Integer.parseInt(y)?1:-1;})).collect(Collectors.toList());

        System.out.println(JSONObject.toJSONString(sortList));*/

        Set<String> set = new HashSet<>();
        set.add("09:25");
        set.add("09:56");
        set.add("09:45");
        set.add("09:33");
        TreeSet<String> treeSet = Sets.newTreeSet(set);
        for (String s : treeSet) {
            System.out.println(s);
        }

    }
}
