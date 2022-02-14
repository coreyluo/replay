package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.ZongZiExportDTO;
import com.bazinga.dto.ZuangExportDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZuangReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockKbarService stockKbarService;


    public void replay(){

        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("000607", "20210823");
        List<ZuangExportDTO> resultList = Lists.newArrayList();

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item-> !item.getStockCode().startsWith("3")).collect(Collectors.toList());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_Date", Sort.SortType.ASC);
            query.setKbarDateFrom("20201220");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbarList)|| stockKbarList.size()<5){
                continue;
            }
            stockKbarList = stockKbarList.stream().filter(item-> item.getTradeQuantity()!=0).collect(Collectors.toList());

            for (int i = 1; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice());
                BigDecimal circulateAmountZ =  stockKbar.getHighPrice().multiply(new BigDecimal(circulateInfo.getCirculateZ()));
              /*  if(circulateAmountZ.compareTo(new BigDecimal("5000000000"))>0){
                    continue;
                }*/

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

                if(highRate.compareTo(new BigDecimal("8")) < 0){
                    continue;
                }
                if(openRate.compareTo(new BigDecimal("3"))>0){
                    continue;
                }

                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list) || list.size()<3){
                    continue;
                }

                ThirdSecondTransactionDataDTO open = list.get(0);
                BigDecimal openTradeAmount =  open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity())).multiply(CommonConstant.DECIMAL_HUNDRED);
                if(openTradeAmount.compareTo(new BigDecimal("1000000"))>0){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> min2List = historyTransactionDataComponent.getFixTimeData(list, "09:32");
                boolean isOver8 = false;
                boolean isPlank = false;
                BigDecimal upperPrice = PriceUtil.calUpperPrice(preStockKbar.getStockCode(),preStockKbar.getClosePrice());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : min2List) {
                    if(!isPlank&& transactionDataDTO.getTradePrice().compareTo(upperPrice) == 0 && transactionDataDTO.getTradeType() ==1 ){
                        isPlank = true;
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                    if(rate.compareTo(new BigDecimal(8))>0){
                        isOver8 = true;
                        break;
                    }
                }
                if(isOver8){
                    String plankTime = "";
                    for (int j = min2List.size(); j < list.size()-1; j++) {
                        ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j);
                        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j+1);
                        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType() ==1){
                            if(preTransactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || preTransactionDataDTO.getTradeType()!=1){
                                log.info("出现涨停S stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                                plankTime = transactionDataDTO.getTradeTime();
                                break;
                            }
                        }
                    }
                    if(StringUtils.isEmpty(plankTime)){
                        continue;
                    }
                    log.info("满足条件 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    ZuangExportDTO exportDTO = new ZuangExportDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                    exportDTO.setBuyTime(plankTime);
                    exportDTO.setCirculateAmountZ(circulateAmountZ);
                    exportDTO.setOpenTradeAmount(openTradeAmount);
                    exportDTO.setBuyPrice(stockKbar.getHighPrice());
                    exportDTO.setOpenRate(openRate);
                    BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(avgPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    resultList.add(exportDTO);
                }
            }

        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\庄股分析.xls");
    }


}
