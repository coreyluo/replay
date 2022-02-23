package com.bazinga.test;

import com.bazinga.replay.component.HistoryTransactionDataComponent;
import org.jsoup.Connection;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InfluxDbTest extends BaseTestCase {

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Test
    public void test1(){
        historyTransactionDataComponent.getDataFromDB("000001","20220208");
    }
}
