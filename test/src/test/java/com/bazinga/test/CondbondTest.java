package com.bazinga.test;

import com.bazinga.replay.component.BondBasicInfoComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CondbondTest extends BaseTestCase {
    @Autowired
    private BondBasicInfoComponent bondBasicInfoComponent;
    @Test
    public void test22(){
        System.out.println("Start");
        bondBasicInfoComponent.calData();

    }

}
