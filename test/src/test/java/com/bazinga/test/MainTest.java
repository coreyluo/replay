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

        try {
            String token = JoinQuantUtil.getToken();
            System.out.println(token);

            String dragonTiger = JoinQuantUtil.getDragonTiger("300745.XSHE", "2021-11-04", token);
            //System.out.println(dragonTiger);
           // String allSecurities = JoinQuantUtil.getAllSecurities(token);
            String[] array = dragonTiger.split(" ");
            for (String s : array) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
