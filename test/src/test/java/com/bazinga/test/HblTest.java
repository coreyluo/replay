package com.bazinga.test;


import com.bazinga.component.*;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HblTest extends BaseTestCase {

    @Autowired
    private SynExcelComponent synExcelComponent;
    @Autowired
    private OtherBuyStockComponent otherBuyStockComponent;
    @Autowired
    private RealBuyOrSellComponent realBuyOrSellComponent;
    @Test
    public void test(){
        synExcelComponent.otherStockBuy();
        /*BlockLevelDTO preBlockLevel = otherBuyStockComponent.getPreBlockLevel("600476", "20210830");
        System.out.println(preBlockLevel);*/
    }
    @Test
    public void test2(){
        realBuyOrSellComponent.realBuyOrSellMin(DateUtil.parseDate("2021-09-03",DateUtil.yyyy_MM_dd));
    }
}
