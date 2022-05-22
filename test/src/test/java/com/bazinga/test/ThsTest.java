package com.bazinga.test;

import com.bazinga.replay.component.*;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * 单元测试基类<p/>
 * 其他单元测试都继承本类
 *
 * @author zixiao
 * @date 2016/5/20
 */
@ContextConfiguration(locations="classpath:/META-INF/spring/replay-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ThsTest {
    @Autowired
    private BigAmountTestComponent bigAmountTestComponent;


    @Test
    public void test1() {
        bigAmountTestComponent.plankExchangeAmountInfo();
    }


}
