package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.Month2RateDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class Month2RateReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;

    public void invokeStrategy(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<Month2RateDTO> resultList = Lists.newArrayList();
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20201001");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<40){
                continue;
            }

            for (int i = 40; i < kbarList.size()-1; i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar sellStockKbar = kbarList.get(i+1);
                StockKbar preStockKbar = kbarList.get(i - 1);
                List<StockKbar> subKbarList = kbarList.subList(i - 40, i);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                StockKbar highKbar = getMaxKbar(subKbarList);
                BigDecimal highPrice = highKbar.getAdjHighPrice();
                BigDecimal nDaysUpperRate = StockKbarUtil.getNDaysUpperRate(subKbarList, 40);
                if(nDaysUpperRate.compareTo(new BigDecimal("40"))<0){
                    continue;
                }

                if(stockKbar.getAdjHighPrice().compareTo(highPrice)>0){
                    continue;
                }

                log.info("满足买入条件 stockCode{} date{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                Month2RateDTO exportDTO = new Month2RateDTO();
                exportDTO.setStockCode(stockKbar.getStockCode());
                exportDTO.setStockName(stockKbar.getStockName());
                exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                exportDTO.setBuyPrice(stockKbar.getHighPrice());
                exportDTO.setSellkbarDate(sellStockKbar.getKbarDate());
               // exportDTO.setDaysToHigh();
                exportDTO.setHighPriceRate(PriceUtil.getPricePercentRate(highPrice.subtract(subKbarList.get(0).getAdjClosePrice()),subKbarList.get(0).getAdjClosePrice()));
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                exportDTO.setPremium(PriceUtil.getPricePercentRate(avgPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                resultList.add(exportDTO);


                ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\涨幅过高未突破买入回测.xls");

            }



        }

    }

    private StockKbar getMaxKbar(List<StockKbar> subKbarList) {
       return subKbarList.stream().max(Comparator.comparing(StockKbar::getAdjHighPrice)).get();

    }


}
