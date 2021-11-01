package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FastRaiseBankerDTO;
import com.bazinga.dto.FastRaiseBestDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class FastRateBankComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private BlockLevelReplayComponent blockLevelReplayComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    public void fastRaiseBanker(){
        List<FastRaiseBankerDTO> daDies = fastRaiseInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(FastRaiseBankerDTO dto:daDies){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketMoney());
            list.add(dto.getTradeDate());
            list.add(dto.getExchangeMoney());
            list.add(dto.getRaiseRate());
            list.add(dto.getRaiseTime());
            list.add(dto.isEndPlank());
            list.add(dto.getPlanks());
            list.add(dto.isPlankBack());
            list.add(dto.getRaiseMoney());
            list.add(dto.getOpenRate());
            list.add(dto.getBuyPrice());
            list.add(dto.getOpenExchange());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","市值","交易日期","异常前成交额","大涨幅度","大涨时间","尾盘是否板","连板高","是否回封","跳变成交金额","开盘涨幅","买入价格","开盘成交额","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("庄逼",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("庄逼");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<FastRaiseBankerDTO> fastRaiseInfo(){
        List<FastRaiseBankerDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("002868")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            /*if(list.size()>20){
                break;
            }*/
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(11);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(preKbar!=null) {
                    boolean upperFlag = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    if (upperFlag) {
                        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                        if (!CollectionUtils.isEmpty(datas)) {
                            FastRaiseBankerDTO bestDTO = isFastRaise(datas, stockKbar, preKbar.getClosePrice());
                            if(bestDTO!=null&&bestDTO.getRaiseRate()!=null) {
                                bestDTO.setStockCode(circulateInfo.getStockCode());
                                bestDTO.setStockName(circulateInfo.getStockName());
                                bestDTO.setCirculateZ(circulateInfo.getCirculateZ());
                                bestDTO.setMarketMoney(new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getHighPrice()));
                                bestDTO.setStockKbar(stockKbar);
                                bestDTO.setTradeDate(stockKbar.getKbarDate());
                                bestDTO.setEndPlank(endPlank);
                                bestDTO.setOpenRate(openRate);
                                bestDTO.setBuyPrice(stockKbar.getHighPrice());
                                calProfit(stockKbars, bestDTO);
                                if(limitQueue.size()>=5) {
                                    judgePlanks(limitQueue,bestDTO);
                                    list.add(bestDTO);
                                }
                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return list;
    }

    public int judgePlanks(LimitQueue<StockKbar> limitQueue, FastRaiseBankerDTO bestDTO){
        if(limitQueue==null||limitQueue.size()<5){
            return 0;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        StockKbar nextKbar = null;
        List<StockKbar> reverse = Lists.reverse(list);
        int i=0;
        int planks = 1;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i>=3){
                boolean upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if(!upperPrice){
                    upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(),nextKbar.getAdjClosePrice(),stockKbar.getAdjClosePrice());
                }
                if(upperPrice){
                    planks++;
                }else{
                    break;
                }
            }
            nextKbar = stockKbar;
        }
        bestDTO.setPlanks(planks);
        return planks;
    }

    public void calProfit(List<StockKbar> stockKbars,FastRaiseBankerDTO bestDTO){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(bestDTO.getStockKbar().getAdjHighPrice()), bestDTO.getStockKbar().getAdjHighPrice());
                bestDTO.setProfit(profit);
            }
            if(stockKbar.getKbarDate().equals(bestDTO.getStockKbar().getKbarDate())){
                flag = true;
            }
        }
    }


    public FastRaiseBankerDTO isFastRaise(List<ThirdSecondTransactionDataDTO> datas,StockKbar stockKbar,BigDecimal preEndPrice){
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean haveRaise = false;
        boolean preIsPlank  = false;
        int plankTimes = 0;
        BigDecimal totalExchangeMoney = null;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(20);
        FastRaiseBankerDTO bestDTO = new FastRaiseBankerDTO();
        for (ThirdSecondTransactionDataDTO data:datas){
            Integer tradeType = data.getTradeType();
            BigDecimal tradePrice = data.getTradePrice();
            if(data.getTradeTime().equals("09:25")){
                BigDecimal openExchangeMoney = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100));
                bestDTO.setOpenExchange(openExchangeMoney);
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            limitQueue.offer(data);
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(!haveRaise) {
                if (tradeType != 0 && upperPrice) {
                    return null;
                }
            }else{
                if(tradeType!=0&&upperPrice){
                    if(!preIsPlank) {
                        preIsPlank = true;
                        plankTimes = plankTimes + 1;
                    }
                }else {
                    preIsPlank=false;
                }
            }
            if(!haveRaise) {
                BigDecimal exchangeMoney = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100));
                if(totalExchangeMoney==null){
                    totalExchangeMoney = exchangeMoney;
                }else {
                    totalExchangeMoney = totalExchangeMoney.add(exchangeMoney);
                }
                BigDecimal raiseRate = raiseRate(limitQueue, preEndPrice);
                BigDecimal raiseMoney = raiseMoney(limitQueue);
                if (raiseRate != null) {
                    haveRaise = true;
                    bestDTO.setRaiseTime(data.getTradeTime());
                    bestDTO.setRaiseRate(raiseRate);
                    bestDTO.setRaiseMoney(raiseMoney);
                    bestDTO.setExchangeMoney(totalExchangeMoney);
                }
            }
            if(plankTimes>=2){
                bestDTO.setPlankBack(true);
            }
        }
        return bestDTO;
    }
    public BigDecimal raiseRate(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal yesterDayPrice){
        if(limitQueue==null||limitQueue.size()<2){
            return null;
        }
        BigDecimal lowPrice = null;
        BigDecimal lastPrice = null;
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        ThirdSecondTransactionDataDTO first = null;
        while(iterator.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator.next();
            if(lowPrice==null||next.getTradePrice().compareTo(lowPrice)==-1){
                lowPrice = next.getTradePrice();
            }
            lastPrice = next.getTradePrice();
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(lastPrice.subtract(lowPrice), yesterDayPrice);
        if(rate.compareTo(new BigDecimal("5"))==1){
            return rate;
        }
        return null;
    }

    public BigDecimal raiseMoney(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue){
        if(limitQueue==null||limitQueue.size()<2){
            return null;
        }
        BigDecimal lowPrice = null;
        BigDecimal raiseMoney = BigDecimal.ZERO;
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        ThirdSecondTransactionDataDTO first = null;
        while(iterator.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator.next();
            BigDecimal exchangeMoney = next.getTradePrice().multiply(new BigDecimal(next.getTradeQuantity() * 100));
            raiseMoney = raiseMoney.add(exchangeMoney);
        }
        return raiseMoney;
    }


    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            if(reverse.size()<=30){
                return null;
            }
            List<StockKbar> bars = reverse.subList(30, reverse.size());
            return bars;
        }catch (Exception e){
            return null;
        }
    }

    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,StockKbar kbar){
        BigDecimal reason = null;
        if(!(kbar.getClosePrice().equals(kbar.getAdjClosePrice()))&&!(kbar.getOpenPrice().equals(kbar.getAdjOpenPrice()))){
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }
    }

    public static void main(String[] args) {

    }


}
