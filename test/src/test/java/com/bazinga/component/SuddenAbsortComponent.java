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

import java.math.BigDecimal;
import java.util.List;
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
        List<SuddenAbsortDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> !item.getStockCode().startsWith("3")).collect(Collectors.toList());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query= new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210101");
            query.setStockCode("000503");
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
                if(!PriceUtil.isSuddenPrice(stockKbar.getStockCode(),stockKbar.getClosePrice(),preStockKbar.getClosePrice())){
                    continue;
                }
                if(PriceUtil.isSuddenPrice(buyStockKbar.getStockCode(),buyStockKbar.getClosePrice(),stockKbar.getClosePrice())&&
                    buyStockKbar.getHighPrice().compareTo(buyStockKbar.getClosePrice())==0){
                    continue;
                }

                PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(subList);
                if(plankHighDTO.getPlankHigh()<3){
                    continue;
                }
                log.info("满足买入条件stockCode{} stockName{}", buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());

                SuddenAbsortDTO exportDTO = new SuddenAbsortDTO();
                exportDTO.setStockCode(buyStockKbar.getStockCode());
                exportDTO.setStockName(buyStockKbar.getStockName());
                exportDTO.setBuykbarDate(buyStockKbar.getKbarDate());
                exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(rateList,10));

                BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                exportDTO.setBuyPrice(buyStockKbar.getOpenPrice());
                exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                exportDTO.setLastSuddenTradeAmount(stockKbar.getTradeAmount());
                resultList.add(exportDTO);
            }



        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\跌停次日低吸.xls");
    }

}
