package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.PremiumUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SelfExcelReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;



    public void replay(){
        File file = new File("E:/excelExport/200w大单突破条件后.xlsx");
        try {
            List<BigOrderExcelDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(BigOrderExcelDTO.class);
            List<BigOrderExcelDTO> resultList = new ArrayList<>();
            Map<String,List<BigOrderExcelDTO>> tempMap = new HashMap<>();
            Map<String,Integer> rankMap = new HashMap<>();
            for (BigOrderExcelDTO bigOrderExcelDTO : importList) {
                String kbarDate = DateUtil.format(bigOrderExcelDTO.getKbarDate(),DateUtil.yyyyMMdd);

                List<BigOrderExcelDTO> list = tempMap.computeIfAbsent(kbarDate, k -> new ArrayList<>());
                list.add(bigOrderExcelDTO);
            }
            tempMap.forEach((kbarDate,list)->{
                list.sort(Comparator.comparing(BigOrderExcelDTO::getTradeTime));
                for (int i = 0; i < list.size(); i++) {
                    String stockCode = list.get(i).getStockCode();
                    rankMap.put(kbarDate + stockCode,i+1);
                }
            });

            for (BigOrderExcelDTO bigOrderExcelDTO : importList) {
                String kbarDate = DateUtil.format(bigOrderExcelDTO.getKbarDate(),DateUtil.yyyyMMdd);
                bigOrderExcelDTO.setRank(rankMap.get(kbarDate+ bigOrderExcelDTO.getStockCode()));
            }

            int times = importList.size()/60000;
            for (int i = 0; i < times + 1; i++) {
                int toIndex = (i+1)* 60000;
                toIndex = toIndex > importList.size()?importList.size():toIndex;
                ExcelExportUtil.exportToFile(importList.subList(i* 60000, toIndex), "E:\\trendData\\200w大单突破条件后增加字段"+i+".xls");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void  replayMarket(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        circulateInfos =circulateInfos.stream().filter(item->item.getStockCode().startsWith("3")).collect(Collectors.toList());
        List<SelfExcelImportDTO> resultList = Lists.newArrayList();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<2){
                continue;
            }
            for (int i = 1; i < kbarList.size(); i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                if(StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)){
                    SelfExcelImportDTO importDTO = new SelfExcelImportDTO();
                    importDTO.setStockName(circulateInfo.getStockName());
                    importDTO.setStockCode(circulateInfo.getStockCode());
                    importDTO.setDragonDate(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd));
                    resultList.add(importDTO);
                }
            }
        }

        replay(resultList);
    }


    public void replayPosition()  {

        File file = new File("E:/excelExport/龙虎/华鑫20211206.xlsx");

        List<SelfExcelImportDTO> importList = null;
        try {
            importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SelfExcelImportDTO.class);
            replay(importList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void replay(List<SelfExcelImportDTO> importList){
        try {
            List<SelfExcelImportDTO> resultList = Lists.newArrayList();
            for (SelfExcelImportDTO importDTO : importList) {
                String stockCode = importDTO.getStockCode().substring(0,6);
                importDTO.setStockCode(stockCode);
               /* if(importDTO.getAbnormalName().startsWith("连续三个交易日")){
                    continue;
                }
                if(!importDTO.getSalesDepartName().contains("华鑫")){
                    continue;
                }*/
                String dragonDate = DateUtil.format(importDTO.getDragonDate(),DateUtil.yyyyMMdd);
                Date buyDate = commonComponent.afterTradeDate(importDTO.getDragonDate());
                Date sellDate = commonComponent.afterTradeDate(buyDate);
                String buyUniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(buyDate,DateUtil.yyyyMMdd);
                StockKbar buyStockKbar = stockKbarService.getByUniqueKey(buyUniqueKey);
                String dragonUniqueKey = stockCode + SymbolConstants.UNDERLINE + dragonDate;
                StockKbar dragonKbar = stockKbarService.getByUniqueKey(dragonUniqueKey);
                if(buyStockKbar ==null || dragonKbar == null){
                    log.info("为获取到K线stockCode{} kbarDate{}",stockCode,DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                    continue;
                }
                log.info("符合买入条件stockCode{} kbarDate{}",stockCode,DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                importDTO.setBuyDate(DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockCode, sellDate);
                List<ThirdSecondTransactionDataDTO> buyList = historyTransactionDataComponent.getData(stockCode, buyDate);
                if(!CollectionUtils.isEmpty(list)){
                    list = historyTransactionDataComponent.getMorningData(list);
                    if(CollectionUtils.isEmpty(list)|| list.size()==1){
                        continue;
                    }
                   // ThirdSecondTransactionDataDTO buyDTO = historyTransactionDataComponent.getFixTimeDataOne(list, "09:36");
                    ThirdSecondTransactionDataDTO open = buyList.get(0);
                   // ThirdSecondTransactionDataDTO transactionDataDTO = buyList.get(9);
                  /*  ThirdSecondTransactionDataDTO fixTimeDataOne = historyTransactionDataComponent.getFixTimeDataOne(buyList, "09:33");
                    if(fixTimeDataOne.getTradePrice().compareTo(open.getTradePrice())<=0){
                        continue;
                    }*/
                    importDTO.setBuyPrice(open.getTradePrice());
                    Float sellPricef = historyTransactionDataComponent.calAveragePrice(list);
                    BigDecimal sellPrice = new BigDecimal(sellPricef.toString());
                    importDTO.setSellDate(DateUtil.format(sellDate,DateUtil.yyyyMMdd));
                    importDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(importDTO.getBuyPrice()),importDTO.getBuyPrice()));
                    importDTO.setOpenTradeAmount(open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity()).multiply(CommonConstant.DECIMAL_HUNDRED)));
                    importDTO.setOpenRate(PriceUtil.getPricePercentRate(open.getTradePrice().subtract(dragonKbar.getClosePrice()),dragonKbar.getClosePrice()));
                    importDTO.setRelativeOpenRate(PriceUtil.getPricePercentRate(open.getTradePrice().subtract(open.getTradePrice()),dragonKbar.getClosePrice()));
                    StockKbarQuery query = new StockKbarQuery();
                    query.setKbarDateTo(dragonDate);
                    query.setStockCode(stockCode);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(11);
                    List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                    if(kbarList.size()<11){
                        continue;
                    }
                    importDTO.setDay3Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,3));
                    importDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,5));
                    importDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,10));
                    PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(Lists.reverse(kbarList));
                    importDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    importDTO.setUnPlank(plankHighDTO.getUnPlank());
                    importDTO.setTradeAmount(dragonKbar.getTradeAmount());
                }
                importDTO.setStockName(buyStockKbar.getStockName());
                resultList.add(importDTO);
            }
            ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\龙虎300封住次日集合买买.xls");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
