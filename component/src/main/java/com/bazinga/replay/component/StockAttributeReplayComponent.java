package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAttributeReplay;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAttributeReplayService;
import com.bazinga.replay.service.StockCommonReplayService;
import com.bazinga.replay.service.StockKbarService;
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
                StockAttributeReplay stockAttributeReplay = new StockAttributeReplay();
                stockAttributeReplay.setStockCode(circulateInfo.getStockCode());
                stockAttributeReplay.setStockName(circulateInfo.getStockName());
                stockAttributeReplay.setKbarDate(currentKbarDate);
                stockAttributeReplay.setUniqueKey(circulateInfo.getStockCode() + "_" + currentKbarDate);
                stockAttributeReplay.setAvgRangeDay10(avgRangeDay10);
                stockAttributeReplay.setCreateTime(new Date());
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
