package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.FastRaiseBestDTO;
import com.bazinga.dto.RaiseAndDropBestDTO;
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
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class RaiseDropComponent {
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
    public void raiseDrop(){
        List<RaiseAndDropBestDTO> daDies = fastRaiseInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(RaiseAndDropBestDTO dto:daDies){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getStockKbar().getKbarDate());
            list.add(dto.getPlankTime());
            list.add(dto.getPlanks());
            list.add(dto.isEndPlankFlag());
            list.add(dto.getLeftRate());
            list.add(dto.getRightRate());
            list.add(dto.getTotalRate());
            list.add(dto.getTradeAmount());
            list.add(dto.getBeforeDay3());
            list.add(dto.getBeforeDay5());
            list.add(dto.getBeforeDay10());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","上板时间","连板数","尾盘是否封住","左边涨幅","右边涨幅","总涨幅","交易金额","3日涨幅","5日涨幅","10日涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("毛刺数据",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("毛刺数据");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<RaiseAndDropBestDTO> fastRaiseInfo(){
        List<RaiseAndDropBestDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("002248")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(8);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(preKbar!=null) {
                    boolean upperFlag = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    boolean isYiZhi = stockKbar.getHighPrice().equals(preKbar.getLowPrice());
                    if (upperFlag&&!isYiZhi) {
                        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                        String plankTime = isPlanked(datas, preKbar.getClosePrice());
                        if(plankTime!=null){
                            /*if(stockKbar.getKbarDate().equals("20210831")){
                                System.out.println(111111);
                            }*/
                            int planks = getPlanks(limitQueue);
                            if(planks!=0){
                                RaiseAndDropBestDTO bestDTO = getBeforeDay5(limitQueue, planks);
                                /*if(bestDTO==null) {
                                    System.out.println(JSONObject.toJSONString(bestDTO));
                                }*/
                                if(bestDTO!=null) {
                                    bestDTO.setStockCode(stockKbar.getStockCode());
                                    bestDTO.setStockName(stockKbar.getStockName());
                                    bestDTO.setStockKbar(stockKbar);
                                    bestDTO.setCirculateZ(circulateInfo.getCirculateZ());
                                    bestDTO.setTradeDate(stockKbar.getKbarDate());
                                    bestDTO.setPlanks(planks);
                                    bestDTO.setPlankTime(plankTime);
                                    bestDTO.setEndPlankFlag(endPlank);
                                    beforeRate(stockKbars, bestDTO);
                                    list.add(bestDTO);
                                }
                            }
                        }

                    }
                }
                preKbar = stockKbar;
            }
            /*if(list.size()>=5){
                return list;
            }*/
        }
        return list;
    }

    public void beforeRate(List<StockKbar> stockKbars,RaiseAndDropBestDTO bestDTO){
        List<StockKbar> list = Lists.newArrayList();
        list.addAll(stockKbars);
        Collections.reverse(list);
        StockKbar endKbar = null;
        boolean flag = false;
        int i = 0;
        StockKbar preKbar = null;
        for (StockKbar stockKbar:list){
            if(flag){
                i++;
            }
            if(i==1){
                endKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                bestDTO.setBeforeDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                bestDTO.setBeforeDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                bestDTO.setBeforeDay10(rate);
            }
            if(stockKbar.getKbarDate().equals(bestDTO.getStockKbar().getKbarDate())){
                flag = true;
                if(preKbar!=null) {
                    BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), preKbar.getKbarDate());
                    avgPrice = chuQuanAvgPrice(avgPrice, preKbar);
                    BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(stockKbar.getAdjHighPrice()), stockKbar.getAdjHighPrice());
                    bestDTO.setProfit(profit);
                }
            }
            preKbar = stockKbar;
        }


    }

    public RaiseAndDropBestDTO getBeforeDay5(LimitQueue<StockKbar> limitQueue,int planks){
        RaiseAndDropBestDTO bestDTO = null;
        Iterator<StockKbar> iterator = limitQueue.iterator();
        List<StockKbar> list = Lists.newArrayList();
        while (iterator.hasNext()){
            StockKbar kbar = iterator.next();
            list.add(kbar);
        }
        Collections.reverse(list);
        StockKbar nextKbar = null;
        int i = 0;
        for (StockKbar stockKbar:list){
            i++;
            if(nextKbar!=null){
                boolean highUpper = PriceUtil.isUpperPrice(nextKbar.getHighPrice(), stockKbar.getClosePrice());
                if(i-planks>=2&&i-planks<=6){
                    if(!highUpper){
                        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(nextKbar.getStockCode(), nextKbar.getKbarDate());
                        RaiseAndDropBestDTO raiseAndDropBestDTO = calRange(datas, stockKbar.getClosePrice());
                        if(raiseAndDropBestDTO!=null) {
                            raiseAndDropBestDTO.setTradeAmount(nextKbar.getTradeAmount());
                            if (bestDTO == null||raiseAndDropBestDTO.getTotalRate().compareTo(bestDTO.getTotalRate())==1){
                                bestDTO  = raiseAndDropBestDTO;
                            }
                        }
                    }
                }
            }
            nextKbar = stockKbar;
        }
        return bestDTO;
    }
    public RaiseAndDropBestDTO calRange(List<ThirdSecondTransactionDataDTO> datas,BigDecimal preEndPrice){
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        String highTime  = null;
        BigDecimal highPrice = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(highPrice==null||data.getTradePrice().compareTo(highPrice)==1){
                highPrice = data.getTradePrice();
                highTime = data.getTradeTime();
            }
        }
        Date highDate = DateUtil.parseDate(highTime, DateUtil.HH_MM);
        Date afterHigh = highDate;
        Date beforeHigh = highDate;
        for (int i=0;i<=4;i++){
            afterHigh = DateUtil.addMinutes(afterHigh,1);
            String afterHighStr = DateUtil.format(afterHigh, DateUtil.HH_MM);
            if(afterHighStr.equals("11:31")){
                afterHigh = DateUtil.parseDate("13:00", DateUtil.HH_MM);
            }
            if(afterHighStr.equals("09:26")){
                afterHigh = DateUtil.parseDate("09:30", DateUtil.HH_MM);
            }
            if(afterHighStr.equals("15:01")){
                afterHigh = DateUtil.parseDate("15:00", DateUtil.HH_MM);
                break;
            }

        }
        for (int i=0;i<=4;i++){
            beforeHigh = DateUtil.addMinutes(beforeHigh,-1);
            String beforeHighStr = DateUtil.format(beforeHigh, DateUtil.HH_MM);
            if(beforeHighStr.equals("12:59")){
                beforeHigh = DateUtil.parseDate("11:30", DateUtil.HH_MM);
            }
            if(beforeHighStr.equals("09:29")||beforeHighStr.equals("09:24")){
                beforeHigh = DateUtil.parseDate("09:25", DateUtil.HH_MM);
                break;
            }
        }
        String beforeHighTime = DateUtil.format(beforeHigh, DateUtil.HH_MM);
        String afterHighTime = DateUtil.format(afterHigh, DateUtil.HH_MM);
        BigDecimal left = null;
        BigDecimal right = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
            if(data.getTradeTime().equals(beforeHighTime)&&left==null){
                left  =  data.getTradePrice();
            }
            if(data.getTradeTime().equals(afterHighTime)){
                right  =  data.getTradePrice();
            }
            if (date.after(afterHigh)&&right==null){
                right = data.getTradePrice();
            }
            if (date.after(beforeHigh)&&left==null){
                left = data.getTradePrice();
            }
        }
        /*System.out.println(preEndPrice);
        if(right==null){
            System.out.println(11111111);
        }*/
        BigDecimal leftRate = PriceUtil.getPricePercentRate(highPrice.subtract(left), preEndPrice);
        BigDecimal rightRate = PriceUtil.getPricePercentRate(highPrice.subtract(right), preEndPrice);
        RaiseAndDropBestDTO best = new RaiseAndDropBestDTO();
        best.setLeftRate(leftRate);
        best.setRightRate(rightRate);
        best.setTotalRate(leftRate.add(rightRate));
        return  best;
    }

    public int getPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<8){
            return 0;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        List<StockKbar> list = Lists.newArrayList();
        while (iterator.hasNext()){
            StockKbar kbar = iterator.next();
            list.add(kbar);
        }
        Collections.reverse(list);
        int planks = 1;
        StockKbar nextKbar = null;
        int i = 0;
        for (StockKbar stockKbar:list){
            i++;
            if(nextKbar!=null) {
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (i == 3) {
                    if (endUpper) {
                       planks = 2;
                    }
                }
                if (i == 4) {
                    if (endUpper) {
                        return 0;
                    }
                }

            }
            nextKbar = stockKbar;
        }
        return planks;
    }

    public String isPlanked(List<ThirdSecondTransactionDataDTO> datas,BigDecimal preEndPrice){
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean isPlank = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            boolean upperPrice = PriceUtil.isUpperPrice(data.getTradePrice(), preEndPrice);
            if(data.getTradeTime().equals("09:25")){
                if(!upperPrice){
                    isPlank = false;
                }
                continue;
            }
            if(data.getTradeType()==null||(data.getTradeType()!=1&&data.getTradeType()!=0)){
                continue;
            }
            boolean isPlankData = false;
            if(upperPrice&&data.getTradeType()==1){
                isPlankData = true;
            }
            if(isPlankData){
                if(!isPlank){
                    return data.getTradeTime();
                }
            }else{
                isPlank = false;
            }
        }
        return null;
    }

    public int judgePlanks(LimitQueue<StockKbar> limitQueue, FastRaiseBestDTO bestDTO){
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

    public void calProfit(List<StockKbar> stockKbars,FastRaiseBestDTO bestDTO){
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


    public FastRaiseBestDTO isFastRaise(List<ThirdSecondTransactionDataDTO> datas,StockKbar stockKbar,BigDecimal preEndPrice){
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean haveRaise = false;
        boolean preIsPlank  = false;
        int plankTimes = 0;
        BigDecimal totalExchangeMoney = null;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(2);
        FastRaiseBestDTO bestDTO = new FastRaiseBestDTO();
        for (ThirdSecondTransactionDataDTO data:datas){
            Integer tradeType = data.getTradeType();
            BigDecimal tradePrice = data.getTradePrice();
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
                if (raiseRate != null) {
                    haveRaise = true;
                    bestDTO.setRaiseTime(data.getTradeTime());
                    bestDTO.setRaiseRate(raiseRate);
                    bestDTO.setRaiseMoney(exchangeMoney);
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
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        ThirdSecondTransactionDataDTO first = null;
        int i=0;
        while(iterator.hasNext()){
            i++;
            ThirdSecondTransactionDataDTO next = iterator.next();
            if(i==1){
                first = next;
            }
            if(first!=null) {
                BigDecimal rate = PriceUtil.getPricePercentRate(next.getTradePrice().subtract(first.getTradePrice()), yesterDayPrice);
                if(rate.compareTo(new BigDecimal("3"))==1){
                    return rate;
                }
            }
        }
        return null;
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
