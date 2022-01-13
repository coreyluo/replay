package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.Plank3To4DTO;
import com.bazinga.dto.ZongZiExportDTO;
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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ZongziReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;

    public void replay(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<ZongZiExportDTO> resultList = Lists.newArrayList();


        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20201210");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item-> item.getTradeQuantity()!=0).collect(Collectors.toList());

            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<11){
                continue;
            }

            for (int i = 11; i < stockKbarList.size()-1; i++) {
                StockKbar buyStockKbar = stockKbarList.get(i);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                StockKbar firstPlankKbar = stockKbarList.get(i - 1);
                StockKbar preKbar = stockKbarList.get(i - 2);
                if(!StockKbarUtil.isUpperPrice(firstPlankKbar,preKbar)){
                    continue;
                }

                PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(stockKbarList.subList(i - 9, i));
                if(plankHighDTO.getPlankHigh()==1){
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(buyStockKbar.getStockCode(), buyStockKbar.getKbarDate());
                    if(CollectionUtils.isEmpty(list)){
                        continue;
                    }
                    BigDecimal openRate = PriceUtil.getPricePercentRate(list.get(0).getTradePrice().subtract(firstPlankKbar.getClosePrice()),firstPlankKbar.getClosePrice());
                    BigDecimal highPrice = BigDecimal.ZERO;
                    BigDecimal lowPrice = list.get(0).getTradePrice();
                    int highIndex = 0;
                    int lowIndex = 0;
                    int buyIndex = 0;
                    Long totalTradeQuantity = 0L;
                    Long plankKbarTradeQuantity = firstPlankKbar.getTradeQuantity();
                    BigDecimal totalTradeAmount = BigDecimal.ZERO;

                    int totalJump = 0;
                    int overJump = 0;
                    for (int j = 0; j < list.size(); j++) {
                        ThirdSecondTransactionDataDTO currentDTO = list.get(j);
                        if(highPrice.compareTo(currentDTO.getTradePrice())<0){
                            highPrice = currentDTO.getTradePrice();
                            highIndex = j;
                        }
                        if(lowPrice.compareTo(currentDTO.getTradePrice())>0){
                            lowPrice = currentDTO.getTradePrice();
                            lowIndex = j;
                        }
                        totalTradeAmount = totalTradeAmount.add(currentDTO.getTradePrice().multiply(new BigDecimal(currentDTO.getTradeQuantity().toString())));
                        totalTradeQuantity = totalTradeQuantity + currentDTO.getTradeQuantity();
                        BigDecimal avgPrice = totalTradeAmount.divide(new BigDecimal(totalTradeQuantity.toString()),2,BigDecimal.ROUND_HALF_UP);
                        if(currentDTO.getTradePrice().compareTo(avgPrice)>0){
                            overJump++;
                        }
                        if(totalTradeQuantity * 100 >= plankKbarTradeQuantity *50){
                            buyIndex = j;
                            break;
                        }
                    }

                    if(overJump * 100 < buyIndex * 50){
                        continue;
                    }

                    if(buyIndex >0 && buyIndex +4 <list.size()){
                        BigDecimal upperPrice = PriceUtil.calUpperPrice(firstPlankKbar.getStockCode(),firstPlankKbar.getClosePrice());
                        if(highPrice.compareTo(upperPrice)==0){
                            log.info("买之前触碰过涨停stockCode{} kbarDate{}",buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                            continue;
                        }

                        ThirdSecondTransactionDataDTO buyDTO = list.get(buyIndex);
                        boolean isBuy = true;
                       /* for (int j = buyIndex; j < buyIndex+4 && j< list.size()-1; j++) {
                            ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                            if(transactionDataDTO.getTradePrice().compareTo(buyDTO.getTradePrice())<0){
                                isBuy = false;
                                break;
                            }
                        }*/
                        if(isBuy){
                            log.info("满足买入条件stockCode{} kbarDate{}",buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                            BigDecimal avgPrice = historyTransactionDataComponent.calBigDecimalAveragePrice(list.subList(0, buyIndex+1));
                            ThirdSecondTransactionDataDTO realBuyDTO = list.get(buyIndex);

                            BigDecimal relativeRate = PriceUtil.getPricePercentRate(realBuyDTO.getTradePrice().subtract(avgPrice), firstPlankKbar.getClosePrice());
                            if(relativeRate.compareTo(new BigDecimal("-1.5"))<=0 || relativeRate.compareTo(new BigDecimal("1.5")) >= 0){
                                continue;
                            }

                            ZongZiExportDTO exportDTO = new ZongZiExportDTO();
                            BigDecimal highAvgPrice = historyTransactionDataComponent.calBigDecimalAveragePrice(list.subList(0, highIndex + 1));
                            BigDecimal lowAvgPrice = historyTransactionDataComponent.calBigDecimalAveragePrice(list.subList(0, lowIndex + 1));

                            BigDecimal highRelativeRate = PriceUtil.getPricePercentRate(highPrice.subtract(highAvgPrice), firstPlankKbar.getClosePrice());
                            BigDecimal lowRelativeRate = PriceUtil.getPricePercentRate(lowPrice.subtract(lowAvgPrice), firstPlankKbar.getClosePrice());

                            exportDTO.setTotalJump(buyIndex);
                            exportDTO.setOverJump(overJump);
                            exportDTO.setBuyTime(realBuyDTO.getTradeTime());
                            exportDTO.setStockCode(buyStockKbar.getStockCode());
                            exportDTO.setStockName(buyStockKbar.getStockName());
                            exportDTO.setBuykbarDate(buyStockKbar.getKbarDate());
                            exportDTO.setBuyPrice(realBuyDTO.getTradePrice());
                            exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                            exportDTO.setHighRelativeRate(highRelativeRate);
                            exportDTO.setLowRelativeRate(lowRelativeRate);
                            exportDTO.setOpenRate(openRate);
                            exportDTO.setBuyRate(PriceUtil.getPricePercentRate(exportDTO.getBuyPrice().subtract(firstPlankKbar.getClosePrice()),firstPlankKbar.getClosePrice()));
                            exportDTO.setPreDayTradeAmount(firstPlankKbar.getTradeAmount());
                            exportDTO.setOpenTradeAmount(list.get(0).getTradePrice().multiply(new BigDecimal(list.get(0).getTradeQuantity())).multiply(CommonConstant.DECIMAL_HUNDRED));

                            exportDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),5));
                            exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),10));
                            if(i-31>=0){
                                exportDTO.setDay30Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-31,i),30));
                            }

                            BigDecimal total10Amount = stockKbarList.subList(i - 10, i).stream().map(StockKbar::getTradeAmount).reduce(BigDecimal::add).get();
                            exportDTO.setDay10AvgAmount(total10Amount.divide(new BigDecimal("10"),2,BigDecimal.ROUND_HALF_UP));
                            BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                            exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));

                            resultList.add(exportDTO);

                        }






                    }





                }


            }



        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\粽子低吸.xls");
    }

}
