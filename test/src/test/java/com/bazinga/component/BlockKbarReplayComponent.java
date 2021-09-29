package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockHeadExportDTO;
import com.bazinga.dto.BlockKbarExportDTO;
import com.bazinga.dto.BlockKbarImportDTO;
import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
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
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210530",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (TradeDatePool tradeDatePool : tradeDatePools) {
            Date tradeDate = tradeDatePool.getTradeDate();
            Date preTradeDate = commonComponent.preTradeDate(tradeDate);
            Date buyTradeDate = commonComponent.afterTradeDate(tradeDate);
            String buyKbarDate = DateUtil.format(buyTradeDate,DateUtil.yyyyMMdd);
            String kbarDate = DateUtil.format(tradeDate,DateUtil.yyyyMMdd);
            if("20210914".equals(buyKbarDate)){
                continue;
            }
            Date sellTradeDate = commonComponent.afterTradeDate(buyTradeDate);
            String sellKbarDate = DateUtil.format(sellTradeDate,DateUtil.yyyyMMdd);

            String toKbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
            Date fromDate = DateUtil.addDays(preTradeDate, -days-1);
            String fromKbarDate = DateUtil.format(fromDate,DateUtil.yyyyMMdd);

            Map<String, BigDecimal> blockRateMap = new HashMap<>();

            Map<String,List<StockKbar>> cacheKbarMap = new HashMap<>();
            for (CirculateInfo circulateInfo : circulateInfos) {
                StockKbarQuery query = new StockKbarQuery();
                query.setKbarDateTo(toKbarDate);
                query.setKbarDateFrom(fromKbarDate);
                query.setStockCode(circulateInfo.getStockCode());
                query.addOrderBy("kbar_date", Sort.SortType.ASC);
                List<StockKbar> kbarList = stockKbarService.listByCondition(query);

                cacheKbarMap.put(circulateInfo.getStockCode(),kbarList);
            }

            blockDetailMap.forEach((blockCode,detailList)->{
                BigDecimal totalRate = BigDecimal.ZERO;
                int count=0;
                for (String stockCode : detailList) {
                    List<StockKbar> kbarList = cacheKbarMap.get(stockCode);
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
                if(computeNum<=10){
                    List<String> detailList = blockDetailMap.get(blockCode);
                    for (String stockCode : detailList) {
                        String buyKey = stockCode + SymbolConstants.UNDERLINE + buyKbarDate;
                        String sellKey = stockCode + SymbolConstants.UNDERLINE + sellKbarDate;
                        StockKbar buyKbar = stockKbarService.getByUniqueKey(buyKey);
                        StockKbar prebuyStockKbar = stockKbarService.getByUniqueKey(stockCode + SymbolConstants.UNDERLINE + kbarDate);
                        StockKbar sellKbar = stockKbarService.getByUniqueKey(sellKey);
                        if(buyKbar == null || prebuyStockKbar ==null || sellKbar == null){
                            continue;
                        }
                        //if(!buySet.contains(stockCode) && StockKbarUtil.isHighUpperPrice(buyKbar,prebuyStockKbar)){
                        if(stockCode.startsWith("3")){
                          //  BigDecimal sellAvgPrice = historyTransactionDataComponent.calPre1HourAvgPrice(stockCode, sellKbarDate);
                            log.info("满足买入条件 stockCode{} kbarDate{}",stockCode,buyKbarDate);
                            buySet.add(buyKey);
                            BlockKbarExportDTO exportDTO = new BlockKbarExportDTO();
                            exportDTO.setStockCode(stockCode);
                            exportDTO.setBlockCode(blockCode);
                            exportDTO.setKbarDate(buyKbarDate);
                            exportDTO.setBlockName(blockNameMap.get(blockCode));
                            exportDTO.setBlockNum(computeNum);
                            exportDTO.setBlockRate(blockRate);
                            exportDTO.setPremium(PriceUtil.getPricePercentRate(sellKbar.getClosePrice().subtract(buyKbar.getOpenPrice()),buyKbar.getOpenPrice()));
                            resultList.add(exportDTO);
                        }
                    }
                }else {
                    break;
                }
                computeNum++;

            }
        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\主流板块300买入日K"+days+".xls");

    }


    public  void  anaysisBest(){

        try {

            Map<Integer,BigDecimal> totalPremiumMap = new HashMap<>();
            Map<Integer,BigDecimal> avgPremiumMap = new HashMap<>();

            for (int i = 5; i <= 60; i++) {
                File file = new File("E:/trendData/主流板块买入日K"+i+".xls");
                List<BlockKbarImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(BlockKbarImportDTO.class);
                importList = importList.stream().filter(item-> item.getBlockNum()<4).collect(Collectors.toList());
                BigDecimal totalPremuium = BigDecimal.ZERO;
                for (BlockKbarImportDTO blockKbarImportDTO : importList) {
                    totalPremuium = totalPremuium.add(blockKbarImportDTO.getPremium());
                }
                totalPremiumMap.put(i,totalPremuium);
                avgPremiumMap.put(i,totalPremuium.divide(new BigDecimal(importList.size()),2,BigDecimal.ROUND_HALF_UP));
            }

            Map<Integer, BigDecimal> sortTotaltMap = SortUtil.sortByValue(totalPremiumMap);
            Map<Integer, BigDecimal> sortAvgtMap = SortUtil.sortByValue(avgPremiumMap);
            sortTotaltMap.forEach((key,value)->{
                log.info("总收益率排名 {}日涨幅,总收益率{}",key,value);
            });
            sortAvgtMap.forEach((key,value)->{
                log.info("平均收益率排名 {}日涨幅,平均收益率{}",key,value);
            });
            log.info("");

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

    }

}
