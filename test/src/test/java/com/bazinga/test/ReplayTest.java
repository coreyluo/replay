package com.bazinga.test;


import com.bazinga.component.BlockHeadReplayComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReplayTest extends BaseTestCase {

    @Autowired
    private BlockHeadReplayComponent blockHeadReplayComponent;

    @Test
    public void test(){
        blockHeadReplayComponent.invokeStrategy();
    }
}
