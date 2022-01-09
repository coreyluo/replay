package com.bazinga.test;

import com.bazinga.replay.component.AccountPositionCalComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DailyTest extends BaseTestCase{

    @Autowired
    private AccountPositionCalComponent accountPositionCalComponent;

    @Test
    public void test(){
      /*  accountPositionCalComponent.cal("398000086400");
        accountPositionCalComponent.cal("398000102550");
        accountPositionCalComponent.cal("398000104348");*/
       // accountPositionCalComponent.cal("398000103912");
        accountPositionCalComponent.cal("398000131333");
     //   accountPositionCalComponent.cal("398000104352");
    }


}
