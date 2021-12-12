package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.SuddenAbsortDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SuddenAbsortComponent {

    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;


    public void replay(){
        Map<String, List<String>> lowRateMap = getLowRateMap();
        Map<String, List<String>> suddenMap = getSuddenMap();
        List<SuddenAbsortDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> !item.getStockCode().startsWith("3")).collect(Collectors.toList());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query= new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
         //   query.setStockCode("000503");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            kbarList = kbarList.stream().filter(item->item.getTradeQuantity()>0).collect(Collectors.toList());

            for (int i = 11; i < kbarList.size()-2; i++) {
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar stockKbar = kbarList.get(i);
                StockKbar buyStockKbar = kbarList.get(i+1);
                StockKbar sellStockKbar = kbarList.get(i+2);

                List<StockKbar> subList = kbarList.subList(i - 8, i);
                List<StockKbar> rateList = kbarList.subList(i - 11, i);
                if(!PriceUtil.isSuddenPrice(stockKbar.getStockCode(),stockKbar.getLowPrice(),preStockKbar.getClosePrice())){
                    continue;
                }
                if(PriceUtil.isSuddenPrice(buyStockKbar.getStockCode(),buyStockKbar.getClosePrice(),stockKbar.getClosePrice())&&
                    buyStockKbar.getHighPrice().compareTo(buyStockKbar.getClosePrice())==0){
                    continue;
                }

                Integer plank = calPlank(rateList);
                if(plank<3){
                    continue;
                }
                log.info("满足买入条件stockCode{} stockName{}", buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                List<String> suddendayList = lowRateMap.get(stockKbar.getKbarDate());
                List<String> preSuddendayList = lowRateMap.get(preStockKbar.getKbarDate());
                Integer suddenCount = CollectionUtils.isEmpty(suddendayList)?0:suddendayList.size();
                Integer sudden2Count = CollectionUtils.isEmpty(preSuddendayList)?0:preSuddendayList.size();

                List<String> suddenList = suddenMap.get(stockKbar.getKbarDate());
                List<String> preSuddenList = suddenMap.get(preStockKbar.getKbarDate());
                Integer sudden = CollectionUtils.isEmpty(suddenList)?0:suddenList.size();
                Integer sudden2 = CollectionUtils.isEmpty(preSuddenList)?0:preSuddenList.size();
                SuddenAbsortDTO exportDTO = new SuddenAbsortDTO();
                exportDTO.setStockCode(buyStockKbar.getStockCode());
                exportDTO.setStockName(buyStockKbar.getStockName());
                exportDTO.setBuykbarDate(buyStockKbar.getKbarDate());
                exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(rateList,10));
                exportDTO.setSuddenClose(stockKbar.getClosePrice().compareTo(stockKbar.getLowPrice())==0?1:0);
                exportDTO.setSuddenCloseRate(PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                exportDTO.setDay1LowRateCount(suddenCount);
                exportDTO.setDay2LowRateCount(sudden2Count);
                exportDTO.setDay1suddenCount(sudden);
                exportDTO.setDay2suddenCount(sudden2);
                BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                exportDTO.setBuyPrice(buyStockKbar.getOpenPrice());
                exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                exportDTO.setPlankHigh(plank);
                exportDTO.setLastSuddenTradeAmount(stockKbar.getTradeAmount());
                resultList.add(exportDTO);
            }



        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\10日板高跌停次日低吸.xls");
    }

    private Integer calPlank(List<StockKbar> stockKbarList) {
        int planks = 0;
        for (int i = stockKbarList.size() - 1; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
            } else {
            }

        }
        return planks;

    }


    public  Map<String,List<String>>  getLowRateMap(){
        Map<String,List<String>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<2){
                continue;
            }
            for (int i = 1; i < kbarList.size(); i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice());
                if(closeRate.compareTo(new BigDecimal("-8"))<0){
                    List<String> lowList = resultMap.get(stockKbar.getKbarDate());
                    if(lowList == null){
                        lowList = new ArrayList<>();
                        resultMap.put(stockKbar.getKbarDate(),lowList);
                    }
                    lowList.add(stockKbar.getStockCode());
                }
            }
        }

        return resultMap;
    }

    public  Map<String,List<String>>  getSuddenMap(){
        Map<String,List<String>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<2){
                continue;
            }
            for (int i = 1; i < kbarList.size(); i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                if(PriceUtil.isSuddenPrice(stockKbar.getStockCode(),stockKbar.getClosePrice(),preStockKbar.getClosePrice())){
                    List<String> suddenList = resultMap.get(stockKbar.getKbarDate());
                    if(suddenList == null){
                        suddenList = new ArrayList<>();
                        resultMap.put(stockKbar.getKbarDate(),suddenList);
                    }
                    suddenList.add(stockKbar.getStockCode());
                }
            }
        }

        return resultMap;
    }


}
