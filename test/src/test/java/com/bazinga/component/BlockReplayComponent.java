package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockCompeteDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockReplayComponent {
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockKbarService stockKbarService;

    public Map<String, BlockCompeteDTO> getBlockRateMap(){

        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<String> circulateStockList = circulateInfos.stream().map(CirculateInfo::getStockCode).collect(Collectors.toList());

        Map<String,List<String>> blockDetailMap  = new HashMap<>();
        Map<String,String> blockNameMap = new HashMap<>();
        for (ThsBlockInfo thsBlockInfo : thsBlockInfos) {
            blockNameMap.put(thsBlockInfo.getBlockCode(),thsBlockInfo.getBlockName());
            ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
            query.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(query);
            List<String> detailList = thsBlockStockDetails.stream()
                    .filter(item -> circulateStockList.contains(item.getStockCode()))
                    .map(ThsBlockStockDetail::getStockCode)
                    .collect(Collectors.toList());
            if(detailList.size()<=10){
                continue;
            }
            blockDetailMap.put(thsBlockInfo.getBlockCode(),detailList);
        }

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210517",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);
        Map<String, BlockCompeteDTO> resultMap = new HashMap<>();
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            Date tradeDate = tradeDatePool.getTradeDate();
            Date preTradeDate = commonComponent.preTradeDate(tradeDate);
            String kbarDate = DateUtil.format(tradeDate, DateUtil.yyyyMMdd);
            String fromKbarDate = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
            Map<String, BigDecimal> blockRateMap = new HashMap<>();

            Map<String, List<StockKbar>> cacheKbarMap = new HashMap<>();
            for (CirculateInfo circulateInfo : circulateInfos) {
                String uniqueKey = circulateInfo.getStockCode() + SymbolConstants.UNDERLINE + kbarDate;
                String preUniqueKey = circulateInfo.getStockCode() + SymbolConstants.UNDERLINE + fromKbarDate;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                StockKbar preStockKbar = stockKbarService.getByUniqueKey(preUniqueKey);

                List<StockKbar> kbarList = Lists.newArrayList();
                if(stockKbar!=null && preStockKbar !=null){
                    kbarList.add(preStockKbar);
                    kbarList.add(stockKbar);
                }
                cacheKbarMap.put(circulateInfo.getStockCode(), kbarList);
            }

            blockDetailMap.forEach((blockCode, detailList) -> {
                BigDecimal totalRate = BigDecimal.ZERO;
                int count = 0;
                for (String stockCode : detailList) {
                    List<StockKbar> kbarList = cacheKbarMap.get(stockCode);
                    if (CollectionUtils.isEmpty(kbarList)) {
                        continue;
                    }
                    StockKbar stockKbar = kbarList.get(0);
                    StockKbar lastStockKbar = kbarList.get(kbarList.size() - 1);
                    BigDecimal rate = PriceUtil.getPricePercentRate(lastStockKbar.getClosePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    totalRate = totalRate.add(rate);
                    count++;
                }
                if(count!=0){
                    BigDecimal blockRate = totalRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                    blockRateMap.put(blockCode, blockRate);
                }
            });

            Map<String, BigDecimal> sortRateMap = SortUtil.sortByValue(blockRateMap);


            int computeNum = 1;
            for (Map.Entry<String, BigDecimal> entry : sortRateMap.entrySet()) {
                if(computeNum<=30){
                    String blockCode = entry.getKey();
                    List<String> detailList = blockDetailMap.get(blockCode);
                    for (String stockCode : detailList) {
                        String mapKey = kbarDate + SymbolConstants.UNDERLINE + stockCode;
                        if(!resultMap.keySet().contains(mapKey)){
                            log.info("得到个股排名 stockCode{} kbarDate{} compute{}", stockCode,kbarDate,computeNum);
                            resultMap.put(mapKey,new BlockCompeteDTO(computeNum,blockNameMap.get(blockCode),entry.getValue()));
                        }
                    }
                }else {
                    break;
                }
                computeNum++;
            }

        }
        return resultMap;
    }
}
