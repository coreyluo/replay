package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.ManyCannonDTO;
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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ManyCannonReplayComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    public void replay() {
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        List<ManyCannonDTO> resultList = Lists.newArrayList();

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210201");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            stockKbarList = stockKbarList.stream().filter(item -> item.getTradeQuantity() > 0).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size() < 12) {
                continue;
            }
            for (int i = 11; i < stockKbarList.size() - 1; i++) {

                StockKbar sellStockKbar = stockKbarList.get(i + 1);
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i - 1);
                StockKbar pre2StockKbar = stockKbarList.get(i - 2);
                StockKbar pre3StockKbar = stockKbarList.get(i - 3);
                if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                    continue;
                }
                if (stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice()) >= 0 && preStockKbar.getClosePrice().compareTo(preStockKbar.getOpenPrice()) < 0
                        && pre2StockKbar.getClosePrice().compareTo(pre2StockKbar.getOpenPrice()) >= 0) {
                    if (preStockKbar.getClosePrice().compareTo(pre3StockKbar.getClosePrice()) > 0 && preStockKbar.getOpenPrice().compareTo(pre2StockKbar.getClosePrice()) < 0) {
                        if (stockKbar.getClosePrice().compareTo(pre2StockKbar.getClosePrice()) <= 0) {
                            continue;
                        }
                        if (preStockKbar.getHighPrice().compareTo(pre2StockKbar.getHighPrice()) > 0) {
                            continue;
                        }
                        BigDecimal comparePrice = pre2StockKbar.getLowPrice().compareTo(pre3StockKbar.getClosePrice()) > 0 ? pre3StockKbar.getClosePrice() : pre2StockKbar.getLowPrice();
                        if (pre2StockKbar.getLowPrice().compareTo(comparePrice) < 0) {
                            continue;
                        }
                        if (StockKbarUtil.isPlank(stockKbarList.subList(i - 10, i))) {
                            continue;
                        }
                        log.info("满足买入条件 stockCode{} kbarDate{}", stockKbar.getStockCode(), stockKbar.getKbarDate());


                        ManyCannonDTO exportDTO = new ManyCannonDTO();

                        exportDTO.setStockCode(stockKbar.getStockCode());
                        exportDTO.setBuyKbarDate(stockKbar.getKbarDate());
                        exportDTO.setStockName(stockKbar.getStockName());
                        exportDTO.setBuyPrice(stockKbar.getClosePrice());
                        exportDTO.setCloseRate(PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice()));
                        exportDTO.setPre1CloseRate(PriceUtil.getPricePercentRate(preStockKbar.getClosePrice().subtract(pre2StockKbar.getClosePrice()), pre2StockKbar.getClosePrice()));
                        exportDTO.setPre2CloseRate(PriceUtil.getPricePercentRate(pre2StockKbar.getClosePrice().subtract(pre3StockKbar.getClosePrice()), pre3StockKbar.getClosePrice()));
                        exportDTO.setDay3Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i - 11, i), 3));
                        exportDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i - 11, i), 5));
                        exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i - 11, i), 10));
                        exportDTO.setPremium(PriceUtil.getPricePercentRate(sellStockKbar.getClosePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice()));
                        resultList.add(exportDTO);
                    }
                }
            }

        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\多头炮.xls");

    }
}
