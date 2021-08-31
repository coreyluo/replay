package com.bazinga.test;


import com.bazinga.component.*;
import com.bazinga.dto.BlockLevelDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HblTest extends BaseTestCase {

    @Autowired
    private SynExcelComponent synExcelComponent;
    @Autowired
    private OtherBuyStockComponent otherBuyStockComponent;
    @Test
    public void test(){
        synExcelComponent.otherStockBuy();
        /*BlockLevelDTO preBlockLevel = otherBuyStockComponent.getPreBlockLevel("600476", "20210830");
        System.out.println(preBlockLevel);*/
    }
}
