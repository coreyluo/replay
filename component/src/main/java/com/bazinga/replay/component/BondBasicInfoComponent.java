package com.bazinga.replay.component;


import com.bazinga.base.Sort;
import com.bazinga.replay.model.BlockKbarSelf;
import com.bazinga.replay.model.BondBasicInfo;
import com.bazinga.replay.query.BlockKbarSelfQuery;
import com.bazinga.replay.query.BondBasicInfoQuery;
import com.bazinga.replay.service.BlockKbarSelfService;
import com.bazinga.replay.service.BondBasicInfoService;
import com.bazinga.replay.service.impl.BondBasicInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
public class BondBasicInfoComponent {
    @Autowired
    private BondBasicInfoService bondBasicInfoService  ;

    @Autowired
    private BlockKbarSelfService blockKbarSelfService;

    public void calData(){
        System.out.println("strat");
        BondBasicInfoQuery bbiq=new BondBasicInfoQuery();
//        BlockKbarSelfQuery blockKbarSelfQuery = new BlockKbarSelfQuery();
//        blockKbarSelfQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
//        List<BlockKbarSelf> blockKbars = blockKbarSelfService.listByCondition(blockKbarSelfQuery);
//        System.out.println(blockKbars);
        List<BondBasicInfo> list= bondBasicInfoService.listByCondition(bbiq);
        System.out.println(list);
    }
}

