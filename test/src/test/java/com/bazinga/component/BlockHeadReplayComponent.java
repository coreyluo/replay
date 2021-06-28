package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BlockHeadExportDTO;
import com.bazinga.dto.PlankInfoDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlockHeadReplayComponent {


    @Autowired
    private ThsBlockInfoService thsBlockInfoService;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void invokeStrategy(){
        List<BlockHeadExportDTO> resultList = Lists.newArrayList();

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String,List<String>> stockBlockMap = new HashMap<>();
        for (CirculateInfo circulateInfo : circulateInfos) {
            ThsBlockStockDetailQuery thsBlockDetailQuery = new ThsBlockStockDetailQuery();
            thsBlockDetailQuery.setStockCode(circulateInfo.getStockCode());
            List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(thsBlockDetailQuery);
            stockBlockMap.put(circulateInfo.getStockCode(),thsBlockStockDetails.stream().map(ThsBlockStockDetail::getBlockCode).collect(Collectors.toList()));
        }


        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            Map<String, List<PlankInfoDTO>> blockPlankMap = new HashMap<>();
            for (CirculateInfo circulateInfo : circulateInfos) {
                String stockCode = circulateInfo.getStockCode();
                StockKbarQuery stockbarQuery = new StockKbarQuery();
                stockbarQuery.setStockCode(stockCode);
                stockbarQuery.setKbarDateTo(kbarDate);
                stockbarQuery.setStockCode(stockCode);
                stockbarQuery.setLimit(4);
                stockbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockbarQuery);
                if(stockKbars.size()<4){
                    continue;
                }
                StockKbar stockKbar = stockKbars.get(0);
                StockKbar pre1Kbar = stockKbars.get(1);
                StockKbar pre2Kbar = stockKbars.get(2);
                StockKbar pre3Kbar = stockKbars.get(3);
                if(!PriceUtil.isUpperPrice(stockCode,stockKbar.getHighPrice(),pre1Kbar.getClosePrice())){
                    continue;
                }
                if(PriceUtil.isUpperPrice(stockCode,pre1Kbar.getClosePrice(),pre2Kbar.getClosePrice()) || PriceUtil.isUpperPrice(stockCode,pre2Kbar.getClosePrice(),pre3Kbar.getClosePrice())){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockCode, kbarDate);
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if(transactionDataDTO.getTradeType()==1 && stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice())==0){
                  //      log.info("判断为首次涨停stockCode{} kbarDate{}",stockCode,kbarDate);
                        List<String> blockList = stockBlockMap.get(stockCode);
                        for (String blockCode : blockList) {
                            List<PlankInfoDTO> plankInfoDTOS = blockPlankMap.get(blockCode);
                            if(plankInfoDTOS ==null){
                                plankInfoDTOS = Lists.newArrayList();
                            }
                            plankInfoDTOS.add(new PlankInfoDTO(stockCode,transactionDataDTO.getTradeTime()));
                            blockPlankMap.put(blockCode,plankInfoDTOS);
                        }
                        break;
                    }
                }
            }
            //上板排序
            for (Map.Entry<String, List<PlankInfoDTO>> entry : blockPlankMap.entrySet()) {
                List<PlankInfoDTO> list = entry.getValue();
                List<PlankInfoDTO> sortList = list.stream().sorted(Comparator.comparing(PlankInfoDTO::getPlankTime,
                        Comparator.comparing(a -> Integer.valueOf(a.replaceAll(":", ""))))).collect(Collectors.toList());
                blockPlankMap.put(entry.getKey(),sortList);
            }
            for (CirculateInfo circulateInfo : circulateInfos) {
                PlankInfoDTO plankInfoDTO = getMaxDragonNum(circulateInfo.getStockCode(), stockBlockMap, blockPlankMap);
                if(plankInfoDTO.getMaxDragonNum()> 0 && plankInfoDTO.getMaxDragonNum()<6){
                    log.info("满足买入条件 stockCode={} ,kbarDate{} blockCode{} plankTime{} maxDragonNum{}", circulateInfo.getStockCode(),kbarDate,plankInfoDTO.getBlockCode(),plankInfoDTO.getPlankTime(),plankInfoDTO.getMaxDragonNum());
                    BlockHeadExportDTO exportDTO = new BlockHeadExportDTO();
                    exportDTO.setStockCode(circulateInfo.getStockCode());
                    exportDTO.setStockName(circulateInfo.getStockName());
                    exportDTO.setBlockCode(plankInfoDTO.getBlockCode());
                    exportDTO.setMaxDragonNum(plankInfoDTO.getMaxDragonNum());
                   // exportDTO.setBuyPrice();
                    exportDTO.setKbarDate(kbarDate);
                    exportDTO.setPlankTime(plankInfoDTO.getPlankTime());
                    StockKbarQuery kbarQuery = new StockKbarQuery();
                    kbarQuery.setStockCode(circulateInfo.getStockCode());
                    kbarQuery.setKbarDateFrom(kbarDate);
                    kbarQuery.setLimit(2);
                    kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
                    List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
                    if(stockKbars.size()==2){
                        StockKbar stockKbar = stockKbars.get(0);
                        StockKbar sellStockKbar = stockKbars.get(1);
                        BigDecimal sellPrice = historyTransactionDataComponent.calAvgPrice(circulateInfo.getStockCode(),DateUtil.parseDate(sellStockKbar.getKbarDate(),DateUtil.yyyyMMdd));
                        exportDTO.setBuyPrice(stockKbar.getHighPrice());
                        exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    }
                    resultList.add(exportDTO);
                }



            }
        }


        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\同花顺概念买入.xls");


    }

    private PlankInfoDTO getMaxDragonNum(String stockCode, Map<String, List<String>> stockBlockMap, Map<String, List<PlankInfoDTO>> blockPlankMap) {
        List<String> blockList = stockBlockMap.get(stockCode);
        PlankInfoDTO plankInfoDTO = new PlankInfoDTO();
        plankInfoDTO.setMaxDragonNum(-2);
        int maxDragonNum = -2;
        for (String blockCode : blockList) {
            List<PlankInfoDTO> plankInfoDTOS = blockPlankMap.get(blockCode);
            if(CollectionUtils.isEmpty(plankInfoDTOS)){
                continue;
            }
            for (int i = 0; i < plankInfoDTOS.size(); i++) {
                if(maxDragonNum>4){
                    plankInfoDTO.setMaxDragonNum(maxDragonNum+1);
                    return plankInfoDTO;
                }
                if(stockCode.equals(plankInfoDTOS.get(i).getStockCode())){
                    if(i>maxDragonNum){
                        plankInfoDTO.setStockCode(stockCode);
                        plankInfoDTO.setPlankTime(plankInfoDTOS.get(i).getPlankTime());
                        plankInfoDTO.setBlockCode(blockCode);
                        maxDragonNum = i;
                    }
                }
            }
        }
        plankInfoDTO.setMaxDragonNum(maxDragonNum+1);
        return plankInfoDTO;
    }
}
