package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.BestAvgDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BestAvgLineReplayComponent {

    @Autowired
    private StockAverageLineService stockAverageLineService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private StockKbarService stockKbarService;

    public void invokeStrategy(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            replay(circulateInfo.getStockCode());
        }
    }

    public void replay(String stockCode){
        List<BestAvgDTO> resultList = Lists.newArrayList();
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20200101",DateUtil.yyyyMMdd));
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        for (int days = 0; days < 61; days++) {

            boolean isPosition = false;
            BestAvgDTO exportDTO = null;
            for (TradeDatePool tradeDatePool : tradeDatePools) {
                String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                String kbarUniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate;
                String avgUniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate + SymbolConstants.UNDERLINE + days;
                StockKbar currentKbar = stockKbarService.getByUniqueKey(kbarUniqueKey);
                StockAverageLine averageLine = stockAverageLineService.getByUniqueKey(avgUniqueKey);
                if(currentKbar ==null || averageLine ==null ){
                    continue;
                }
                if(isPosition){
                    if(currentKbar.getAdjClosePrice().compareTo(averageLine.getAveragePrice()) <=0 ){
                        log.info("满足卖出条件 stockCode{} kbarDate{}",stockCode,currentKbar.getKbarDate());
                        exportDTO.setSellkbarDate(kbarDate);
                        exportDTO.setSellPrice(currentKbar.getAdjClosePrice());
                        exportDTO.setPremium(PriceUtil.getPricePercentRate(exportDTO.getSellPrice().subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                        resultList.add(exportDTO);
                        isPosition = false;
                    }
                }else {
                    if(currentKbar.getAdjClosePrice().compareTo(averageLine.getAveragePrice()) >0 ){
                        log.info("满足买入条件 stockCode{} kbarDate{}",stockCode,currentKbar.getKbarDate());
                        exportDTO = new BestAvgDTO();
                        exportDTO.setBuykbarDate(kbarDate);
                        exportDTO.setAvgDays(days);
                        exportDTO.setStockCode(currentKbar.getStockCode());
                        exportDTO.setStockName(currentKbar.getStockName());
                        exportDTO.setBuyPrice(currentKbar.getAdjClosePrice());
                        isPosition = true;
                    }
                }
            }
        }
        if(!CollectionUtils.isEmpty(resultList)){
            ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\均线\\均线买入stockCode"+stockCode+".xls");
        }
    }


}
