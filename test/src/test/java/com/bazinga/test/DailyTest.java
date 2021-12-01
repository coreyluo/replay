package com.bazinga.test;

import com.bazinga.replay.component.AccountPositionCalComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DailyTest extends BaseTestCase{

    @Autowired
    private AccountPositionCalComponent accountPositionCalComponent;

    @Test
    public void test(){
        accountPositionCalComponent.cal();
    }


}
