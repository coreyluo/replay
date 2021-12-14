package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.SuddenAbsortDTO;
import com.bazinga.dto.SunPlankAbsortDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PrePlankAbsortComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){

        List<SunPlankAbsortDTO> resultList = Lists.newArrayList();

        Map<String, Long> circulateZMap = getCirculateZMap();
        Map<String, List<String>> upperMap = getUpperMap();
        Map<String, List<String>> sunPlankMap = getSunPlankMap();
        log.info("");

        sunPlankMap.forEach((kbarDate,stockList)->{
            List<StockOpenAmountDTO> openAmountList = Lists.newArrayList();

            Date afterTradeDate = commonComponent.afterTradeDate(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd));
            Date sellTradeDate = commonComponent.afterTradeDate(afterTradeDate);


            for (String stockCode : stockList) {

                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockCode, afterTradeDate);
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                ThirdSecondTransactionDataDTO open = list.get(0);
                openAmountList.add(new StockOpenAmountDTO(stockCode, open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity().toString())).multiply(CommonConstant.DECIMAL_HUNDRED)));
            }

            List<StockOpenAmountDTO> sortList = openAmountList.stream().sorted(Comparator.comparing(StockOpenAmountDTO::getOpenAmount).reversed()).collect(Collectors.toList());
            int toIndex = sortList.size()>15?15:sortList.size();

            for (int i = 0; i < toIndex; i++) {
                StockOpenAmountDTO stockOpenAmountDTO = sortList.get(i);
                log.info("满足买入条件 stockCode{} kbarDate{}", stockOpenAmountDTO.getStockCode(),kbarDate);

                StockKbarQuery query = new StockKbarQuery();
                query.setStockCode(stockOpenAmountDTO.getStockCode());
                query.setKbarDateTo(DateUtil.format(afterTradeDate,DateUtil.yyyyMMdd));
                query.addOrderBy("kbar_date", Sort.SortType.DESC);
                query.setLimit(10);
                List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<3){
                    continue;
                }
                kbarList = Lists.reverse(kbarList);
                SunPlankAbsortDTO exportDTO = new SunPlankAbsortDTO();
                if(kbarList.size()==10){
                    PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(kbarList.subList(0, kbarList.size()-1));
                    exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    exportDTO.setUnPlankHigh(plankHighDTO.getUnPlank());
                }
                StockKbar buyStockKbar = kbarList.get(kbarList.size() - 1);
                StockKbar preStockKbar = kbarList.get(kbarList.size() - 2);
                StockKbar prepreStockKbar = kbarList.get(kbarList.size() - 3);
                exportDTO.setStockCode(stockOpenAmountDTO.getStockCode());
                exportDTO.setCompeteNum(i+1);
                exportDTO.setStockName(buyStockKbar.getStockName());
                exportDTO.setBuykbarDate(buyStockKbar.getKbarDate());
                exportDTO.setBuyPrice(buyStockKbar.getOpenPrice());
                exportDTO.setBuyRate(PriceUtil.getPricePercentRate(buyStockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                exportDTO.setPreTradeAmount(preStockKbar.getTradeAmount());
                exportDTO.setOpenRate(PriceUtil.getPricePercentRate(buyStockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                exportDTO.setOpenTradeAmount(stockOpenAmountDTO.getOpenAmount());
                exportDTO.setPreOpenRate(PriceUtil.getPricePercentRate(preStockKbar.getOpenPrice().subtract(prepreStockKbar.getClosePrice()),prepreStockKbar.getClosePrice()));
                exportDTO.setCirculateZ(circulateZMap.get(buyStockKbar.getStockCode()));
                exportDTO.setCirculateAmountZ(buyStockKbar.getOpenPrice().multiply(new BigDecimal(exportDTO.getCirculateZ().toString())));
                List<String> upperList = upperMap.get(kbarDate);
                exportDTO.setPrePlankNum(upperList.size());

                BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(buyStockKbar.getStockCode(), DateUtil.format(sellTradeDate, DateUtil.yyyyMMdd));
                if(sellPrice!=null){
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                }
                resultList.add(exportDTO);
            }

        });
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\阳线板次日集合低吸.xls");

    }

    @Data
    class StockOpenAmountDTO {
        private String stockCode;

        private BigDecimal openAmount;
        public StockOpenAmountDTO(String stockCode, BigDecimal openAmount) {
            this.stockCode = stockCode;
            this.openAmount = openAmount;
        }
    }

    private Map<String ,Long > getCirculateZMap(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, Long > resultMap = new HashMap<>();
        for (CirculateInfo circulateInfo : circulateInfos) {
            resultMap.put(circulateInfo.getStockCode(),circulateInfo.getCirculateZ());
        }

        return resultMap;
    }


    private Map<String, List<String>> getSunPlankMap(){
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
                if(StockKbarUtil.isUpperPrice(stockKbar,preStockKbar) && stockKbar.getOpenPrice().compareTo(stockKbar.getClosePrice())<0){
                    List<String> sunPlankList = resultMap.get(stockKbar.getKbarDate());
                    if(sunPlankList == null){
                        sunPlankList = new ArrayList<>();
                        resultMap.put(stockKbar.getKbarDate(),sunPlankList);
                    }
                    sunPlankList.add(stockKbar.getStockCode());
                }
            }
        }
        return resultMap;
    }


    public  Map<String,List<String>>  getUpperMap(){
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
                if(PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getClosePrice(),preStockKbar.getClosePrice())){
                    List<String> upperList = resultMap.get(stockKbar.getKbarDate());
                    if(upperList == null){
                        upperList = new ArrayList<>();
                        resultMap.put(stockKbar.getKbarDate(),upperList);
                    }
                    upperList.add(stockKbar.getStockCode());
                }
            }
        }

        return resultMap;
    }

}
