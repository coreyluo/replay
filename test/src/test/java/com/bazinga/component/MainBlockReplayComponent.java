package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.BlockHeadExportDTO;
import com.bazinga.dto.MainBlockExportDTO;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockInfoService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MainBlockReplayComponent {


    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void invokeStrategy(){
        List<MainBlockExportDTO> resultList = Lists.newArrayList();


        ThsBlockStockDetailQuery thsDetailQuery = new ThsBlockStockDetailQuery();
        thsDetailQuery.setBlockCode("DADC");
        List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(thsDetailQuery);
        thsBlockStockDetails = thsBlockStockDetails.stream().filter(item-> !item.getStockCode().startsWith("688")).collect(Collectors.toList());

        for (ThsBlockStockDetail thsBlockStockDetail : thsBlockStockDetails) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(thsBlockStockDetail.getStockCode());
            query.setKbarDateFrom("20201220");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            for (int i = 8; i < kbarList.size()-2; i++) {
                StockKbar sellKbar = kbarList.get(i+1);
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                List<StockKbar> plankHighList = kbarList.subList(i - 8, i + 1);
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(plankHighList);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(thsBlockStockDetail.getStockCode(), stockKbar.getKbarDate());
                list = historyTransactionDataComponent.getPreOneHourData(list);
                for (int j = 2; j < list.size(); j++) {
                    ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                    ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j-1);
                    if(transactionDataDTO.getTradeType()==1 && stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) ==0 ){
                        if(preTransactionDataDTO.getTradeType()!=1 || stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) <0){
                            log.info("判断为上板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                            MainBlockExportDTO exportDTO = new MainBlockExportDTO();
                            exportDTO.setStockCode(stockKbar.getStockCode());
                            exportDTO.setStockName(stockKbar.getStockName());
                            exportDTO.setBlockName(thsBlockStockDetail.getBlockName());
                            exportDTO.setKbarDate(stockKbar.getKbarDate());
                            exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                            exportDTO.setUnPlank(plankHighDTO.getUnPlank());
                            exportDTO.setPlankTime(transactionDataDTO.getTradeTime());
                            Integer sealType = stockKbar.getClosePrice().compareTo(stockKbar.getHighPrice())==0 ?1 :0;
                            exportDTO.setSealType(sealType);
                            BigDecimal sellAvgPrice = historyTransactionDataComponent.calAvgPrice(sellKbar.getStockCode(), sellKbar.getKbarDate());
                            if(sellKbar.getAdjFactor().compareTo(stockKbar.getAdjFactor()) ==0){
                                exportDTO.setPremium(PriceUtil.getPricePercentRate(sellAvgPrice.subtract(stockKbar.getHighPrice()),stockKbar.getHighPrice()));
                            }else {
                                log.info("复权因子发生变更stockCode{} kbarDate{}",sellKbar.getStockCode(),sellKbar.getKbarDate());
                                sellAvgPrice = sellAvgPrice.multiply(sellKbar.getAdjFactor().divide(stockKbar.getAdjFactor(),9,BigDecimal.ROUND_HALF_UP));
                                exportDTO.setPremium(PriceUtil.getPricePercentRate(sellAvgPrice.subtract(stockKbar.getHighPrice()),stockKbar.getHighPrice()));
                            }
                            resultList.add(exportDTO);
                            break;
                        }
                    }

                }

            }
        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\主流板块光伏打板.xls");

    }
}
