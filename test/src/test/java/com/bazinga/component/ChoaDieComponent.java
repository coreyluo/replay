package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.DaDieBestDTO;
import com.bazinga.dto.DaDieBuyDTO;
import com.bazinga.dto.ZhongZhengBestDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ChoaDieComponent {
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
    public void chaoDie(){
        List<String> keys = Lists.newArrayList();
        List<DaDieBestDTO> daDies = choaDieInfo();
        Map<String, List<Object>> map = new HashMap<>();
        for(DaDieBestDTO dto:daDies){
            for (DaDieBuyDTO buyDTO:dto.getBuys1()) {
                String key = buyDTO.getStockKbar().getKbarDate() + buyDTO.getStockKbar().getStockCode();
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getCirculateZ());
                list.add(dto.getHighDate());
                list.add(dto.getHighRate());
                list.add(dto.getContinuePlanks());
                list.add(dto.getBeautifulPlanks());
                list.add(buyDTO.getStockKbar().getKbarDate());
                list.add(dto.getMaxExchangeMoney());
                list.add(buyDTO.getNegLineExchangeMoney());
                list.add(buyDTO.getNegLineEndRate());
                list.add(buyDTO.getEndRate());
                list.add(buyDTO.getLowerRate());
                list.add(buyDTO.getBuyThanHighRate());
                list.add(buyDTO.getBuyThanHighDays());
                list.add(buyDTO.getPlanks());
                list.add(buyDTO.getBuyDayRelativeRate());
                list.add(buyDTO.getHighPrice());
                list.add(buyDTO.getLowPrice());
                list.add(buyDTO.getBuyPrice());
                list.add(buyDTO.getBuyDayExchangeMoney());
                list.add(buyDTO.getRedTime());
                list.add(buyDTO.getGreenTime());
                list.add(buyDTO.getProfit());
                map.put(key,list);
            }
            for (DaDieBuyDTO buyDTO:dto.getBuys2()) {
                String key = buyDTO.getStockKbar().getKbarDate() + buyDTO.getStockKbar().getStockCode();
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getCirculateZ());
                list.add(dto.getHighDate());
                list.add(dto.getHighRate());
                list.add(dto.getContinuePlanks());
                list.add(dto.getBeautifulPlanks());
                list.add(buyDTO.getStockKbar().getKbarDate());
                list.add(dto.getMaxExchangeMoney());
                list.add(buyDTO.getNegLineExchangeMoney());
                list.add(buyDTO.getNegLineEndRate());
                list.add(buyDTO.getEndRate());
                list.add(buyDTO.getLowerRate());
                list.add(buyDTO.getBuyThanHighRate());
                list.add(buyDTO.getBuyThanHighDays());
                list.add(buyDTO.getPlanks());
                list.add(buyDTO.getBuyDayRelativeRate());
                list.add(buyDTO.getHighPrice());
                list.add(buyDTO.getLowPrice());
                list.add(buyDTO.getBuyPrice());
                list.add(buyDTO.getBuyDayExchangeMoney());
                list.add(buyDTO.getRedTime());
                list.add(buyDTO.getGreenTime());
                list.add(buyDTO.getProfit());
                map.put(key,list);
            }
        }


        List<Object[]> datas = Lists.newArrayList();
        for(String key:map.keySet()){
            Object[] objects = map.get(key).toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","最高点日期","最高点涨幅","最高点前最高连板","最高点一字板数","买入日期","最大成交金额","阴线成交金额","阴线收盘涨幅","买入阳线收盘涨幅","买入阳线触发前最低点跌幅",
                "买入日相对最高点涨幅","买入日与最高点间隔日期","买入日与最高点之间成功涨停数量","买入日阳线涨幅","最高点价格","最低点价格","买入价格","买入日成交金额","收红次数","收绿次数","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("大涨回调低吸",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("大涨回调低吸");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<DaDieBestDTO> choaDieInfo(){
        List<DaDieBestDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("002598")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                boolean isHigh = isHigh(limitQueue);
                if(isHigh){
                    BigDecimal tradeAmount = stockKbar.getTradeAmount();
                    if(preKbar.getTradeAmount().compareTo(stockKbar.getTradeAmount())>0){
                        tradeAmount = preKbar.getTradeAmount();
                    }
                    DaDieBestDTO bestDTO = new DaDieBestDTO();
                    bestDTO.setStockCode(circulateInfo.getStockCode());
                    bestDTO.setStockName(circulateInfo.getStockName());
                    bestDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    bestDTO.setHighDate(stockKbar.getKbarDate());
                    bestDTO.setStockKbar(stockKbar);
                    bestDTO.setMaxExchangeMoney(tradeAmount);
                    plankInfo(limitQueue,bestDTO);
                    afterJudgeHigh(stockKbars,bestDTO);
                    if(bestDTO.getNegLineEndRate()!=null){
                        calProfit(stockKbars,bestDTO);
                        list.add(bestDTO);
                    }
                }
                preKbar = stockKbar;
            }
        }
        return list;
    }

    public void calProfit(List<StockKbar> stockKbars,DaDieBestDTO bestDTO){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        StockKbar nextKbar = null;
        for (StockKbar stockKbar:reverse) {
            if (bestDTO.getBuys1().size() > 0) {
                for (DaDieBuyDTO buyDTO : bestDTO.getBuys1()) {
                    if(buyDTO.getStockKbar().getKbarDate().equals(stockKbar.getKbarDate())&&nextKbar!=null){
                        BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(buyDTO.getStockKbar().getAdjClosePrice()), buyDTO.getStockKbar().getAdjClosePrice());
                        buyDTO.setProfit(rate);
                    }
                }
            }
            if (bestDTO.getBuys2().size() > 0) {
                for (DaDieBuyDTO buyDTO : bestDTO.getBuys2()) {
                    if(buyDTO.getStockKbar().getKbarDate().equals(stockKbar.getKbarDate())&&nextKbar!=null){
                        BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(buyDTO.getStockKbar().getAdjClosePrice()), buyDTO.getStockKbar().getAdjClosePrice());
                        buyDTO.setProfit(rate);
                    }
                }
            }
            nextKbar = stockKbar;
        }
    }

    public void afterJudgeHigh(List<StockKbar> stockKbars,DaDieBestDTO bestDTO){
        StockKbar preKbar = null;
        boolean lowFlag  = false;
        int lowTimes = 0;
        BigDecimal lowerPrice = null;
        int planks = 0;
        int redTime = 0;
        int greenTime = 0;
        BigDecimal lowestExchangeMoney  = null;

        boolean flag  = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
                if(lowerPrice==null||stockKbar.getAdjLowPrice().compareTo(lowerPrice)==-1){
                    lowerPrice = stockKbar.getAdjLowPrice();
                }
            }
            if(flag && !lowFlag){
                if(stockKbar.getAdjHighPrice().compareTo(bestDTO.getStockKbar().getAdjHighPrice())>=0){
                    return ;
                }
            }
            if(i==1){
                if(stockKbar.getTradeAmount().compareTo(bestDTO.getMaxExchangeMoney())>0){
                    bestDTO.setMaxExchangeMoney(stockKbar.getTradeAmount());
                }
            }
            if(i>1){
                if(lowFlag){
                    if(stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice())==1&&
                        stockKbar.getTradeAmount().compareTo(bestDTO.getNegLineExchangeMoney())==1){
                        boolean upperPrice = PriceUtil.isUpperPrice(bestDTO.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                        if(!upperPrice){
                            upperPrice = PriceUtil.isUpperPrice(bestDTO.getStockCode(),stockKbar.getAdjHighPrice(),preKbar.getAdjClosePrice());
                        }
                        if(!upperPrice){
                            BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                            DaDieBuyDTO buyDTO = new DaDieBuyDTO();
                            buyDTO.setStockKbar(stockKbar);
                            buyDTO.setEndRate(endRate);
                            BigDecimal lowPercent = PriceUtil.getPricePercentRate(lowerPrice.subtract(bestDTO.getStockKbar().getAdjHighPrice()), bestDTO.getStockKbar().getAdjHighPrice());
                            buyDTO.setLowerRate(lowPercent);
                            buyDTO.setNegLineEndRate(bestDTO.getNegLineEndRate());
                            buyDTO.setNegLineExchangeMoney(bestDTO.getNegLineExchangeMoney());
                            BigDecimal buyThanHighRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(bestDTO.getStockKbar().getAdjHighPrice()), bestDTO.getStockKbar().getAdjHighPrice());
                            buyDTO.setBuyThanHighRate(buyThanHighRate);
                            buyDTO.setBuyThanHighDays(i);
                            buyDTO.setPlanks(planks);
                            BigDecimal relativeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(stockKbar.getAdjOpenPrice()), preKbar.getAdjClosePrice());
                            buyDTO.setBuyDayRelativeRate(relativeRate);
                            buyDTO.setBuyPrice(stockKbar.getAdjClosePrice());
                            buyDTO.setLowPrice(lowerPrice);
                            buyDTO.setHighPrice(bestDTO.getStockKbar().getAdjHighPrice());
                            buyDTO.setBuyDayExchangeMoney(stockKbar.getTradeAmount());
                            buyDTO.setRedTime(redTime);
                            buyDTO.setGreenTime(greenTime);
                            if(lowTimes==0) {
                                if(bestDTO.getBuys1().size()<3) {
                                    bestDTO.getBuys1().add(buyDTO);
                                }else{
                                    return;
                                }
                            }
                            if(lowTimes==1) {
                                if(bestDTO.getBuys2().size()<3) {
                                    bestDTO.getBuys2().add(buyDTO);
                                }else{
                                    return;
                                }
                            }
                        }
                    }
                }

                //寻找阴线
                if(!lowFlag) {
                    if (stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice()) == -1 &&
                            stockKbar.getTradeAmount().divide(bestDTO.getMaxExchangeMoney(), 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.6")) == -1&&
                            (lowestExchangeMoney==null||stockKbar.getTradeAmount().compareTo(lowestExchangeMoney)==-1)) {
                        boolean suddenPrice = PriceUtil.isSuddenPrice(bestDTO.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                        if (!suddenPrice) {
                            suddenPrice = PriceUtil.isSuddenPrice(bestDTO.getStockCode(), stockKbar.getAdjClosePrice(), preKbar.getAdjClosePrice());
                        }
                        if (!suddenPrice) {
                            lowFlag = true;
                            if (bestDTO.getBuys1().size() > 0) {
                                lowTimes++;
                            }
                            if (lowTimes >= 3) {
                                return;
                            }
                            BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                            bestDTO.setNegLineEndRate(endRate);
                            bestDTO.setNegLineExchangeMoney(stockKbar.getTradeAmount());
                        }
                    }
                }else{
                    if (stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice()) == -1&&(lowestExchangeMoney==null||stockKbar.getTradeAmount().compareTo(lowestExchangeMoney)==-1)) {
                        boolean suddenPrice = PriceUtil.isSuddenPrice(bestDTO.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                        if (!suddenPrice) {
                            suddenPrice = PriceUtil.isSuddenPrice(bestDTO.getStockCode(), stockKbar.getAdjClosePrice(), preKbar.getAdjClosePrice());
                        }
                        if (!suddenPrice) {
                            lowFlag = true;
                            if (bestDTO.getBuys1().size() > 0) {
                                lowTimes++;
                            }
                            if (lowTimes >= 3) {
                                return;
                            }
                            BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                            bestDTO.setNegLineEndRate(endRate);
                            bestDTO.setNegLineExchangeMoney(stockKbar.getTradeAmount());
                        }
                    }
                }
            }
            if(i>=1){
                boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                if(upperPrice){
                    planks++;
                }
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                if(rate.compareTo(BigDecimal.ZERO)==1){
                    redTime++;
                }
                if(rate.compareTo(BigDecimal.ZERO)==-1){
                    greenTime++;
                }
                boolean suddenPrice = PriceUtil.isSuddenPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                BigDecimal relativeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(stockKbar.getAdjOpenPrice()), preKbar.getAdjClosePrice());
                if(i>=2){
                    if(!suddenPrice){
                        if(relativeRate.compareTo(BigDecimal.ZERO)==-1){
                            if(lowestExchangeMoney==null || stockKbar.getTradeAmount().compareTo(lowestExchangeMoney)==-1){
                                lowestExchangeMoney = stockKbar.getTradeAmount();
                            }
                        }
                    }
                }
            }
            if(stockKbar.getKbarDate().equals(bestDTO.getHighDate())){
                flag = true;
            }
            preKbar = stockKbar;
        }
    }

    public boolean isHigh(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<10){
            return false;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar lastKbar = null;
        StockKbar highKbar = null;
        StockKbar lowerKbar = null;
        int i = 0;
        while(iterator.hasNext()){
            i++;
            StockKbar kbar = iterator.next();
            if(lowerKbar==null||kbar.getAdjLowPrice().compareTo(lowerKbar.getAdjLowPrice())==-1){
                lowerKbar   = kbar;
            }
            if(i<=9) {
                if (highKbar == null || kbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice()) == 1) {
                    highKbar = kbar;
                }
            }
            lastKbar = kbar;
        }
        if(lastKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())!=1){
            return false;
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjHighPrice().subtract(lowerKbar.getAdjLowPrice()), lowerKbar.getAdjLowPrice());
        if(rate.compareTo(new BigDecimal("50"))>=0){
            return true;
        }
        return false;
    }
    public void plankInfo(LimitQueue<StockKbar> limitQueue,DaDieBestDTO daDieBestDTO){

        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar lastKbar = null;
        StockKbar lowerKbar = null;
        StockKbar preKbar = null;
        int currentPlanks = 0;
        int continuePlanks = 0;
        int beautifulPlanks =0;
        int i = 0;
        while(iterator.hasNext()){
            i++;
            StockKbar kbar = iterator.next();
            if(lowerKbar==null||kbar.getAdjLowPrice().compareTo(lowerKbar.getAdjLowPrice())==-1){
                lowerKbar   = kbar;
            }
            if(preKbar!=null){
                boolean upperPrice = PriceUtil.isUpperPrice(kbar.getStockCode(), kbar.getClosePrice(), preKbar.getClosePrice());
                if(!upperPrice){
                    upperPrice = PriceUtil.isUpperPrice(kbar.getStockCode(),kbar.getAdjClosePrice(),preKbar.getAdjClosePrice());
                }
                if(upperPrice && kbar.getLowPrice().compareTo(kbar.getClosePrice())==0){
                    beautifulPlanks++;
                }
                if(upperPrice){
                    currentPlanks++;
                }else {
                    currentPlanks = 0;
                }
                if(currentPlanks>continuePlanks){
                    continuePlanks = currentPlanks;
                }
            }
            lastKbar = kbar;
            preKbar = kbar;
        }

        BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjHighPrice().subtract(lowerKbar.getAdjLowPrice()), lowerKbar.getAdjLowPrice());
        daDieBestDTO.setHighRate(rate);
        daDieBestDTO.setContinuePlanks(continuePlanks);
        daDieBestDTO.setBeautifulPlanks(beautifulPlanks);
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
