package com.bazinga.test;


import com.bazinga.component.BlockHeadReplayComponent;
import com.bazinga.component.MainBlockReplayComponent;
import com.bazinga.component.MiddlePlankReplayComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReplayTest extends BaseTestCase {

    @Autowired
    private BlockHeadReplayComponent blockHeadReplayComponent;

    @Autowired
    private MiddlePlankReplayComponent middlePlankReplayComponent;

    @Autowired
    private MainBlockReplayComponent mainBlockReplayComponent;

    @Test
    public void test(){
        blockHeadReplayComponent.invokeStrategy();
    }

    @Test
    public void test2(){
        middlePlankReplayComponent.invoke();
    }

    @Test
    public void test3(){
        mainBlockReplayComponent.invokeStrategy();
    }
}
