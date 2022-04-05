package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.StockBollingReplayDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockBolling;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockBollingService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.PriceUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BollingReplayComponent {


    @Autowired
    private StockBollingService stockBollingService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    public void replay() throws Exception {
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
    //    circulateInfos = circulateInfos.stream().filter(item-> "000029".equals(item.getStockCode())).collect(Collectors.toList());
        List<StockBollingReplayDTO> resultList = new ArrayList<>();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20200101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<30){
                continue;
            }
            for (int i = 30; i < stockKbarList.size()-1; i++) {

                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                StockKbar pre1stockKbar = stockKbarList.get(i-1);
                StockKbar pre2StockKbar = stockKbarList.get(i-2);
                StockKbar pre3StockKbar = stockKbarList.get(i-3);
                StockKbar pre8StockKbar = stockKbarList.get(i-8);

                BigDecimal closeRelativeOpenRate = PriceUtil.getPricePercentRate(pre1stockKbar.getClosePrice().subtract(pre1stockKbar.getOpenPrice()),pre2StockKbar.getClosePrice());

                if(closeRelativeOpenRate.compareTo(new BigDecimal("0.5"))>=0){
                    continue;
                }

                BigDecimal pre3to7 = PriceUtil.getPricePercentRate(pre3StockKbar.getClosePrice().subtract(pre8StockKbar.getClosePrice()),pre8StockKbar.getClosePrice());

                if(pre3to7.compareTo(new BigDecimal("5"))<0){
                    continue;
                }

                BigDecimal total2Brand = BigDecimal.ZERO;
                BigDecimal total5Brand = BigDecimal.ZERO;

                for (int j = i-1; j >=i-8; j--) {
                    StockKbar tempKbar = stockKbarList.get(j);
                    String uniqueKey  = tempKbar.getStockCode() + SymbolConstants.UNDERLINE + tempKbar.getKbarDate() + SymbolConstants.UNDERLINE + 20;
                    StockBolling stockBolling = stockBollingService.getByUniqueKey(uniqueKey);
                    if(stockBolling == null ){
                        log.info("未找到布林带信息stockCode{} kbarDate{}",tempKbar.getStockCode(),tempKbar.getKbarDate());
                        throw new Exception();
                    }
                    if(j>=i-2){
                        total2Brand = total2Brand.add(stockBolling.getUpPrice().subtract(stockBolling.getLowPrice()));
                    }else {
                        total5Brand = total5Brand.add(stockBolling.getUpPrice().subtract(stockBolling.getLowPrice()));
                    }
                }

                BigDecimal avg2Brand = total2Brand.divide(new BigDecimal("2"),4, RoundingMode.HALF_UP);
                if(avg2Brand.compareTo(BigDecimal.ZERO) ==0){
                    log.info("avg2Brand0 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                BigDecimal avg5Brand = total5Brand.divide(new BigDecimal("5"),4, RoundingMode.HALF_UP);
                if(avg5Brand.compareTo(BigDecimal.ZERO) ==0){
                    log.info("avg5Brand0 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                if(avg2Brand.divide(avg5Brand,4,RoundingMode.HALF_UP).compareTo(new BigDecimal("0.7"))<0){
                    log.info("满足买入条件stockCode{} kbarDate{}" ,stockKbar.getStockCode(),stockKbar.getKbarDate());
                    StockBollingReplayDTO exportDTO = new StockBollingReplayDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                    exportDTO.setBuyPrice(stockKbar.getOpenPrice());
                    exportDTO.setBuyTime("09:25");
                    exportDTO.setBuyRate(PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(pre1stockKbar.getClosePrice()),pre1stockKbar.getClosePrice()));
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellStockKbar.getClosePrice().subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    resultList.add(exportDTO);
                }
            }
        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\布林带宽缩量0.7买入.xls");

    }
}
