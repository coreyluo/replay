package com.bazinga.test;

import com.bazinga.replay.component.SynInfoComponent;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.service.IndexDetailService;
import com.bazinga.replay.util.TuShareUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SynTest extends BaseTestCase {

    @Autowired
    private SynInfoComponent synInfoComponent;

    @Autowired
    private IndexDetailService indexDetailService;

    @Test
    public void test(){
        try {
            synInfoComponent.synBlockInfo();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    @Test
    public void testThs(){
        try {
            synInfoComponent.synThsBlockInfo();
            synInfoComponent.thsblockIndex();

        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    @Test
    public void testIndexDetail(){
        List<IndexDetail> list = TuShareUtil.getHistoryIndexDetail("000905", "20171001");
        for (IndexDetail indexDetail : list) {
            indexDetailService.save(indexDetail);
        }
    }
}
