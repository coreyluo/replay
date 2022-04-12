package com.bazinga.component;


import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.query.BlockInfoQuery;
import com.bazinga.replay.query.BlockStockDetailQuery;
import com.bazinga.replay.service.BlockInfoService;
import com.bazinga.replay.service.BlockStockDetailService;
import com.bazinga.replay.service.StockKbarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BlockOpenReplayComponent {

    @Autowired
    private BlockInfoService blockInfoService;

    @Autowired
    private BlockStockDetailService blockStockDetailService;

    @Autowired
    private StockKbarService stockKbarService;


    public void replay(){
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        blockInfos =  blockInfos.stream().filter(item-> item.getBlockCode().startsWith("8803") || item.getBlockCode().startsWith("8804")).collect(Collectors.toList());
        Map<String,List<String>> blockDetailMap = new HashMap<>();
        for (BlockInfo blockInfo : blockInfos) {
            BlockStockDetailQuery detailQuery = new BlockStockDetailQuery();
            detailQuery.setBlockCode(blockInfo.getBlockCode());
            List<BlockStockDetail> blockStockDetails = blockStockDetailService.listByCondition(detailQuery);
            blockDetailMap.put(blockInfo.getBlockCode(),blockStockDetails.stream().map(BlockStockDetail::getStockCode).collect(Collectors.toList()));
        }






    }



}
