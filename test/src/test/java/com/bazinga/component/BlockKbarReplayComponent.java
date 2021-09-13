package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockHeadExportDTO;
import com.bazinga.dto.BlockKbarExportDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockKbarReplayComponent {

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

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(int days){

        List<BlockKbarExportDTO> resultList = Lists.newArrayList();

        Set<String> buySet = new HashSet<>();

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
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210830",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (TradeDatePool tradeDatePool : tradeDatePools) {
            Date tradeDate = tradeDatePool.getTradeDate();
            Date preTradeDate = commonComponent.preTradeDate(tradeDate);
            Date buyTradeDate = commonComponent.afterTradeDate(tradeDate);
            String buyKbarDate = DateUtil.format(buyTradeDate,DateUtil.yyyyMMdd);
            String kbarDate = DateUtil.format(tradeDate,DateUtil.yyyyMMdd);
            Date sellTradeDate = commonComponent.afterTradeDate(buyTradeDate);
            String sellKbarDate = DateUtil.format(sellTradeDate,DateUtil.yyyyMMdd);

            String toKbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
            Date fromDate = DateUtil.addDays(preTradeDate, -days-1);
            String fromKbarDate = DateUtil.format(fromDate,DateUtil.yyyyMMdd);

            Map<String, BigDecimal> blockRateMap = new HashMap<>();

            blockDetailMap.forEach((blockCode,detailList)->{
                BigDecimal totalRate = BigDecimal.ZERO;
                int count=0;
                for (String stockCode : detailList) {
                    StockKbarQuery query = new StockKbarQuery();
                    query.setKbarDateTo(toKbarDate);
                    query.setKbarDateFrom(fromKbarDate);
                    query.setStockCode(stockCode);
                    query.addOrderBy("kbar_date", Sort.SortType.ASC);
                    List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(kbarList)){
                        continue;
                    }
                    StockKbar stockKbar = kbarList.get(0);
                    StockKbar lastStockKbar = kbarList.get(kbarList.size()-1);
                    BigDecimal rate = PriceUtil.getPricePercentRate(lastStockKbar.getClosePrice().subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice());
                    totalRate = totalRate.add(rate);
                    count++;
                }
                BigDecimal blockRate = totalRate.divide(new BigDecimal(count),2,BigDecimal.ROUND_HALF_UP);
                blockRateMap.put(blockCode,blockRate);
            });

            Map<String, BigDecimal> sortRateMap  = SortUtil.sortByValue(blockRateMap);

            int computeNum = 1;

            for (Map.Entry<String, BigDecimal> entry : sortRateMap.entrySet()) {
                String blockCode = entry.getKey();
                BigDecimal blockRate = entry.getValue();
                if(computeNum<=50){
                    List<String> detailList = blockDetailMap.get(blockCode);
                    for (String stockCode : detailList) {
                        String buyKey = stockCode + SymbolConstants.UNDERLINE + buyKbarDate;
                        StockKbar buyKbar = stockKbarService.getByUniqueKey(buyKey);
                        StockKbar prebuyStockKbar = stockKbarService.getByUniqueKey(stockCode + SymbolConstants.UNDERLINE + kbarDate);
                        if(buyKbar == null || prebuyStockKbar ==null){
                            continue;
                        }
                        if(!buySet.contains(stockCode) && StockKbarUtil.isHighUpperPrice(buyKbar,prebuyStockKbar)){
                            BigDecimal sellAvgPrice = historyTransactionDataComponent.calPre1HourAvgPrice(stockCode, sellKbarDate);
                            log.info("满足买入条件 stockCode{} kbarDate{}",stockCode,buyKbarDate);
                            buySet.add(buyKey);
                            BlockKbarExportDTO exportDTO = new BlockKbarExportDTO();
                            exportDTO.setStockCode(stockCode);
                            exportDTO.setBlockCode(blockCode);
                            exportDTO.setBlockName(blockNameMap.get(blockCode));
                            exportDTO.setBlockNum(computeNum);
                            exportDTO.setBlockRate(blockRate);
                            exportDTO.setPremium(PriceUtil.getPricePercentRate(sellAvgPrice.subtract(buyKbar.getHighPrice()),buyKbar.getHighPrice()));
                            resultList.add(exportDTO);
                        }
                    }
                }else {
                    break;
                }
                computeNum++;

            }
        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\主流板块买入日K"+days+".xls");

    }

}
