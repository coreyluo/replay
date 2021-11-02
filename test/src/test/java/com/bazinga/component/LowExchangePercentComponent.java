package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FastRaiseBankerDTO;
import com.bazinga.dto.LowExchangeDTO;
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
public class LowExchangePercentComponent {
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
    public void lowExchangeAvg(){
        List<LowExchangeDTO> daDies = judgeDownInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(LowExchangeDTO dto:daDies){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketMoney());
            list.add(dto.getTradeDate());
            if(dto.isEndPlank()) {
                list.add(1);
            }else{
                list.add(0);
            }
            list.add(dto.getLwoExchangePercent());
            list.add(dto.getDownRate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","市值","交易日期","尾盘是否封住","量能比例","相对最高点跌幅","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("缩量模型",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("缩量模型");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<LowExchangeDTO> judgeDownInfo(){
        List<LowExchangeDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
           /* if(!circulateInfo.getStockCode().equals("002815")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 380);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            /*if(list.size()>20){
                break;
            }*/
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(61);
            StockKbar preKbar = null;
            int planks = 0;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(preKbar!=null) {
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    if(highPlank){
                        if(planks<=1) {
                            LowExchangeDTO lowExchangeDTO = judgeDownExchange(limitQueue);
                            if (lowExchangeDTO != null) {
                                lowExchangeDTO.setStockCode(circulateInfo.getStockCode());
                                lowExchangeDTO.setStockName(circulateInfo.getStockName());
                                lowExchangeDTO.setCirculateZ(circulateInfo.getCirculateZ());
                                lowExchangeDTO.setMarketMoney(new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getHighPrice()));
                                lowExchangeDTO.setStockKbar(stockKbar);
                                lowExchangeDTO.setTradeDate(stockKbar.getKbarDate());
                                lowExchangeDTO.setEndPlank(endPlank);
                                calProfit(stockKbars, lowExchangeDTO);
                                list.add(lowExchangeDTO);
                            }
                        }
                    }
                    if(endPlank){
                        planks++;
                    }else{
                        planks=0;
                    }
                }
                preKbar = stockKbar;
            }
        }
        return list;
    }

    public LowExchangeDTO judgeDownExchange(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<61){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        int i=0;
        StockKbar highKbar = null;
        StockKbar lowKbar = null;
        StockKbar lowKbar57 = null;
        Long nearExchangeTotal = 0l;
        int nearCount = 0;
        Long farExchangeTotal = 0l;
        int farCount = 0;
        boolean threeDayLow = false;
        for (StockKbar stockKbar:list){
            i++;
            if(i<=60){
                if(highKbar==null||stockKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())==1){
                    highKbar = stockKbar;
                }
                if(lowKbar==null||stockKbar.getAdjLowPrice().compareTo(lowKbar.getAdjLowPrice())<1){
                    lowKbar = stockKbar;
                }
                if(i<=30){
                    farCount++;
                    farExchangeTotal = farExchangeTotal+stockKbar.getTradeQuantity();
                }
                if(i>30&&i<=60){
                    nearCount++;
                    nearExchangeTotal = nearExchangeTotal+stockKbar.getTradeQuantity();
                }
            }
            if(i<=57){
                if(lowKbar57==null||stockKbar.getAdjLowPrice().compareTo(lowKbar57.getAdjLowPrice())<1){
                    lowKbar57 = stockKbar;
                }
            }

            if(i>57&&i<=60){
                if(stockKbar.getAdjLowPrice().compareTo(lowKbar57.getAdjLowPrice())<1){
                    threeDayLow = true;
                }
            }
        }
        if(threeDayLow&&nearCount>0&&farCount>0){
            LowExchangeDTO lowExchangeDTO = new LowExchangeDTO();
            BigDecimal downRate = PriceUtil.getPricePercentRate(highKbar.getAdjHighPrice().subtract(lowKbar.getAdjLowPrice()), highKbar.getAdjHighPrice());
            BigDecimal nearExchange = new BigDecimal(nearExchangeTotal).divide(new BigDecimal(nearCount), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal farExchange = new BigDecimal(farExchangeTotal).divide(new BigDecimal(farCount), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal divide = nearExchange.divide(farExchange, 2, BigDecimal.ROUND_HALF_UP);
            lowExchangeDTO.setDownRate(downRate);
            lowExchangeDTO.setLwoExchangePercent(divide);
            if(divide.compareTo(new BigDecimal("0.6"))==-1){
                return lowExchangeDTO;
            }
        }
        return null;

    }

    public void calProfit(List<StockKbar> stockKbars,LowExchangeDTO bestDTO){
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
        BigDecimal allExchange = BigDecimal.ZERO;
        BigDecimal eightExchange = null;
        boolean eightFlag = false;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(20);
        FastRaiseBankerDTO bestDTO = new FastRaiseBankerDTO();
        for (ThirdSecondTransactionDataDTO data:datas){
            Integer tradeType = data.getTradeType();
            BigDecimal tradePrice = data.getTradePrice();
            if(data.getTradeTime().equals("09:25")){
                BigDecimal openExchangeMoney = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100));
                bestDTO.setOpenExchange(openExchangeMoney);
            }
            BigDecimal oneMoney = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100));
            allExchange = allExchange.add(oneMoney);
            BigDecimal rate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(preEndPrice), preEndPrice);
            if(stockKbar.getStockCode().startsWith("3")&&rate.compareTo(new BigDecimal("15"))>0){
                eightFlag = true;
            }
            if((!stockKbar.getStockCode().startsWith("3"))&&rate.compareTo(new BigDecimal("8"))>0){
                eightFlag = true;
            }
            if(!eightFlag){
                if(eightExchange==null){
                    eightExchange = oneMoney;
                }else{
                    eightExchange = eightExchange.add(oneMoney);
                }
            }else{
                bestDTO.setEightExchangeMoney(eightExchange);
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
                        if(plankTimes==1){
                            bestDTO.setPlankTime(data.getTradeTime());
                            bestDTO.setPlankExchangeMoney(allExchange);
                        }
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
