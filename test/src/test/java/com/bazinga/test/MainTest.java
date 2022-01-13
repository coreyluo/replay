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

        List<String > huiceList = Lists.newArrayList("002694","002432","600986","001317","603387","605167","605196","603278","002715","002191","605287","600807","603530","002856","000400","002246","601702","603577","000518","002554","603908","600131","002057","603258","002537","001208","603979","603117","002172","002589","002937","603390","601700","000721","002112","002799","002550","002633","000978","002879","605089","002702","603566","002337","605108","000796","002951","000607","600318","600992","603799","600706","605338","603703","603351","002707","603229","603536","001216","603829","600288","002585","603189","605567","603861","002921","002751","002878","600257","002412","603311","002750");
        List<String > onlieList = Lists.newArrayList("002633","601700","002337","002856","002537","002878","002112","600318","603829","002750","002432","002554","000796","000978","002879","002715","001208","000518","605196","603351","603530","603117","603799","603979","002589","002921","002702","000400","600706","002707","600807","001317","605287","603861","002550","605108","002172");
        System.out.println(onlieList.size());
        huiceList.removeAll(onlieList);
        System.out.println(JSONObject.toJSONString(huiceList));
    }
}
