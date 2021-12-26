package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.PlankFirstSealDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.PlankHighDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.PlankHighUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class PlankFirstSealReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){
        List<PlankFirstSealDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20201210");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            for (int i = 10; i < kbarList.size()-1; i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar sellStockKbar = kbarList.get(i+1);

                if(!StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> buyList = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                int isPlank = 0;
                String plankTime ="";
                int fromIndex = 0;
                for (int j = 0; j < buyList.size()-1; j++) {
                    if(j==0 && stockKbar.getHighPrice().compareTo(buyList.get(0).getTradePrice())==0){
                        buyList.get(0).setTradeType(1);
                    }
                    ThirdSecondTransactionDataDTO currentDto = buyList.get(j);
                    ThirdSecondTransactionDataDTO afterDto = buyList.get(j + 1);
                    if(afterDto.getTradeType()==1 && afterDto.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        if(currentDto.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || currentDto.getTradeType()==0){
                            isPlank++;
                            plankTime = afterDto.getTradeTime();
                            fromIndex = j+1;
                        }
                    }
                }

                if(isPlank==1){
                    log.info("符合回测条件stockCode{} stockName{}", stockKbar.getStockCode(), stockKbar.getKbarDate());

                    PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(kbarList.subList(i - 10, i + 1));

                    List<ThirdSecondTransactionDataDTO> tradeList = buyList.subList(fromIndex, buyList.size());
                    BigDecimal plankRTradeAmount = BigDecimal.ZERO;
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : tradeList) {
                        plankRTradeAmount = plankRTradeAmount.add(transactionDataDTO.getTradePrice().multiply(new BigDecimal(transactionDataDTO.getTradeQuantity().toString()))
                                .multiply(CommonConstant.DECIMAL_HUNDRED));
                    }

                    PlankFirstSealDTO exportDTO = new PlankFirstSealDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                    exportDTO.setBuyPrice(stockKbar.getHighPrice());
                    exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    exportDTO.setPlankTime(plankTime);
                    exportDTO.setPlankTradeAmount(plankRTradeAmount);
                    exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    exportDTO.setUnPlankHigh(plankHighDTO.getUnPlank());
                    BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    if(sellPrice!=null){
                        exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    }
                    resultList.add(exportDTO);



                }


            }


        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\首封不开板数据.xls");

    }

}
