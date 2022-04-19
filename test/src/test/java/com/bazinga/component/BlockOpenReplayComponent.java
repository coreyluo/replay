package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockOpenReplayDTO;
import com.bazinga.dto.RankDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Rank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private RedisMoniorService redisMoniorService;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    @Autowired
    private CommonComponent commonComponent;

    public void replay(String kbarDateFrom ,String kbarDateTo){

        List<BlockOpenReplayDTO> resultList = new ArrayList<>();
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        Map<String, List<RankDTO>> openAmountRankMap = getOpenAmountRank(kbarDateFrom,kbarDateTo);

        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        blockInfos =  blockInfos.stream().filter(item-> item.getBlockCode().startsWith("8803") || item.getBlockCode().startsWith("8804")).collect(Collectors.toList());
        Map<String,List<String>> blockDetailMap = new HashMap<>();
        Map<String, String > blockNameMap = new HashMap<>();
       // Map<String, BigDecimal> blockRateMap = getBlockRateMap(blockInfos);
        for (BlockInfo blockInfo : blockInfos) {
            BlockStockDetailQuery detailQuery = new BlockStockDetailQuery();
            detailQuery.setBlockCode(blockInfo.getBlockCode());
            List<BlockStockDetail> blockStockDetails = blockStockDetailService.listByCondition(detailQuery);
            blockDetailMap.put(blockInfo.getBlockCode(),blockStockDetails.stream().map(BlockStockDetail::getStockCode).collect(Collectors.toList()));
            blockNameMap.put(blockInfo.getBlockCode(),blockInfo.getBlockName());
        }

        Map<String, Integer> blockPlankInfoMap = getPlankInfo(blockDetailMap);


        openAmountRankMap.forEach((kbarDate,list) -> {
            Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd));
            String preKbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
            Map<String,List<RankDTO>> blockCurrentMap = new HashMap<>();
            for (RankDTO rankDTO : list) {
                String stockCode = rankDTO.getStockCode();
                blockDetailMap.forEach((blockCode,detailList)->{
                    if(detailList.contains(stockCode)){
                        List<RankDTO> currentList = blockCurrentMap.computeIfAbsent(blockCode, k -> new ArrayList<>());
                        currentList.add(rankDTO);
                    }
                });
            }

            log.info("{}", JSONObject.toJSONString(blockCurrentMap));

            BigDecimal totalOpenAmount = list.stream().map(RankDTO::getTradeAmount).reduce(BigDecimal::add).get();

            Map<String,Integer> blockCountMap = new HashMap<>();
            Map<String,BigDecimal> blockOpenAmountMap = new HashMap<>();
            Map<String,Integer> tdxBlockOpenAmountMap = new HashMap<>();
            blockCurrentMap.forEach((blockCode,rankList)->{
                blockCountMap.put(blockCode,rankList.size());
                blockOpenAmountMap.put(blockCode,rankList.stream().map(RankDTO::getTradeAmount).reduce(BigDecimal::add).get());
               /* List<ThirdSecondTransactionDataDTO> tdxList = historyTransactionDataComponent.getData(blockCode, kbarDate);
                tdxBlockOpenAmountMap.put(blockCode,tdxList.get(0).getTradeQuantity());*/
            });

            Map<String, Integer> sortedCountMap = SortUtil.sortByValue(blockCountMap);
            Map<String, BigDecimal> sortedOpenAmountMap = SortUtil.sortByValue(blockOpenAmountMap);
            Map<String, Integer> sortedTdxOpenMap = SortUtil.sortByValue(tdxBlockOpenAmountMap);
            Map<String, Integer> blockCountRankMap = new HashMap<>();
            Map<String, Integer> blockOpenAmountRankMap = new HashMap<>();
            Map<String, Integer> blockTdxOpenAmountRankMap = new HashMap<>();

            int rank= 1;
            for (Map.Entry<String, Integer> entry : sortedCountMap.entrySet()) {
                blockCountRankMap.put(entry.getKey(),rank);
                rank++;
            }
            rank = 1;
            for (Map.Entry<String, BigDecimal> entry : sortedOpenAmountMap.entrySet()) {
                blockOpenAmountRankMap.put(entry.getKey(),rank);
                rank++;
            }
            rank = 1;
            for (Map.Entry<String, Integer> entry : sortedTdxOpenMap.entrySet()) {
                blockTdxOpenAmountRankMap.put(entry.getKey(),rank);
                rank++;
            }


            blockCurrentMap.forEach((blockCode,rankList)->{
                BlockOpenReplayDTO exportDTO = new BlockOpenReplayDTO();
                exportDTO.setBlockCode(blockCode);
                exportDTO.setBlockName(blockNameMap.get(blockCode));
                exportDTO.setKbarDate(kbarDate);
                exportDTO.setDetailSize(rankList.size());
                exportDTO.setOpenCountRank(blockCountRankMap.get(blockCode));
                exportDTO.setOpenAmount200(blockOpenAmountMap.get(blockCode));
                exportDTO.setOpenAmountRank(blockOpenAmountRankMap.get(blockCode));
                exportDTO.setTotalOpenAmount200(totalOpenAmount);
                exportDTO.setShOpenRate(shOpenRateMap.get(kbarDate));
                BigDecimal totalOpenRate = rankList.stream().map(RankDTO::getOpenRate).reduce(BigDecimal::add).get();
                BigDecimal totalPremium = rankList.stream().map(RankDTO::getPremium).reduce(BigDecimal::add).get();
                exportDTO.setTotalOpenRate(totalOpenRate);
                exportDTO.setPremium(totalPremium);

                List<ThirdSecondTransactionDataDTO> fenshiList = historyTransactionDataComponent.getData(blockCode, kbarDate);
                ThirdSecondTransactionDataDTO open = fenshiList.get(0);
                int overOpenCount = 0;
                for (int j = 1; j < 11; j++) {
                    ThirdSecondTransactionDataDTO transactionDataDTO = fenshiList.get(j);
                    if(transactionDataDTO.getTradePrice().compareTo(open.getTradePrice())>0){
                        overOpenCount++;
                    }
                }
                exportDTO.setOverOpenCount(overOpenCount);
                /*if(blockRateMap!=null){
                    exportDTO.setDayRate(blockRateMap.get(blockCode + SymbolConstants.UNDERLINE + kbarDate +1));
                    exportDTO.setDay3Rate(blockRateMap.get(blockCode + SymbolConstants.UNDERLINE + kbarDate +3));
                    exportDTO.setDay5Rate(blockRateMap.get(blockCode + SymbolConstants.UNDERLINE + kbarDate +5));
                    exportDTO.setBlockOpenRate(blockRateMap.get(blockCode + SymbolConstants.UNDERLINE + kbarDate +0));
                }*/
                if(blockPlankInfoMap!=null){
                    exportDTO.setClosePlankCount(blockPlankInfoMap.get(blockCode + SymbolConstants.UNDERLINE + preKbarDate +1));
                    exportDTO.setCloseUnPlankCount(blockPlankInfoMap.get(blockCode + SymbolConstants.UNDERLINE + preKbarDate +2));
                    exportDTO.setPlankHigh(blockPlankInfoMap.get(blockCode + SymbolConstants.UNDERLINE + preKbarDate +3));
                }
                resultList.add(exportDTO);
            });

        });
        log.info("文件输出");
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\板块竞价维度"+kbarDateFrom+"_"+kbarDateTo+".xls");
    }

    private  Map<String,Integer>  getPlankInfo(Map<String, List<String>> blockDetailMap) {

        Map<String,Integer> resultMap = new HashMap<>();

        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20201201",DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
            blockDetailMap.forEach((blockCode,detailList)->{

                int plankCount = 0;
                int unPlankCount = 0;
                int plankHigh = 0;
                for (String stockCode : detailList) {
                    StockKbarQuery query = new StockKbarQuery();
                    query.setStockCode(stockCode);
                    query.setKbarDateTo(kbarDate);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(10);
                    List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<10){
                        continue;
                    }

                    StockKbar stockKbar = stockKbarList.get(0);
                    StockKbar preStockKbar = stockKbarList.get(1);
                    if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                        continue;
                    }
                    if(StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)){
                        plankCount ++;
                        int plank = PlankHighUtil.calSerialsPlank(stockKbarList);
                        if(plankHigh<plank){
                            plankHigh = plank;
                        }
                    }else {
                        unPlankCount++;
                    }
                }

                resultMap.put(blockCode + SymbolConstants.UNDERLINE + kbarDate+ 1,plankCount);
                resultMap.put(blockCode + SymbolConstants.UNDERLINE + kbarDate+ 2,unPlankCount);
                resultMap.put(blockCode + SymbolConstants.UNDERLINE + kbarDate+ 3,plankHigh);
            });


        }

        return resultMap;

    }


    public Map<String,BigDecimal> getBlockRateMap(List<BlockInfo> blockInfos){


        Map<String,BigDecimal> resultMap = new HashMap<>();
        for (BlockInfo blockInfo : blockInfos) {
            String blockCode = blockInfo.getBlockCode();
            List<StockKbar> kbarList = Lists.newArrayList();
            for (int i = 0; i < 300; i++) {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, blockCode, i, 1);
                List<StockKbar> kBarDTOS = KBarDTOConvert.convertStockKbar(blockCode,dataTable);
                if(CollectionUtils.isEmpty(kBarDTOS)){
                    break;
                }
                kbarList.add(kBarDTOS.get(0));
            }
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<6){
                return resultMap;
            }
            for (int i = 0; i < kbarList.size()-6; i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i+1);

                resultMap.put(blockCode + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+0,
                        PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                resultMap.put(blockCode + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+1, getNDaysUpperRateDesc(kbarList.subList(i+1,i+7),1));
                resultMap.put(blockCode + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+3, getNDaysUpperRateDesc(kbarList.subList(i+1,i+7),3));
                resultMap.put(blockCode + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+5, getNDaysUpperRateDesc(kbarList.subList(i+1,i+7),5));
            }
        }

        return  resultMap;

    }


    public Map<String,List<RankDTO>> getOpenAmountRank(String kbarDateFrom ,String kbarDateTo){
        Map<String,List<RankDTO>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate(kbarDateFrom,DateUtil.yyyyMMdd));
        tradeQuery.setTradeDateTo(DateUtil.parseDate(kbarDateTo,DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);

        String redisKey = "OPEN_AMOUNT_RANK"+ kbarDateFrom + SymbolConstants.UNDERLINE + kbarDateTo;
        RedisMonior byRedisKey = redisMoniorService.getByRedisKey(redisKey);
        if(byRedisKey!=null){
            JSONObject jsonObject = JSONObject.parseObject(byRedisKey.getRedisValue());
            jsonObject.forEach((key,value)->{
                resultMap.put(key,JSONObject.parseArray(value.toString(),RankDTO.class));
            });
            return resultMap;
        }
        for (TradeDatePool tradeDatePool : tradeDatePools) {
          /*  if(!commonComponent.isTradeDate(tradeDatePool.getTradeDate())){
                continue;
            }*/
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
            Map<String, BigDecimal> tempMap = new HashMap<>();
            for (CirculateInfo circulateInfo : circulateInfos) {
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), tradeDatePool.getTradeDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                ThirdSecondTransactionDataDTO open = list.get(0);
                tempMap.put(circulateInfo.getStockCode(), open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity()).multiply(CommonConstant.DECIMAL_HUNDRED)));
            }
            Map<String, BigDecimal> sortedMap = SortUtil.sortByValue(tempMap);
            int rank = 1;
            List<RankDTO> rankList = Lists.newArrayList();
            for (Map.Entry<String, BigDecimal> entry : sortedMap.entrySet()) {
                if(rank<=200){

                    StockKbarQuery query= new StockKbarQuery();
                    query.setStockCode(entry.getKey());
                    query.setKbarDateTo(kbarDate);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(2);
                    List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<2){
                        continue;
                    }
                    StockKbar stockKbar = stockKbarList.get(0);
                    StockKbar preStockKbar = stockKbarList.get(1);
                    BigDecimal openRate;
                    if(stockKbar.getAdjFactor().compareTo(preStockKbar.getAdjFactor())==0){
                        openRate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice());
                    }else {
                        openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()),preStockKbar.getAdjClosePrice());
                    }

                    StockKbarQuery sellQuery= new StockKbarQuery();
                    sellQuery.setStockCode(entry.getKey());
                    sellQuery.setKbarDateFrom(kbarDate);
                    sellQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
                    sellQuery.setLimit(2);
                    List<StockKbar> sellStockKbarList = stockKbarService.listByCondition(sellQuery);
                    if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<2){
                        continue;
                    }
                    StockKbar sellStockKbar = sellStockKbarList.get(1);
                    BigDecimal premium;

                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                    if(CollectionUtils.isEmpty(list) || list.size()<20){
                        continue;
                    }
                    ThirdSecondTransactionDataDTO buyDTO = list.get(10);
                    BigDecimal sellPrice = sellStockKbar.getOpenPrice().add(sellStockKbar.getClosePrice()).divide(new BigDecimal("2"),2,BigDecimal.ROUND_HALF_UP);
                    premium  = PriceUtil.getPricePercentRate(sellPrice.subtract(buyDTO.getTradePrice()),buyDTO.getTradePrice());
                   /* if(stockKbar.getAdjFactor().compareTo(sellStockKbar.getAdjFactor())==0){

                    }else {
                        log.info("卖出发生复权stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                        BigDecimal sellPrice = sellStockKbar.getAdjOpenPrice().add(sellStockKbar.getAdjClosePrice()).divide(new BigDecimal("2"),2,BigDecimal.ROUND_HALF_UP);
                        premium  = PriceUtil.getPricePercentRate(sellPrice.subtract(stockKbar.getAdjOpenPrice()),stockKbar.getAdjOpenPrice());
                    }*/
                    if(StockKbarUtil.isUpperPrice(stockKbar,preStockKbar) && stockKbar.getLowPrice().compareTo(stockKbar.getHighPrice())==0){
                        log.info("判断为一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                        premium = BigDecimal.ZERO;
                    }
                    rankList.add(new RankDTO(entry.getKey(),rank,entry.getValue(),openRate,premium));



                }
                rank++;
            }
            resultMap.put(kbarDate,rankList);
        }
        RedisMonior redisMonior = new RedisMonior();
        redisMonior.setRedisKey(redisKey);
        redisMonior.setRedisValue(JSONObject.toJSONString(resultMap));
        redisMoniorService.save(redisMonior);
        return resultMap;
    }


    public static BigDecimal getNDaysUpperRateDesc(List<StockKbar> stockKbarList45, int i) {
        StockKbar stockKbar = stockKbarList45.get(i);
        StockKbar currentStockKbar = stockKbarList45.get(0);

        return PriceUtil.getPricePercentRate(currentStockKbar.getClosePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
    }
}
