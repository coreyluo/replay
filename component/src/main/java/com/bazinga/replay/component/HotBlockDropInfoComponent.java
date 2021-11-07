package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockDropBuyInfoDTO;
import com.bazinga.replay.dto.StockKbarSumInfoDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
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
    @Autowired
    private DropFactorService dropFactorService;
    @Autowired
    private HotBlockDropStockService hotBlockDropStockService;

    public void thsBlockKbar(Date date){
        Map<String, BlockDropBuyInfoDTO> map = new HashMap<>();
        List<ThsBlockKbar> hotBlockDrops = hotBlockDrop(date, map);
        if(map.size()==0){
            return;
        }
        for (String key:map.keySet()){
            BlockDropBuyInfoDTO dropBuyInfoDTO = map.get(key);
            ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
            detailQuery.setBlockCode(dropBuyInfoDTO.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
            if(CollectionUtils.isEmpty(details)){
                continue;
            }
            for (ThsBlockStockDetail detail:details){
                StockKbarQuery dropDayQuery = new StockKbarQuery();
                dropDayQuery.setKbarDate(dropBuyInfoDTO.getDropDayKBar().getTradeDate());
                dropDayQuery.setStockCode(detail.getStockCode());
                List<StockKbar> dropDayKbars = stockKbarService.listByCondition(dropDayQuery);
                StockKbarQuery raiseDayQuery = new StockKbarQuery();
                raiseDayQuery.setKbarDate(dropBuyInfoDTO.getRaiseDayKBar().getTradeDate());
                raiseDayQuery.setStockCode(detail.getStockCode());
                List<StockKbar> raiseDayKbars = stockKbarService.listByCondition(raiseDayQuery);
                StockKbarQuery raiseDayPreQuery = new StockKbarQuery();
                Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(dropBuyInfoDTO.getRaiseDayKBar().getTradeDate(), DateUtil.yyyyMMdd));
                raiseDayPreQuery.setKbarDate(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));
                raiseDayPreQuery.setStockCode(detail.getStockCode());
                List<StockKbar> raiseDayPreKbars = stockKbarService.listByCondition(raiseDayPreQuery);

                if(CollectionUtils.isEmpty(dropDayKbars)||CollectionUtils.isEmpty(raiseDayKbars)||CollectionUtils.isEmpty(raiseDayPreKbars)){
                    continue;
                }
                Long tradeQuantity = dropDayKbars.get(0).getTradeQuantity();
                BigDecimal closeRate = PriceUtil.getPricePercentRate(raiseDayKbars.get(0).getAdjClosePrice().subtract(raiseDayPreKbars.get(0).getAdjClosePrice()), raiseDayPreKbars.get(0).getAdjClosePrice());
                DropFactorQuery dropFactorQuery = new DropFactorQuery();
                dropFactorQuery.setFactorType(1);
                List<DropFactor> dropFactors = dropFactorService.listByCondition(dropFactorQuery);
                if(CollectionUtils.isEmpty(dropFactors)){
                    continue;
                }
                for(DropFactor dropFactor:dropFactors){
                    BigDecimal blockDropRate = dropBuyInfoDTO.getBlockDropRate();
                    BigDecimal blockRaiseRate = dropBuyInfoDTO.getBlockRaiseRate();
                    BigDecimal blockRaiseDay5Rate = dropBuyInfoDTO.getBlockRaiseDay5Rate();
                    if(blockDropRate.compareTo(dropFactor.getBlockDropRate())==1){
                        continue;
                    }
                    if(blockRaiseRate.compareTo(dropFactor.getBlockRaiseRate())==1){
                        continue;
                    }
                    if(tradeQuantity<dropFactor.getStockDropDayExchange()){
                        continue;
                    }
                    if(blockRaiseDay5Rate.compareTo(dropFactor.getBlockRaiseDay5Rate())==-1){
                        continue;
                    }
                    if(closeRate.compareTo(dropFactor.getStockRaiseRate())==-1){
                        continue;
                    }
                    HotBlockDropStock hotBlockDropStock = new HotBlockDropStock();
                    hotBlockDropStock.setBlockCode(detail.getBlockCode());
                    hotBlockDropStock.setBlockName(detail.getBlockName());
                    hotBlockDropStock.setStockCode(detail.getStockCode());
                    hotBlockDropStock.setStockName(detail.getStockName());
                    Date tradeDate = commonComponent.afterTradeDate(date);
                    hotBlockDropStock.setTradeDate(DateUtil.format(tradeDate,DateUtil.yyyyMMdd));
                    hotBlockDropStock.setCreateTime(new Date());
                    HotBlockDropStockQuery query = new HotBlockDropStockQuery();
                    query.setStockCode(hotBlockDropStock.getStockCode());
                    query.setTradeDate(hotBlockDropStock.getTradeDate());
                    List<HotBlockDropStock> hotBlockDropStocks = hotBlockDropStockService.listByCondition(query);
                    if(CollectionUtils.isEmpty(hotBlockDropStocks)) {
                        hotBlockDropStockService.save(hotBlockDropStock);
                    }
                }

            }

        }


    }
    public List<ThsBlockKbar> hotBlockDrop(Date date,Map<String, BlockDropBuyInfoDTO> map){
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
                        BlockDropBuyInfoDTO blockDropBuyInfoDTO = new BlockDropBuyInfoDTO();
                        blockDropBuyInfoDTO.setBlockCode(thsBlockKbar.getBlockCode());
                        blockDropBuyInfoDTO.setBlockDropRate(closeRate);
                        blockDropBuyInfoDTO.setBlockRaiseRate(hotBlock.getCloseRate());
                        blockDropBuyInfoDTO.setBlockRaiseDay5Rate(hotBlock.getCloseRateDay5());
                        blockDropBuyInfoDTO.setDropDayKBar(thsBlockKbar);
                        blockDropBuyInfoDTO.setRaiseDayKBar(hotBlock);
                        map.put(thsBlockKbar.getBlockCode(),blockDropBuyInfoDTO);
                    }
                }
            }
        }

        return list;
    }

    public void getAvgPrice(){
        List<HotBlockDropStock> hotBlockDropStocks = hotBlockDropStockService.listByCondition(new HotBlockDropStockQuery());
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (HotBlockDropStock hotBlockDropStock:hotBlockDropStocks){
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setKbarDate(hotBlockDropStock.getTradeDate());
            stockKbarQuery.setStockCode(hotBlockDropStock.getStockCode());
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            BigDecimal openPrice = stockKbars.get(0).getOpenPrice();
            BigDecimal avgPrice = currentDayTransactionDataComponent.calAvgPrice(hotBlockDropStock.getStockCode());
            BigDecimal rate = PriceUtil.getPricePercentRate(avgPrice.subtract(openPrice), openPrice);
            System.out.println(hotBlockDropStock.getStockCode()+"======="+rate);
            count = count+1;
            total = total.add(rate);
        }
        System.out.println(total+"======"+count);
        BigDecimal divide = total.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
        System.out.println(divide);
    }


}
