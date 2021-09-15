package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.MainBlockExportDTO;
import com.bazinga.dto.PositionBlockDTO;
import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockInfoService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PositionBlockReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private ThsBlockInfoService thsBlockInfoService;


    public void replay(){

        List<PositionBlockDTO> resultList = Lists.newArrayList();


        File file = new File("E:/excelExport/主流板块市场数据源.xlsx");
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String,String> blockNameMap = new HashMap<>();
        Map<String,List<String>> blockDetailMap = new HashMap<>();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo : thsBlockInfos) {
            ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
            query.setBlockCode(thsBlockInfo.getBlockCode());
            blockNameMap.put(thsBlockInfo.getBlockCode(),thsBlockInfo.getBlockName());
            List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(query);
            if(CollectionUtils.isEmpty(thsBlockStockDetails)){
                continue;
            }
            List<String> detailList = thsBlockStockDetails.stream().filter(item->!item.getStockCode().startsWith("688")).map(ThsBlockStockDetail::getStockCode).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(thsBlockStockDetails) && thsBlockStockDetails.size()>10){
                blockDetailMap.put(thsBlockInfo.getBlockCode(),detailList);
            }
        }


        try {
            Map<String, Set<String>> plankMinMap = new HashMap<>();

            List<PositionOwnImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(PositionOwnImportDTO.class);
            for (PositionOwnImportDTO stockPosition : importList) {
                String kbarDate = DateUtil.format(stockPosition.getKbarDate(),DateUtil.yyyyMMdd);
             /*   if(!kbarDate.startsWith("20210823")){
                    continue;
                }*/
                Date preMinDate = DateUtil.addMinutes(stockPosition.getOrderTime(),-1);
                String preMin = DateUtil.format(preMinDate,DateUtil.HH_MM);
                if("09:29".equals(preMin)){
                    preMin = "09:25";
                }
                if("12:59".equals(preMin)){
                    preMin = "11:29";
                }
                Set<String> plankMinSet = plankMinMap.get(kbarDate);
                if(plankMinSet ==null ){
                    plankMinSet = new HashSet<>();
                    plankMinSet.add("09:25");
                    plankMinSet.add(preMin);
                    plankMinMap.put(kbarDate,plankMinSet);
                }
                plankMinSet.add(preMin);
            }
            log.info("委托时间map数据{}", JSONObject.toJSONString(plankMinMap));

            for (Map.Entry<String, Set<String>> entry : plankMinMap.entrySet()) {
                String kbarDate = entry.getKey();
                Set<String> minSet = entry.getValue();

                Map<String,BigDecimal> stockRateMinMap = new HashMap<>();
                Map<String,Boolean> stockUpperMap = new HashMap<>();
                for (CirculateInfo circulateInfo : circulateInfos) {
                    StockKbarQuery query = new StockKbarQuery();
                    query.setKbarDateTo(kbarDate);
                    query.setStockCode(circulateInfo.getStockCode());
                    query.setLimit(2);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<2){
                        continue;
                    }
                    StockKbar stockKbar = kbarList.get(0);
                    StockKbar preStockKbar = kbarList.get(1);
                    if(preStockKbar.getClosePrice().compareTo(BigDecimal.ZERO) ==0){
                        continue;
                    }
                    if(preStockKbar.getAdjFactor().compareTo(stockKbar.getAdjFactor())!=0){
                        BigDecimal closePrice = preStockKbar.getClosePrice().multiply(preStockKbar.getAdjFactor().divide(stockKbar.getAdjFactor(),10,BigDecimal.ROUND_HALF_UP));
                        log.info("需要做收盘价复权处理 stockCode{} kbarDate{} 复权后收盘价格{}",preStockKbar.getStockCode(),preStockKbar.getKbarDate(),closePrice);
                        preStockKbar.setClosePrice(closePrice);
                    }
                    log.info("获取分时成交数据stockCode{} kbarDate{}",circulateInfo.getStockCode(),kbarDate);
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), kbarDate);
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        if(minSet.contains(transactionDataDTO.getTradeTime())){
                            if(StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                                boolean isUpper = transactionDataDTO.getTradeType()==1 && transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())==0;
                                stockUpperMap.put(stockKbar.getStockCode() + SymbolConstants.UNDERLINE + transactionDataDTO.getTradeTime(),isUpper);
                            }
                            BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                            stockRateMinMap.put(stockKbar.getStockCode() + SymbolConstants.UNDERLINE + transactionDataDTO.getTradeTime(),rate);

                        }
                    }
                }

                Map<String,List<BlockResult>> minBlockRateMap = new HashMap<>();
                for (Map.Entry<String, List<String>>  detailListEntry : blockDetailMap.entrySet()) {
                    String blockCode = detailListEntry.getKey();
                    List<String> detailList = detailListEntry.getValue();
                    BigDecimal openRate = BigDecimal.ZERO;
                    boolean openRateFlag = true;
                    TreeSet<String> treeSet = Sets.newTreeSet(minSet);
                    for (String min : treeSet) {
                        BigDecimal totalRate = BigDecimal.ZERO;
                        int count = 0;
                        for (String stockCode : detailList) {
                            BigDecimal rate = stockRateMinMap.get(stockCode + SymbolConstants.UNDERLINE + min);
                            if(rate==null){
                                Date tempDate = DateUtil.parseDate(min, DateUtil.HH_MM);
                                Date preMinDate = DateUtil.addMinutes(tempDate,-1L);
                                Date pre2MinDate = DateUtil.addMinutes(tempDate,-2L);
                                String preMin = DateUtil.format(preMinDate, DateUtil.HH_MM);
                                String pre2Min = DateUtil.format(pre2MinDate, DateUtil.HH_MM);
                                rate = stockRateMinMap.get(stockCode + SymbolConstants.UNDERLINE + preMin);
                                if(rate == null ){
                                    rate = stockRateMinMap.get(stockCode + SymbolConstants.UNDERLINE + pre2Min);
                                }
                            }
                            if(rate!=null){
                                totalRate = totalRate.add(rate);
                                count++;
                            }
                        }
                        if(count==0){
                            continue;
                        }
                        BigDecimal avgRate = totalRate.divide(new BigDecimal(count),2,BigDecimal.ROUND_HALF_UP);
                        log.info("板块blockCode{} 涨幅{}",blockCode,avgRate);
                        if("09:25".equals(min) && openRateFlag){
                            openRate = avgRate;
                            openRateFlag = false;
                        }
                        List<BlockResult> blockResults = minBlockRateMap.get(min);
                        if(blockResults ==null){
                            blockResults = new ArrayList<>();
                            blockResults.add(new BlockResult(blockCode,blockNameMap.get(blockCode),avgRate,openRate));
                            minBlockRateMap.put(min,blockResults);
                        }
                        blockResults.add(new BlockResult(blockCode,blockNameMap.get(blockCode),avgRate,openRate));
                    }

                }
                Map<String,List<BlockResult>> minBlockResult = new HashMap<>();
                for (String min : minSet) {
                    List<BlockResult> blockResults = minBlockRateMap.get(min);
                    if(CollectionUtils.isEmpty(blockResults)){
                        log.info("板块涨幅结果为空 min{}",min);
                        continue;
                    }
                    List<BlockResult> sortList = blockResults.stream().sorted(Comparator.comparing(BlockResult::getBlockRate).reversed()).collect(Collectors.toList());
                    minBlockResult.put(min,sortList);
                }


                for (PositionOwnImportDTO stockPosition : importList) {
                    String positionKbarDate = DateUtil.format(stockPosition.getKbarDate(),DateUtil.yyyyMMdd);
                    if(kbarDate.equals(positionKbarDate)){
                        Date preMinDate = DateUtil.addMinutes(stockPosition.getOrderTime(),-1);
                        String preMin = DateUtil.format(preMinDate,DateUtil.HH_MM);
                        if("09:29".equals(preMin)){
                            preMin = "09:25";
                        }
                        if("12:59".equals(preMin)){
                            preMin = "11:29";
                        }

                        List<BlockResult> blockResults = minBlockResult.get(preMin);
                        if(CollectionUtils.isEmpty(blockResults)){
                            log.info("板块涨幅数据未空 orderTime{}",preMin);
                            continue;
                        }
                        for (int i = 0; i < blockResults.size(); i++) {
                            BlockResult blockResult = blockResults.get(i);
                            List<String> detailList = blockDetailMap.get(blockResult.getBlockCode());
                            if(detailList.contains(stockPosition.getStockCode())){
                                log.info("找到最大涨幅板块 blockCode{}, blockName{} rate{}",blockResult.getBlockCode(),blockResult.getBlockName(),blockResult.getBlockRate());
                                int dragon = 0;
                                for (String stockCode : detailList) {
                                    Boolean isUpper = stockUpperMap.get(stockCode + SymbolConstants.UNDERLINE + preMin);
                                    if(isUpper!=null && isUpper){
                                        log.info("已有一个票涨停 stockCode{} kbarDate{}",stockCode,kbarDate);
                                        dragon++;
                                    }
                                }

                                List<String> detail300List = detailList.stream().filter(item -> item.startsWith("3")).collect(Collectors.toList());
                                int dragon300 = 0;
                                for (String stockCode : detail300List) {
                                    Boolean isUpper = stockUpperMap.get(stockCode + SymbolConstants.UNDERLINE + preMin);
                                    if(isUpper!=null && isUpper){
                                        log.info("已有一个300票涨停 stockCode{} kbarDate{}",stockCode,kbarDate);
                                        dragon300++;
                                    }
                                }
                                PositionBlockDTO exportDTO = new PositionBlockDTO();
                                exportDTO.setPlankInfo(stockPosition.getPlankInfo());
                                exportDTO.setDragonNum(dragon+1);
                                if(stockPosition.getStockCode().startsWith("3")){
                                    exportDTO.setDragonNum300(dragon300+1);
                                }else {
                                }
                                exportDTO.setBlockCode(blockResult.getBlockCode());
                                exportDTO.setBlockName(blockResult.getBlockName());
                                exportDTO.setBlockRate(blockResult.getBlockRate());
                                exportDTO.setCompareNum(i+1);
                                exportDTO.setStockCode(stockPosition.getStockCode());
                                exportDTO.setPremium(stockPosition.getPremium());
                                exportDTO.setPremiumRate(stockPosition.getPremiumRate());
                                exportDTO.setStockName(stockPosition.getStockName());
                                exportDTO.setSealType(stockPosition.getSealType());
                                exportDTO.setOrderTime(DateUtil.format(stockPosition.getOrderTime(),DateUtil.HH_MM));
                                exportDTO.setBlockOpenRate(blockResult.getOpenRate());
                                exportDTO.setKbarDate(positionKbarDate);
                                resultList.add(exportDTO);
                                break;
                            }
                        }
                    }
                }

            }
            ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\市场主流板块带龙头排名回测.xls");

        } catch (Exception e) {
            throw new BusinessException("文件解析及同步异常", e);
        }
    }

    @Data
    class BlockResult{

        private String blockCode;

        private String blockName;

        private BigDecimal blockRate;

        private BigDecimal openRate;

        public BlockResult( String blockCode, String blockName, BigDecimal blockRate , BigDecimal openRate) {
            this.blockCode = blockCode;
            this.blockName = blockName;
            this.blockRate = blockRate;
            this.openRate = openRate;
        }
    }
}
