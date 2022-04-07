package com.bazinga.replay.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.DateFormatUtils;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class StockBollingComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private StockAverageLineService stockAverageLineService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private StockBollingService stockBollingService;

    @Autowired
    private CommonComponent commonComponent;

    public void  batchInitBoll(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            initBoll(circulateInfo.getStockCode());
        }
    }

    public void calCurrentDayBoll(Date date){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            String stockCode = circulateInfo.getStockCode();
            String kbarDate = DateFormatUtils.format(date, DateUtil.yyyyMMdd);
            String uniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate +SymbolConstants.UNDERLINE + 5;
            StockAverageLine byUniqueKey = stockAverageLineService.getByUniqueKey(uniqueKey);
            if(byUniqueKey!=null){
                double std = calStd(stockCode, kbarDate, 5);
                if(std<0){
                    continue;
                }
                BigDecimal stdDecimal = new BigDecimal(std).setScale(2,RoundingMode.HALF_UP);
                log.info("stockCode{} kbarDate{} std{}",stockCode,kbarDate ,std);
                StockBolling stockBolling = new StockBolling();
                stockBolling.setStockCode(byUniqueKey.getStockCode());
                stockBolling.setStockName(byUniqueKey.getStockName());
                stockBolling.setDayType(5);
                stockBolling.setMiddlePrice(byUniqueKey.getAveragePrice());
                BigDecimal up = stockBolling.getMiddlePrice().add(stdDecimal.multiply(new BigDecimal("2"))).setScale(2,RoundingMode.HALF_UP);
                BigDecimal low = stockBolling.getMiddlePrice().subtract(stdDecimal.multiply(new BigDecimal("2"))).setScale(2,RoundingMode.HALF_UP);
                stockBolling.setUpPrice(up);
                stockBolling.setLowPrice(low);
                stockBolling.setKbarDate(byUniqueKey.getKbarDate());
                stockBolling.setUniqueKey(uniqueKey);
                stockBollingService.save(stockBolling);
            }
        }


    }



    public  void initBoll(String stockCode){
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(new TradeDatePoolQuery());
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateFormatUtils.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            String uniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate +SymbolConstants.UNDERLINE + 5;
            StockAverageLine byUniqueKey = stockAverageLineService.getByUniqueKey(uniqueKey);
            if(byUniqueKey!=null){
                double std = calStd(stockCode, kbarDate, 5);
                if(std<0){
                    continue;
                }
                BigDecimal stdDecimal = new BigDecimal(std).setScale(2,RoundingMode.HALF_UP);
                log.info("stockCode{} kbarDate{} std{}",stockCode,kbarDate ,std);
                StockBolling stockBolling = new StockBolling();
                stockBolling.setStockCode(byUniqueKey.getStockCode());
                stockBolling.setStockName(byUniqueKey.getStockName());
                stockBolling.setDayType(5);
                stockBolling.setMiddlePrice(byUniqueKey.getAveragePrice());
                BigDecimal up = stockBolling.getMiddlePrice().add(stdDecimal.multiply(new BigDecimal("2"))).setScale(2,RoundingMode.HALF_UP);
                BigDecimal low = stockBolling.getMiddlePrice().subtract(stdDecimal.multiply(new BigDecimal("2"))).setScale(2,RoundingMode.HALF_UP);
                stockBolling.setUpPrice(up);
                stockBolling.setLowPrice(low);
                stockBolling.setKbarDate(byUniqueKey.getKbarDate());
                stockBolling.setUniqueKey(uniqueKey);
                stockBollingService.save(stockBolling);
            }

        }
    }

    public  double calStd(String stockCode, String kbarDate , int days){
        StockKbarQuery query = new StockKbarQuery();
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        query.setStockCode(stockCode);
        query.setKbarDateTo(kbarDate);
        query.setLimit(days);
        List<StockKbar> stockAverageLines = stockKbarService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockAverageLines) || stockAverageLines.size() <5){
            return -1d;
        }
        BigDecimal avgPrice = stockAverageLines.stream().map(StockKbar::getAdjClosePrice).reduce(BigDecimal::add).get()
                .divide(new BigDecimal(String.valueOf(days)),2,BigDecimal.ROUND_HALF_UP);

        BigDecimal total = BigDecimal.ZERO;
        for (StockKbar stockKbar : stockAverageLines) {
            total = total.add(stockKbar.getAdjClosePrice().subtract(avgPrice).pow(2));
        }
        log.info("stockCode{} kbarDate{} total{} days{}",stockCode,kbarDate ,total,days);

        return Math.sqrt(total.divide(new BigDecimal(String.valueOf(days-1)),10,BigDecimal.ROUND_HALF_UP).doubleValue());
    }


}
