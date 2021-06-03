package com.bazinga.test;

import com.bazinga.replay.component.SynInfoComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
public class SynTest extends BaseTestCase {

    @Autowired
    private SynInfoComponent synInfoComponent;

    @Test
    public void test(){
        try {
            synInfoComponent.synBlockInfo();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }
}