package com.bazinga.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import com.bazinga.replay.util.JoinQuantUtil;

import java.io.IOException;


public class MainTest {


    public static void main(String[] args) {

       /* try {
            String token = JoinQuantUtil.getToken();
            System.out.println(token);

           // String result = JoinQuantUtil.getDragonTiger("123048.XSHE", "2021-11-04", token);
            String result = JoinQuantUtil.getTicks("300148.XSHE", "2021-11-04", token);

            String[] array = result.split(" ");
            for (String s : array) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        List<String > huiceList = Lists.newArrayList("600556","603178","603002","603327","600576","603916","605018","000810","002045","002468","002164","002467","002060","002992","002120","600580","603081","600688","000861","600860","600246","600012","002488","600892","603489");
        List<String > onlieList = Lists.newArrayList("600556","000810","002992","600580","002164","002120","603213","002045","603178","600860","002060","605018","600246","000861","002468","600688","002488","002467","603916","603081","603048","600892","603002","600012","603327","603489","600576");
        System.out.println(onlieList.size());
        huiceList.removeAll(onlieList);
        System.out.println(JSONObject.toJSONString(huiceList));
    }
}
