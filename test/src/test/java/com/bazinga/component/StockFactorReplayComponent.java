package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.StockFactorExportDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockFactor;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockFactorQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockFactorService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.PremiumUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.CSVUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.util.TdxHqUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class StockFactorReplayComponent {

    @Autowired
    private StockFactorService stockFactorService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    public void replay(String kbarDateFrom ,String kbarDateTo){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        List<List<Object>> resultList = Lists.newArrayList();

        Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(kbarDateFrom, DateUtil.yyyyMMdd));

        Date afterTradeDate10 = commonComponent.afterTradeDate(DateUtil.parseDate(kbarDateTo, DateUtil.yyyyMMdd), 11);

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));
            if(afterTradeDate10!=null){
                query.setKbarDateTo(DateUtil.format(afterTradeDate10,DateUtil.yyyyMMdd));
            }
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            for (int i = 1; i < stockKbarList.size(); i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);

                Date tradeDate = DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd);
                if(tradeDate.after(DateUtil.parseDate(kbarDateTo, DateUtil.yyyyMMdd))){
                    continue;
                }
                if(PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(),stockKbar.getOpenPrice(),preStockKbar.getClosePrice(),stockKbar.getKbarDate())){
                    continue;
                }
                String uniqueKey = DateUtil.format(DateUtil.parseDate(preStockKbar.getKbarDate(),DateUtil.yyyyMMdd),DateUtil.yyyy_MM_dd)
                        + SymbolConstants.UNDERLINE + preStockKbar.getStockCode();
                StockFactor stockFactor = stockFactorService.getByUniqueKey(uniqueKey);
                StockFactorExportDTO exportDTO = new StockFactorExportDTO();

                List<Object> objectList = new ArrayList<>();
                objectList.add(stockKbar.getStockCode());
                objectList.add(stockKbar.getStockName());
                objectList.add(stockKbar.getKbarDate());
                objectList.add(preStockKbar.getKbarDate());
                if(stockFactor == null){
                    log.info("未查询到因子对象 uniqueKey {}",uniqueKey);
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                    objectList.add("");
                }else {
                    objectList.add(stockFactor.getIndex1());
                    objectList.add(stockFactor.getIndex2a());
                    objectList.add(stockFactor.getIndex2b());
                    objectList.add(stockFactor.getIndex2c());
                    objectList.add(stockFactor.getIndex3());
                    objectList.add(stockFactor.getIndex4());
                    objectList.add(stockFactor.getIndex5());
                    objectList.add(stockFactor.getIndex6());
                    objectList.add(stockFactor.getIndex7());
                }
                for (int j = 1; j <= 10; j++) {
                    if(i+11<stockKbarList.size()){
                        BigDecimal premium = PremiumUtil.calDaysProfit(stockKbarList.subList(i,i+11),j);
                        objectList.add(premium);
                    }
                }
                resultList.add(objectList);
            }
        }
        List<Object> headList = getHeadList();

        CSVUtil.createCSVFile(headList,resultList,"E:\\trendData","因子收益样例"+kbarDateFrom+"_"+kbarDateTo);
    }

    private List<Object> getHeadList(){
        List<Object> headList = new ArrayList<>();

        headList.add("股票代码");
        headList.add("股票名称");
        headList.add("买入日期");
        headList.add("因子日期");
        headList.add("因子1");
        headList.add("因子2a");
        headList.add("因子2b");
        headList.add("因子2c");
        headList.add("因子3");
        headList.add("因子4");
        headList.add("因子5");
        headList.add("因子6");
        headList.add("因子7");
        headList.add("1日收益");
        headList.add("2日收益");
        headList.add("3日收益");
        headList.add("4日收益");
        headList.add("5日收益");
        headList.add("6日收益");
        headList.add("7日收益");
        headList.add("8日收益");
        headList.add("9日收益");
        headList.add("10日收益");
        return headList;
    }


    public void factorOutPut(String kbarDateFrom ,String kbarDateTo){
        StockFactorQuery stockFactorQuery = new StockFactorQuery();
        stockFactorQuery.setLimit(50000000);
        List<StockFactor> stockFactors = stockFactorService.listByCondition(stockFactorQuery);

        Map<String,List<StockFactor>> tempMap = new HashMap<>();
        for (StockFactor stockFactor : stockFactors) {
            BigDecimal sumValue = BigDecimal.ZERO;
            if(stockFactor.getIndex1()!=null){
                sumValue = sumValue.add(stockFactor.getIndex1());
            }if(stockFactor.getIndex2a()!=null){
                sumValue = sumValue.add(stockFactor.getIndex2a());
            }if(stockFactor.getIndex2b()!=null){
                sumValue = sumValue.add(stockFactor.getIndex2b());
            }if(stockFactor.getIndex2c()!=null){
                sumValue = sumValue.add(stockFactor.getIndex2c());
            }if(stockFactor.getIndex3()!=null){
                sumValue = sumValue.add(stockFactor.getIndex3());
            }if(stockFactor.getIndex4()!=null){
                sumValue = sumValue.add(stockFactor.getIndex4());
            }if(stockFactor.getIndex5()!=null){
                sumValue = sumValue.add(stockFactor.getIndex5());
            }if(stockFactor.getIndex6()!=null){
                sumValue = sumValue.add(stockFactor.getIndex6());
            }if(stockFactor.getIndex7()!=null){
                sumValue = sumValue.add(stockFactor.getIndex7());
            }
            stockFactor.setSumValue(sumValue);
            List<StockFactor> list = tempMap.computeIfAbsent(stockFactor.getKbarDate(), k -> new ArrayList<>());
            list.add(stockFactor);
        }
        List<List<Object>> resultList = new ArrayList<>();
        tempMap.forEach((kbarDate,list)->{
            List<StockFactor> sortList = list.stream().sorted(Comparator.comparing(StockFactor::getSumValue).reversed()).collect(Collectors.toList());
            List<Object> objectList = new ArrayList<>();
            for (int i = 0; i <200; i++) {
                StockFactor stockFactor = sortList.get(i);
                objectList.add(stockFactor.getStockCode());
                objectList.add(stockFactor.getStockName());
                objectList.add(stockFactor.getKbarDate());
                objectList.add(stockFactor.getIndex1());
                objectList.add(stockFactor.getIndex2a());
                objectList.add(stockFactor.getIndex2b());
                objectList.add(stockFactor.getIndex2c());
                objectList.add(stockFactor.getIndex3());
                objectList.add(stockFactor.getIndex4());
                objectList.add(stockFactor.getIndex5());
                objectList.add(stockFactor.getIndex6());
                objectList.add(stockFactor.getIndex7());
                resultList.add(objectList);
            }
            log.info("");
        });
        List<Object> headList = getHeadList();
        CSVUtil.createCSVFile(headList,resultList,"E:\\trendData","因子总和排名前200");

    }

    private List<Object> getSortHeadList(){
        List<Object> headList = new ArrayList<>();

        headList.add("股票代码");
        headList.add("股票名称");
        headList.add("因子日期");
        headList.add("因子1");
        headList.add("因子2a");
        headList.add("因子2b");
        headList.add("因子2c");
        headList.add("因子3");
        headList.add("因子4");
        headList.add("因子5");
        headList.add("因子6");
        headList.add("因子7");
        return headList;
    }

}
