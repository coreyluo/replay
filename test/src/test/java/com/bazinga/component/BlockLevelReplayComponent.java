package com.bazinga.component;


import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.OtherExcelDTO;
import com.bazinga.dto.StockRateDTO;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.google.common.collect.Lists;
import com.tradex.util.Conf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockLevelReplayComponent {


    @Autowired
    private ThsBlockInfoService thsBlockInfoService;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    public Map<String, BlockLevelDTO> calBlockLevelDTO(List<StockRateDTO> stockRates) {
        Map<String, BlockLevelDTO> levelMap = new HashMap<>();
        List<BlockLevelDTO> list = Lists.newArrayList();
        if(CollectionUtils.isEmpty(stockRates)){
            return levelMap;
        }
        Map<String, StockRateDTO> map = new HashMap<>();
        for (StockRateDTO stockRateDTO:stockRates){
            map.put(stockRateDTO.getStockCode(),stockRateDTO);
        }
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            ThsBlockStockDetailQuery thsBlockStockDetailQuery = new ThsBlockStockDetailQuery();
            thsBlockStockDetailQuery.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(thsBlockStockDetailQuery);
            if(CollectionUtils.isEmpty(details)){
                continue;
            }
            if(details.size()<=20){
                continue;
            }
            BlockLevelDTO blockLevelDTO = new BlockLevelDTO();
            blockLevelDTO.setBlockCode(thsBlockInfo.getBlockCode());
            blockLevelDTO.setBlockName(thsBlockInfo.getBlockName());
            int i = 0;
            BigDecimal rate = null;
            for (ThsBlockStockDetail detail:details){
                StockRateDTO stockRateDTO = map.get(detail.getStockCode());
                if(stockRateDTO!=null&&stockRateDTO.getRate()!=null){
                    i++;
                    if(rate==null){
                        rate = stockRateDTO.getRate();
                    }else {
                        rate = rate.add(stockRateDTO.getRate());
                    }
                }
            }
            if(rate!=null){
                BigDecimal avgRate = rate.divide(new BigDecimal(i), 4, BigDecimal.ROUND_HALF_UP);
                blockLevelDTO.setAvgRate(avgRate);
            }
            list.add(blockLevelDTO);
        }
        Collections.sort(list);

        int i = 0;
        for (BlockLevelDTO blockLevelDTO:list){
            i++;
            blockLevelDTO.setLevel(i);
            levelMap.put(blockLevelDTO.getBlockCode(),blockLevelDTO);
        }
        return levelMap;
    }

    public BlockLevelDTO getBlockLevel(Map<String, BlockLevelDTO> map,String stockCode){
        ThsBlockStockDetailQuery thsBlockStockDetailQuery = new ThsBlockStockDetailQuery();
        thsBlockStockDetailQuery.setStockCode(stockCode);
        List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(thsBlockStockDetailQuery);
        if(CollectionUtils.isEmpty(thsBlockStockDetails)){
            return null;
        }
        BlockLevelDTO levelDTO = null;
        for (ThsBlockStockDetail detail:thsBlockStockDetails){
            BlockLevelDTO blockLevelDTO = map.get(detail.getBlockCode());
            if(blockLevelDTO==null || blockLevelDTO.getLevel()==null){
                continue;
            }
            if(levelDTO==null||blockLevelDTO.getLevel()<levelDTO.getLevel()){
                levelDTO = blockLevelDTO;
            }
        }
        return levelDTO;
    }

    public BlockLevelDTO userBlockCodeBlockLevel(Map<String, BlockLevelDTO> map,String blockCode){
        if(CollectionUtils.isEmpty(map)||blockCode==null){
            return null;
        }else{
            BlockLevelDTO blockLevelDTO = map.get(blockCode);
            return blockLevelDTO;
        }
    }


}
