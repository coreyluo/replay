package com.bazinga.test;


import com.bazinga.component.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReplayTest extends BaseTestCase {

    @Autowired
    private BlockHeadReplayComponent blockHeadReplayComponent;

    @Autowired
    private MiddlePlankReplayComponent middlePlankReplayComponent;

    @Autowired
    private MainBlockReplayComponent mainBlockReplayComponent;

    @Autowired
    private PositionOwnReplayComponent positionOwnReplayComponent;

    @Autowired
    private MarketPlankReplayComponent marketPlankReplayComponent;

    @Test
    public void test(){
        blockHeadReplayComponent.invokeStrategy();
    }

    @Test
    public void test2(){
        middlePlankReplayComponent.invokeUnPlank();
    }

    @Test
    public void test3(){
        mainBlockReplayComponent.invokeStrategy();
    }

    @Test
    public void test4(){
        positionOwnReplayComponent.replay();
    }

    @Test
    public void test5(){
        marketPlankReplayComponent.replay();
    }
}
