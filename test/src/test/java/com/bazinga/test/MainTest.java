package com.bazinga.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import com.bazinga.replay.util.JoinQuantUtil;

import java.io.IOException;

import static com.bazinga.replay.component.StockKbarComponent.getAdjFactorMap;


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
        Map<String, AdjFactorDTO> adjFactorMap = getAdjFactorMap("002677", "20211201");


        String[] strarr = new String[]{
                "301235","300199","300149","002083","301137","300030","000651","001218","300660","300939","600211","300829","605056","603699","600771","605086","603327","600628","000065","000582","000547","300673","300906","301019","603629","603882","301100","603324","300003","600833","300317","605016","600237","603338","000504","600536","000567","301201","603822","300630","603600","603041","300995","300971","300065","603968","300965","002489","300603","300585","300659","300487","002484","600735","301127","300950","003030","003007","300143","301052","300432","002095","603712","301090","600529","300537","300994","002813","300702","300820","300170","000553","301117","002025","002917","300117","002762","600661","300584","605567","603583","002349","300956","001267","002551","301155","603648","603511","300204","301169","301061","002882","600079","600378","301110","600982","603938","000665","300729","600857","603209","600448","600346","003037","300952","300479","603558","001234","300844","300411","301001","603868","001317","301130","601116","002693","600228","300013","003010"
        };
        System.out.println(strarr.length);

        List<String > huiceList = Lists.newArrayList("002607","000779","002775","002309","002633","002528","002708","002116","000151","000017","000609","002871","000635","002780","002518","000985","002269","002887","002642","000909","002811","002305","603767","603628","603032","601968","600841","600725","600291","600148");
        List<String > onlieList = Lists.newArrayList("002607","002775","002309","002633","002528","002708","002116","000151","000017","000609","002871","000635","002780","002518","000985","002269","002887","002642","000909","002811","002305","603767","603628","603032","601968","600841","600725","600291","600148");
        System.out.println(huiceList.size());
        huiceList.removeAll(onlieList);
        System.out.println(JSONObject.toJSONString(huiceList));
    }
}
