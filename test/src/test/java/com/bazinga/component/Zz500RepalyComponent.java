package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.ReplayConstant;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.*;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.PlankHighDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.PlankHighUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Zz500RepalyComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private Index500Component index500Component;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private RedisMoniorService redisMoniorService;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    public void replay(String kbarDateFrom ,String kbarDateTo){
        Map<String, Index500NDayDTO> ndayRateMap = index500Component.getNdayRateMap(1200);
        log.info("获取500涨幅成功");
        Map<String, IndexRate500DTO> index500RateMap = index500Component.getIndex500RateMap();
        log.info("获取500分时map成功");
        Map<String, Integer> circulateAmountRankMap = commonReplayComponent.initAmountRankMap(kbarDateFrom, kbarDateTo);
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());

        /*circulateInfos = circulateInfos.stream()
                .filter(item-> ReplayConstant.HISTORY_ALL_500_LIST.contains(item.getStockCode()))
             //   .filter(item-> "000089".equals(item.getStockCode()))
                .collect(Collectors.toList());*/
        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateTo(DateUtil.parseDate(kbarDateFrom,DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.DESC);
        tradeQuery.setLimit(16);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);
        kbarDateFrom = DateUtil.format(tradeDatePools.get(0).getTradeDate(),DateUtil.yyyyMMdd);
        TradeDatePoolQuery tradeToQuery = new TradeDatePoolQuery();
        tradeToQuery.setTradeDateFrom(DateUtil.parseDate(kbarDateTo,DateUtil.yyyyMMdd));
        tradeToQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        tradeToQuery.setLimit(2);
        tradeDatePools = tradeDatePoolService.listByCondition(tradeToQuery);
        kbarDateTo = DateUtil.format(tradeDatePools.get(1).getTradeDate(),DateUtil.yyyyMMdd);
        Map<String, List<String>> nodeList = index500Component.getNodeList();
        // Map<String,RankDTO> rankMap = getStockDayRank(circulateInfos);

        List<Zz500ReplayDTO> resultList = Lists.newArrayList();

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom(kbarDateFrom);
            query.setKbarDateTo(kbarDateTo);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item-> item.getTradeQuantity()!=0).collect(Collectors.toList());

            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<16){
                continue;
            }

            for (int i = 16; i < stockKbarList.size()-1; i++) {
                StockKbar buyStockKbar = stockKbarList.get(i);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                StockKbar firstPlankKbar = stockKbarList.get(i - 1);
                StockKbar preKbar = stockKbarList.get(i - 2);

              /*  String recentNode = index500Component.getRecentNode(buyStockKbar.getKbarDate());
                List<String> history500List = nodeList.get(recentNode);
                if(!history500List.contains(buyStockKbar.getStockCode())){
                    log.info("此票不在历史500数据 stockCode{} kbarDate{}",buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                    continue;
                }*/
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(buyStockKbar.getStockCode(), buyStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list) || list.size()<3){
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
                    if(totalTradeAmount.compareTo(BigDecimal.ZERO)==0){
                        continue;
                    }
                    BigDecimal avgPrice = totalTradeAmount.divide(new BigDecimal(totalTradeQuantity.toString()),2,BigDecimal.ROUND_HALF_UP);
                    if(currentDTO.getTradePrice().compareTo(avgPrice)>0){
                        overJump++;
                    }
                    if(totalTradeQuantity * 100 >= plankKbarTradeQuantity *40){
                        buyIndex = j;
                        break;
                    }
                }

                if(overJump * 100 < buyIndex * 70){
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
                   /* for (int j = buyIndex; j < buyIndex+3 && j< list.size()-1; j++) {
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
                     /*   if(relativeRate.compareTo(new BigDecimal("-1.5"))<=0 || relativeRate.compareTo(new BigDecimal("1.5")) >= 0){
                            continue;
                        }*/
                        BigDecimal avgHighLowRate = getAvgHighLowRate(stockKbarList.subList(i-11,i));
                        BigDecimal moonRate = getMoonRate(stockKbarList.subList(i-11,i));
                        BigDecimal day10HighPrice = stockKbarList.subList(i - 13, i - 3).stream().map(StockKbar::getHighPrice).max(BigDecimal::compareTo).get();

                        Zz500ReplayDTO exportDTO = new Zz500ReplayDTO();
                        BigDecimal highAvgPrice = historyTransactionDataComponent.calBigDecimalAveragePrice(list.subList(0, highIndex + 1));
                        BigDecimal lowAvgPrice = historyTransactionDataComponent.calBigDecimalAveragePrice(list.subList(0, lowIndex + 1));
                        if(highAvgPrice == null || lowAvgPrice ==null ){
                            continue;
                        }

                        BigDecimal highRelativeRate = PriceUtil.getPricePercentRate(highPrice.subtract(highAvgPrice), firstPlankKbar.getClosePrice());
                        BigDecimal lowRelativeRate = PriceUtil.getPricePercentRate(lowPrice.subtract(lowAvgPrice), firstPlankKbar.getClosePrice());
                        BigDecimal highRate = PriceUtil.getPricePercentRate(highPrice.subtract(firstPlankKbar.getClosePrice()), firstPlankKbar.getClosePrice());
                        BigDecimal lowRate = PriceUtil.getPricePercentRate(lowPrice.subtract(firstPlankKbar.getClosePrice()), firstPlankKbar.getClosePrice());
                        BigDecimal avgRate = PriceUtil.getPricePercentRate(avgPrice.subtract(firstPlankKbar.getClosePrice()), firstPlankKbar.getClosePrice());

                        exportDTO.setBuyRelativeAvgRate(relativeRate);
                        exportDTO.setTotalJump(buyIndex);
                        exportDTO.setOverJump(overJump);
                        exportDTO.setBuyTime(realBuyDTO.getTradeTime());
                        exportDTO.setStockCode(buyStockKbar.getStockCode());
                        exportDTO.setStockName(buyStockKbar.getStockName());
                        exportDTO.setBuykbarDate(buyStockKbar.getKbarDate());
                        exportDTO.setBuyPrice(realBuyDTO.getTradePrice());
                        exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                     /*   RankDTO rankDTO = rankMap.get(buyStockKbar.getStockCode() + SymbolConstants.UNDERLINE + buyStockKbar.getKbarDate());
                        if(rankDTO!=null){
                            exportDTO.setRank(rankDTO.getRank());
                            exportDTO.setOpenTradeAmount(rankDTO.getTradeAmount());
                        }*/

                        IndexRate500DTO indexRate500DTO = index500RateMap.get(buyStockKbar.getKbarDate() + realBuyDTO.getTradeTime());
                        IndexRate500DTO preIndexRate500DTO = index500RateMap.get(firstPlankKbar.getKbarDate() + realBuyDTO.getTradeTime());
                        IndexRate500DTO index0935DTO = index500RateMap.get(buyStockKbar.getKbarDate() + "09:34");
                        if(indexRate500DTO ==null){
                            log.info("dto为空 tradeTime{}",realBuyDTO.getTradeTime());
                            continue;
                        }
                        Index500NDayDTO index500NDayDTO = ndayRateMap.get(firstPlankKbar.getKbarDate());
                        if(index500NDayDTO!=null){
                            exportDTO.setPreDay500Amount(index500NDayDTO.getTradeAmount());
                            exportDTO.setDay5Rate500(ndayRateMap.get(buyStockKbar.getKbarDate()).getDay5Rate());
                            exportDTO.setDay10Rate500(ndayRateMap.get(buyStockKbar.getKbarDate()).getDay10Rate());
                            exportDTO.setDay15Rate500(ndayRateMap.get(buyStockKbar.getKbarDate()).getDay15Rate());
                        }
                        exportDTO.setOverOpenCount10(indexRate500DTO.getOverOpenCount10());
                        exportDTO.setOverOpenCount20(indexRate500DTO.getOverOpenCount20());
                        exportDTO.setMin5TradeAmount(indexRate500DTO.getMin5TradeAmount());
                        exportDTO.setPreMin5TradeAmount(preIndexRate500DTO.getMin5TradeAmount());
                        exportDTO.setMin10TradeAmount(indexRate500DTO.getMin10TradeAmount());
                        exportDTO.setPreMin10TradeAmount(preIndexRate500DTO.getMin10TradeAmount());
                        exportDTO.setOpenRate500(indexRate500DTO.getOpenRate());
                        exportDTO.setHighRate500(indexRate500DTO.getHighRate());
                        exportDTO.setLowRate500(indexRate500DTO.getLowRate());
                        exportDTO.setBuyRate500(indexRate500DTO.getBuyRate());
                        exportDTO.setHighRateMin5(index0935DTO.getHighRate());
                        exportDTO.setLowRateMin5(index0935DTO.getLowRate());
                        exportDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-16,i),5));
                        exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-16,i),10));
                        exportDTO.setDay15Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-16,i),15));

                        Integer rank = circulateAmountRankMap.get(preKbar.getKbarDate() + SymbolConstants.UNDERLINE + buyStockKbar.getStockCode());
                        if(rank!=null){
                            exportDTO.setAmountRank(rank);
                        }

                        exportDTO.setOverOpenCountMin5(indexRate500DTO.getOverOpenCount());

                        exportDTO.setDay10HighPrice(day10HighPrice);
                        exportDTO.setMoonRate(moonRate);
                        exportDTO.setAvgHighLowRate(avgHighLowRate);
                        exportDTO.setOpenRate(openRate);
                        exportDTO.setBuyRate(PriceUtil.getPricePercentRate(exportDTO.getBuyPrice().subtract(firstPlankKbar.getClosePrice()),firstPlankKbar.getClosePrice()));
                        exportDTO.setPreDayTradeAmount(firstPlankKbar.getTradeAmount());
                        exportDTO.setHighRelativeRate(highRelativeRate);
                        exportDTO.setLowRelativeRate(lowRelativeRate);
                        exportDTO.setHighRate(highRate);
                        exportDTO.setLowRate(lowRate);
                        exportDTO.setAvgRate(avgRate);

                        BigDecimal total10Amount = stockKbarList.subList(i - 10, i).stream().map(StockKbar::getTradeAmount).reduce(BigDecimal::add).get();
                        BigDecimal sellPrice = historyTransactionDataComponent.calMorningAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                        exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));

                        resultList.add(exportDTO);

                    }






                }







            }



        }
        log.info("输出文件500低吸");
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\500低吸40"+kbarDateFrom+"-"+kbarDateTo+".xls");

    }

    private Map<String, RankDTO> getStockDayRank(List<CirculateInfo> circulateInfos) {
        Map<String,RankDTO> resultMap = new HashMap<>();
        String key = "zz500_open_amount_rank";
        RedisMonior redisMonior = redisMoniorService.getByRedisKey(key);
        if(redisMonior !=null){
            JSONObject jsonObject = JSONObject.parseObject(redisMonior.getRedisValue());
            jsonObject.forEach((jsonKey,value)->{
                resultMap.put(jsonKey,JSONObject.parseObject(value.toString(),RankDTO.class));
            });
        }else {
            TradeDatePoolQuery query = new TradeDatePoolQuery();
            query.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
            query.setTradeDateTo(DateUtil.parseDate("20220228",DateUtil.yyyyMMdd));
            List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
            for (TradeDatePool tradeDatePool : tradeDatePools) {
                Map<String,BigDecimal> tempMap = new HashMap<>();
                for (CirculateInfo circulateInfo : circulateInfos) {
                    Date tradeDate = tradeDatePool.getTradeDate();
                    String kbarDate = DateUtil.format(tradeDate,DateUtil.yyyyMMdd);
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), tradeDate);
                    if(CollectionUtils.isEmpty(list)|| list.size()<3) {
                        continue;
                    }
                    ThirdSecondTransactionDataDTO open = list.get(0);
                    tempMap.put(circulateInfo.getStockCode() + SymbolConstants.UNDERLINE + kbarDate,open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity().toString())));
                }
                Map<String, BigDecimal> sortedMap = SortUtil.sortByValue(tempMap);

                int rank= 1;
                for (Map.Entry<String, BigDecimal> entry : sortedMap.entrySet()) {
                    resultMap.put(entry.getKey(),new RankDTO(rank,entry.getValue()));
                    rank++;
                }
            }
            RedisMonior monior = new RedisMonior();
            monior.setRedisKey(key);
            monior.setRedisValue(JSONObject.toJSONString(resultMap));
            redisMoniorService.save(monior);
        }
        return resultMap;
    }

    private BigDecimal getMoonRate(List<StockKbar> list) {

        for (int i = list.size()-1; i>0; i--) {
            StockKbar stockKbar = list.get(i);
            StockKbar preStockKbar = list.get(i-1);
            BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(stockKbar.getOpenPrice()),preStockKbar.getClosePrice());
            if(closeRate.compareTo(new BigDecimal("0"))<0){
                return closeRate;
            }

        }
        return null;


    }

    private BigDecimal getAvgHighLowRate(List<StockKbar> list) {
        BigDecimal maxRate = BigDecimal.ZERO;
        BigDecimal minRate = new BigDecimal("40");
        BigDecimal sumRate = BigDecimal.ZERO;
        for (int i = 1; i < list.size(); i++) {
            StockKbar stockKbar = list.get(i);
            StockKbar preStockKbar = list.get(i-1);
            BigDecimal currentRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(stockKbar.getLowPrice()), preStockKbar.getClosePrice());
            if(currentRate.compareTo(maxRate) >0){
                maxRate = currentRate;
            }
            if(currentRate.compareTo(minRate)<0){
                minRate = currentRate;
            }
            sumRate = sumRate.add(currentRate);
        }

        BigDecimal totalRate = sumRate.subtract(minRate).subtract(maxRate);
        return  totalRate.divide(new BigDecimal("10"),2,BigDecimal.ROUND_HALF_UP);


    }

}
