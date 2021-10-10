package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.dto.StockKbarSumInfoDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HotBlockDropInfoComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockCommonReplayService stockCommonReplayService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private ThsBlockKbarService thsBlockKbarService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    public void thsBlockKbar(Date date){
        List<ThsBlockKbar> hotBlockDrops = hotBlockDrop(date);
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo blockInfo:thsBlockInfos){

            ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
            detailQuery.setBlockCode(blockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
            if(CollectionUtils.isEmpty(details)||details.size()<10){
                continue;
            }

        }

    }
    public List<ThsBlockKbar> hotBlockDrop(Date date){
        List<ThsBlockKbar> list = Lists.newArrayList();
        Map<String,ThsBlockKbar> hotBlockMap = new HashMap<>();
        Date preTradeDate = commonComponent.preTradeDate(date);
        Date prePreTradeDate = commonComponent.preTradeDate(preTradeDate);
        String dateStr = DateUtil.format(date, DateUtil.yyyyMMdd);
        String preDateStr = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
        String prePreDateStr = DateUtil.format(prePreTradeDate, DateUtil.yyyyMMdd);
        ThsBlockKbarQuery preQuery = new ThsBlockKbarQuery();
        preQuery.addOrderBy("close_rate", Sort.SortType.DESC);
        preQuery.setTradeDate(preDateStr);
        List<ThsBlockKbar> preBlockKBars= thsBlockKbarService.listByCondition(preQuery);
        if(!CollectionUtils.isEmpty(preBlockKBars)){
            int i = 0;
            for (ThsBlockKbar thsBlockKbar:preBlockKBars){
                i++;
                if(i<=3){
                    hotBlockMap.put(thsBlockKbar.getBlockCode(),thsBlockKbar);
                }
            }
        }

        ThsBlockKbarQuery prePreQuery = new ThsBlockKbarQuery();
        prePreQuery.addOrderBy("close_rate", Sort.SortType.DESC);
        prePreQuery.setTradeDate(prePreDateStr);
        List<ThsBlockKbar> prePreBlockKBars= thsBlockKbarService.listByCondition(prePreQuery);
        if(!CollectionUtils.isEmpty(prePreBlockKBars)){
            int i = 0;
            for (ThsBlockKbar thsBlockKbar:prePreBlockKBars){
                i++;
                if(i<=3){
                    hotBlockMap.put(thsBlockKbar.getBlockCode(),thsBlockKbar);
                }
            }
        }
        ThsBlockKbarQuery query = new ThsBlockKbarQuery();
        query.addOrderBy("close_rate", Sort.SortType.DESC);
        query.setTradeDate(dateStr);
        List<ThsBlockKbar> blockKBars= thsBlockKbarService.listByCondition(query);
        if(!CollectionUtils.isEmpty(blockKBars)){
            for (ThsBlockKbar thsBlockKbar:blockKBars){
                BigDecimal closeRate = thsBlockKbar.getCloseRate();
                if(closeRate!=null&&closeRate.compareTo(new BigDecimal("-1"))==-1){
                    ThsBlockKbar hotBlock = hotBlockMap.get(thsBlockKbar.getBlockCode());
                    if(hotBlock!=null){
                        list.add(hotBlock);
                    }
                }
            }
        }

        return list;
    }


}
