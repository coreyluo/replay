package com.bazinga.component;


import com.bazinga.ReplayConstant;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.LowTrendReplayDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LowTrendReplayComponent {

    @Autowired
    private StockAverageLineService stockAverageLineService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    private Set<String> buySet = new HashSet<>();

    public void replay(String kbarDateFrom ,String kbarDateTo){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream()
                .filter(item-> ReplayConstant.ZZ_500_LIST.contains(item.getStockCode()))
                //.filter(item-> "603638".equals(item.getStockCode()))
                .collect(Collectors.toList());

        List<LowTrendReplayDTO> resultList = Lists.newArrayList();
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom(kbarDateFrom);
            query.setKbarDateTo(kbarDateTo);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item-> item.getTradeQuantity()>0).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<20){
                continue;
            }
            stock:for (int i = 20; i < stockKbarList.size(); i++) {
                for (int j = 1; j >=0; j--) {
                    StockKbar stockKbar = stockKbarList.get(i-j);
                    String uniqueKey5 = stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE  + 5;
                    String uniqueKey10 = stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE  + 10;
                    String uniqueKey20 = stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE  + 20;
                    StockAverageLine day20Line = stockAverageLineService.getByUniqueKey(uniqueKey20);
                    StockAverageLine day5Line = stockAverageLineService.getByUniqueKey(uniqueKey5);
                    StockAverageLine day10Line = stockAverageLineService.getByUniqueKey(uniqueKey10);
                    if(day20Line==null){
                        log.info("未找到均线数据 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                        continue stock;
                    }
                    if(stockKbar.getAdjHighPrice().compareTo(day5Line.getAveragePrice())> 0
                            ||stockKbar.getAdjHighPrice().compareTo(day10Line.getAveragePrice())>0
                            || stockKbar.getAdjHighPrice().compareTo(day20Line.getAveragePrice())>0){
                        continue stock;
                    }
                }
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                if(stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice())>=0 && preStockKbar.getClosePrice().compareTo(preStockKbar.getOpenPrice())>=0){
                    continue ;
                }

                buy:for (int j = 0; j < 20 && j + i +1< stockKbarList.size()-2; j++) {
                    StockKbar moonKbar = stockKbarList.get(j + i);
                   // double avgQuantity = stockKbarList.subList(j + i - 4, j + i + 1).stream().mapToLong(StockKbar::getTradeQuantity).average().getAsDouble();
                    StockKbar sunKbar = stockKbarList.get(j + i +1);
                    int buyIndex = i+j +1;
                    if(sunKbar.getClosePrice().compareTo(sunKbar.getOpenPrice())>0 && sunKbar.getTradeAmount().compareTo(moonKbar.getTradeAmount())>0){
                        if(buySet.contains(sunKbar.getStockCode()+ sunKbar.getKbarDate())){
                            break;
                        }
                        log.info("满足买入条件 stockCode{} stockName{}", sunKbar.getStockCode(), sunKbar.getKbarDate());
                        buySet.add(sunKbar.getStockCode() + sunKbar.getKbarDate());
                        BigDecimal avgPrice = getAvgTradeAmount(stockKbarList.subList(buyIndex-5,buyIndex));

                        LowTrendReplayDTO exportDTO = new LowTrendReplayDTO();
                        exportDTO.setBuyTradeAmount(sunKbar.getTradeAmount());
                        exportDTO.setPreBuyTradeAmount(moonKbar.getTradeAmount());

                        exportDTO.setBuyPrice(sunKbar.getClosePrice());
                        int betweenDay = 0;
                        for (int k = buyIndex; k <buyIndex+40 && k+3< stockKbarList.size() ; k++) {
                            StockKbar startKbar = stockKbarList.get(k);
                            StockKbar sellDay1Kbar = stockKbarList.get(k+1);
                            StockKbar sellDay2Kbar = stockKbarList.get(k+2);
                            BigDecimal startHighPrice = startKbar.getOpenPrice().compareTo(startKbar.getClosePrice())>0?startKbar.getOpenPrice():startKbar.getClosePrice();
                            BigDecimal lowRate = PriceUtil.getPricePercentRate(sellDay1Kbar.getLowPrice().subtract(startHighPrice), startKbar.getClosePrice());
                            if(lowRate.compareTo(new BigDecimal("-3"))<0){
                                BigDecimal startSellPrice = startKbar.getClosePrice().multiply(new BigDecimal("0.97")).setScale(2,BigDecimal.ROUND_HALF_UP);
                                BigDecimal sellPrice = startSellPrice.add(sellDay1Kbar.getClosePrice()).divide(new BigDecimal("2"),2,BigDecimal.ROUND_HALF_UP);
                                exportDTO.setSellKbarDate(sellDay1Kbar.getKbarDate());
                                exportDTO.setSellPrice(sellPrice);
                                exportDTO.setBetweenDay(betweenDay+1);
                                break ;
                            }
                            BigDecimal sell1HighPrice = sellDay1Kbar.getOpenPrice().compareTo(sellDay1Kbar.getClosePrice())>0?sellDay1Kbar.getOpenPrice():sellDay1Kbar.getClosePrice();
                            BigDecimal sell2HighPrice = sellDay2Kbar.getOpenPrice().compareTo(sellDay2Kbar.getClosePrice())>0?sellDay2Kbar.getOpenPrice():sellDay2Kbar.getClosePrice();
                            if(sell1HighPrice.compareTo(startHighPrice) <=0 && sell2HighPrice.compareTo(startHighPrice)<=0){
                                StockKbar sellKbar = stockKbarList.get(k+3);
                                BigDecimal sellPrice = sellKbar.getOpenPrice().add(sellKbar.getClosePrice()).divide(new BigDecimal("2"),2,BigDecimal.ROUND_HALF_UP);
                                exportDTO.setSellPrice(sellPrice);
                                exportDTO.setSellKbarDate(sellKbar.getKbarDate());
                                exportDTO.setBetweenDay(betweenDay+3);
                                break ;
                            }
                            betweenDay++;
                        }
                        UnderInfo underInfo = getUnderInfo(stockKbarList.subList(buyIndex - 20, buyIndex + 1));
                        if(underInfo ==null){
                            break ;
                        }
                        exportDTO.setUnderLineDays(underInfo.getUnderDays());
                        exportDTO.setBuyUnderLine(underInfo.getBuyUnder());
                        exportDTO.setLowRate(underInfo.getLowRate());
                        exportDTO.setStockCode(sunKbar.getStockCode());
                        exportDTO.setStockName(sunKbar.getStockName());
                        exportDTO.setBuyKbarDate(sunKbar.getKbarDate());
                        if(exportDTO.getSellPrice()!=null){
                            exportDTO.setPremium(PriceUtil.getPricePercentRate(exportDTO.getSellPrice().subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                        }
                        resultList.add(exportDTO);
                        break;
                    }



                }



            }

        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\500破趋势"+kbarDateFrom+"-"+kbarDateTo+".xls");







    }

    private BigDecimal getAvgTradeAmount(List<StockKbar> list) {

        //

        return null;
    }

    private UnderInfo getUnderInfo(List<StockKbar> list) {
        BigDecimal highPrice = list.get(0).getAdjHighPrice();

        int underDays = 0;
        int buyUnder = 0;
        for (int i = 0; i < list.size(); i++) {
            StockKbar stockKbar = list.get(i);


            String uniqueKey5= stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate() +SymbolConstants.UNDERLINE +5;
            String uniqueKey10= stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+SymbolConstants.UNDERLINE +10;
            String uniqueKey20= stockKbar.getStockCode() + SymbolConstants.UNDERLINE + stockKbar.getKbarDate()+SymbolConstants.UNDERLINE +20;
            StockAverageLine averageLine5 = stockAverageLineService.getByUniqueKey(uniqueKey5);
            StockAverageLine averageLine10 = stockAverageLineService.getByUniqueKey(uniqueKey10);
            StockAverageLine averageLine20 = stockAverageLineService.getByUniqueKey(uniqueKey20);
            if(averageLine20==null){
                return null;
            }
            if(stockKbar.getAdjHighPrice().compareTo(averageLine5.getAveragePrice())<0
                &&stockKbar.getAdjHighPrice().compareTo(averageLine10.getAveragePrice())<0
                &&stockKbar.getAdjHighPrice().compareTo(averageLine20.getAveragePrice())<0){
                if(i==list.size()-1){
                    buyUnder = 1;
                }else {
                    if(highPrice.compareTo(stockKbar.getAdjHighPrice())<0){
                        highPrice = stockKbar.getAdjHighPrice();
                    }
                    underDays++;
                }
            }


        }
        StockKbar buyStockKbar = list.get(list.size() - 1);
        BigDecimal lowRate = PriceUtil.getPricePercentRate(buyStockKbar.getAdjClosePrice().subtract(highPrice), highPrice);
        return new UnderInfo(underDays, buyUnder, lowRate);
    }

    @Data
    class UnderInfo{

        private Integer underDays;

        private Integer buyUnder;

        private BigDecimal lowRate;

        public UnderInfo(Integer underDays, Integer buyUnder, BigDecimal lowRate) {
            this.underDays = underDays;
            this.buyUnder = buyUnder;
            this.lowRate = lowRate;
        }
    }



}
