package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class StockAttributeReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockAttributeReplayService stockAttributeReplayService;

    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private StockAverageLineService stockAverageLineService;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;


    public void saveStockAttributeReplay(Date date){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Date currentDate = date;
        if(!commonComponent.isTradeDate(currentDate)){
            log.info("当前日期不是交易日期");
            return;
        }
        String currentKbarDate = DateUtil.format(currentDate,DateUtil.yyyyMMdd);
        for (CirculateInfo circulateInfo : circulateInfos) {
            try {
                List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode());
                BigDecimal avgRangeDay10 = calAvgRangeDay10(stockKBars);
                BigDecimal rateDay5 = calRateDay5(stockKBars);
                BigDecimal rateDay3 = calRateDay3(stockKBars);
                boolean marketNewFlag = isMarketNew(circulateInfo.getStockCode());
                Integer markerNew  = 0;
                if(marketNewFlag){
                    markerNew = 1;
                }
                BigDecimal marketValue = calMarketValue(stockKBars, circulateInfo);
                Integer planksDay10 = calPlanksDay10(stockKBars, circulateInfo.getStockCode());
                Integer closePlanksDay10 = calPlanksEndDay10(stockKBars, circulateInfo.getStockCode());
                BigDecimal highRate = calHighRate(stockKBars);
                BigDecimal upperShadowRate = calUpperShadowRate(stockKBars);
                BigDecimal avgRate5 = calAvgRate5(stockKBars, circulateInfo);
                String highTime = calHighTime(circulateInfo.getStockCode());
                BigDecimal avgAmountDay5 = calAvgAmountDay5(stockKBars);

                StockAttributeReplay stockAttributeReplay = new StockAttributeReplay();
                stockAttributeReplay.setStockCode(circulateInfo.getStockCode());
                stockAttributeReplay.setStockName(circulateInfo.getStockName());
                stockAttributeReplay.setKbarDate(currentKbarDate);
                stockAttributeReplay.setUniqueKey(circulateInfo.getStockCode() + "_" + currentKbarDate);
                stockAttributeReplay.setAvgRangeDay10(avgRangeDay10);
                stockAttributeReplay.setAvgAmountDay5(avgAmountDay5);
                stockAttributeReplay.setRateDay5(rateDay5);
                stockAttributeReplay.setRateDay3(rateDay3);
                stockAttributeReplay.setMarketNew(markerNew);
                stockAttributeReplay.setMarketValue(marketValue);
                stockAttributeReplay.setPlanksDay10(planksDay10);
                stockAttributeReplay.setClosePlanksDay10(closePlanksDay10);
                stockAttributeReplay.setHighRate(highRate);
                stockAttributeReplay.setUpperShadowRate(upperShadowRate);
                stockAttributeReplay.setAvgRate5(avgRate5);
                stockAttributeReplay.setCreateTime(new Date());
                stockAttributeReplay.setHighTime(highTime);
                stockAttributeReplayService.save(stockAttributeReplay);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }

    public BigDecimal calAvgRangeDay10(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<11){
            return null;
        }
        int i = 0;
        BigDecimal totalRange = BigDecimal.ZERO;
        StockKbar nextKbar = null;
        BigDecimal maxRange = null;
        BigDecimal minRange = null;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(nextKbar!=null&&i<=11){
                BigDecimal range = PriceUtil.getPricePercentRate(nextKbar.getAdjHighPrice().subtract(nextKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                totalRange = totalRange.add(range);
                if(maxRange==null||range.compareTo(maxRange)==1){
                    maxRange = range;
                }
                if(minRange==null||range.compareTo(minRange)==-1){
                    minRange = range;
                }
            }
            nextKbar = stockKbar;
        }
        totalRange = totalRange.subtract(maxRange).subtract(minRange);
        BigDecimal avgRange = totalRange.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
        return avgRange;
    }

    public BigDecimal calAvgAmountDay5(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<11){
            return null;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < 5; i++) {
            StockKbar stockKbar = stockKbars.get(i);
            totalAmount = totalAmount.add(stockKbar.getTradeAmount());
        }
        return totalAmount.divide(new BigDecimal("5"),1,BigDecimal.ROUND_HALF_UP);
    }


    public BigDecimal calRateDay5(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<6){
            return null;
        }
        int i = 0;
        StockKbar firstKbar = null;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(i==1){
                firstKbar = stockKbar;
            }
            if(i==6){
                BigDecimal rateDay5 = PriceUtil.getPricePercentRate(firstKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                return rateDay5;
            }
        }
        return null;
    }

    public BigDecimal calRateDay3(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<4){
            return null;
        }
        int i = 0;
        StockKbar firstKbar = null;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(i==1){
                firstKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rateDay3 = PriceUtil.getPricePercentRate(firstKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                return rateDay3;
            }
        }
        return null;
    }

    public Integer calPlanksDay10(List<StockKbar> stockKbars,String stockCode){
        if(CollectionUtils.isEmpty(stockKbars)){
            return null;
        }
        int planks = 0;
        int i = 0;
        StockKbar nextKbar = null;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(nextKbar!=null&&i<=11){
                boolean upperPrice = PriceUtil.isUpperPrice(stockCode, nextKbar.getHighPrice(), stockKbar.getClosePrice());
                if(upperPrice){
                    planks++;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;
    }

    public Integer calPlanksEndDay10(List<StockKbar> stockKbars,String stockCode){
        if(CollectionUtils.isEmpty(stockKbars)){
            return null;
        }
        int planks = 0;
        int i = 0;
        StockKbar nextKbar = null;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(nextKbar!=null&&i<=11){
                boolean upperPrice = PriceUtil.isUpperPrice(stockCode, nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if(upperPrice){
                    planks++;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;
    }

    public BigDecimal calHighRate(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<2){
            return null;
        }
        StockKbar stockKbar = stockKbars.get(0);
        StockKbar preStockKbar = stockKbars.get(1);
        BigDecimal highRate = (stockKbar.getAdjHighPrice().subtract(preStockKbar.getAdjClosePrice())).divide(preStockKbar.getAdjClosePrice(), 4, BigDecimal.ROUND_HALF_UP);
        return highRate;
    }
    public BigDecimal calUpperShadowRate(List<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<1){
            return null;
        }
        StockKbar stockKbar = stockKbars.get(0);
        BigDecimal highRate = (stockKbar.getAdjHighPrice().subtract(stockKbar.getAdjClosePrice())).divide(stockKbar.getAdjClosePrice(), 4, BigDecimal.ROUND_HALF_UP);
        return highRate;
    }

    public BigDecimal calMarketValue(List<StockKbar> stockKbars,CirculateInfo circulateInfo){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<1){
            return null;
        }
        StockKbar stockKbar = stockKbars.get(0);
        BigDecimal marketValue = new BigDecimal(circulateInfo.getCirculate()).multiply(stockKbar.getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return marketValue;
    }

    public BigDecimal calAvgRate5(List<StockKbar> stockKbars,CirculateInfo circulateInfo){
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<1){
            return null;
        }
        StockKbar stockKbar = stockKbars.get(0);
        StockAverageLine stockAverageLine = stockAverageLineService.getByUniqueKey(circulateInfo.getStockCode() + "_" + stockKbar.getKbarDate()+"_"+5);
        if(stockAverageLine!=null){
            return stockAverageLine.getAveragePrice();
        }
        return null;
    }

    public boolean isMarketNew(String stockCode){
        try {
            DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 200);
            List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(securityBars);
            if (kBarDTOS.size() >= 180) {
                return true;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    public String calHighTime(String stockCode){
        List<ThirdSecondTransactionDataDTO> datas = currentDayTransactionDataComponent.getData(stockCode);
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        String highTime = null;
        BigDecimal highPrice = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(highPrice==null||data.getTradePrice().compareTo(highPrice)==1){
                highPrice = data.getTradePrice();
                highTime = data.getTradeTime();
            }
        }
        return highTime;
    }
    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.setStockCode(stockCode);
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
        stockKbarQuery.setLimit(50);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<1){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getTradeQuantity()==null||stockKbar.getTradeQuantity()<100){
                continue;
            }
            list.add(stockKbar);
        }
        return stockKbars;
    }



}
