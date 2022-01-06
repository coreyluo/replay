package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FirstMinuteBuyDTO;
import com.bazinga.dto.TwoToThreeDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
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
public class BlockHighProfitInfoComponent {
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
    public void badPlankInfo(){
        Map<String, TwoToThreeDTO> map = judgePlankInfo();
        List<TwoToThreeDTO> twoToThreeDTOS = twoToThreePlankRate(map);
        List<Object[]> datas = Lists.newArrayList();
        for(TwoToThreeDTO dto:twoToThreeDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getPreTwoPlanks());
            list.add(dto.getThreePlanks());
            list.add(dto.getRate());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","日期","前一天二板数量","当天三连板数量","二进三成功比例"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("18个点",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("二进三比例");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<TwoToThreeDTO> twoToThreePlankRate(Map<String, TwoToThreeDTO> map){
        List<TwoToThreeDTO> list = Lists.newArrayList();
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20200101",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        String preTradeDateStr = null;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if(preTradeDateStr!=null) {
                TwoToThreeDTO twoToThreeDTO = map.get(tradeDateStr);
                if(twoToThreeDTO!=null) {
                    TwoToThreeDTO preTwoToThreeDTO = map.get(preTradeDateStr);
                    if (preTwoToThreeDTO != null) {
                        twoToThreeDTO.setPreTwoPlanks(preTwoToThreeDTO.getTwoPlanks());
                    }
                    if (twoToThreeDTO != null && twoToThreeDTO.getPreTwoPlanks() != 0) {
                        BigDecimal rate = new BigDecimal(twoToThreeDTO.getThreePlanks()).divide(new BigDecimal(twoToThreeDTO.getPreTwoPlanks()), 2, BigDecimal.ROUND_HALF_UP);
                        twoToThreeDTO.setRate(rate);
                    }
                    if (twoToThreeDTO != null) {
                        list.add(twoToThreeDTO);
                    }
                }
            }
            preTradeDateStr =  tradeDateStr;
        }
        return list;
    }

    public Map<String, TwoToThreeDTO> judgePlankInfo(){
        Map<String, Map<String,BigDecimal>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preKbar!=null) {
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    if(!highPlank){
                        highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getAdjHighPrice(),preKbar.getAdjClosePrice());
                    }
                    if(highPlank){
                        boolean isPlank = isPlank(stockKbar, preKbar.getClosePrice());
                        if(isPlank){
                            Map<String, BigDecimal> stockMap = map.get(stockKbar.getKbarDate());
                            if(stockMap==null){

                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return null;
    }

    public boolean isPlank(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
                return true;
            }
        }
        return false;
    }

    public BigDecimal rangeCondition(LimitQueue<StockKbar> limitQueue,FirstMinuteBuyDTO buyDTO){
        if(limitQueue.size()<3){
            return null;
        }
        int count = 0;
        BigDecimal realRangeTotal = BigDecimal.ZERO;
        BigDecimal highRangeMax = null;
        BigDecimal downRangeMax = null;

        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar preKbar = null;
        int i  = 0;
        while (iterator.hasNext()){
            i++;
            StockKbar next = iterator.next();
            if(preKbar!=null && i<limitQueue.size()){
                BigDecimal adjTwoPrice = next.getAdjOpenPrice();
                BigDecimal adjThreePrice = next.getAdjOpenPrice();
                if(next.getAdjClosePrice().compareTo(next.getAdjOpenPrice())==1){
                    adjTwoPrice = next.getAdjClosePrice();
                }
                if(next.getAdjClosePrice().compareTo(next.getAdjOpenPrice())==-1){
                    adjThreePrice  = next.getAdjClosePrice();
                }
                BigDecimal highRange = PriceUtil.getPricePercentRate(next.getAdjHighPrice().subtract(adjTwoPrice), preKbar.getAdjClosePrice());
                BigDecimal downRange = PriceUtil.getPricePercentRate(adjThreePrice.subtract(next.getAdjLowPrice()), preKbar.getAdjClosePrice());
                BigDecimal realRange = PriceUtil.getPricePercentRate(adjTwoPrice.subtract(adjThreePrice), preKbar.getAdjClosePrice());

                if(!PriceUtil.isUpperPrice(next.getStockCode(),next.getAdjClosePrice(),preKbar.getAdjClosePrice())) {
                    count++;
                    realRangeTotal = realRangeTotal.add(realRange);
                }
                if(highRangeMax==null||highRange.compareTo(highRangeMax)==1){
                    highRangeMax = highRange;
                }
                if(downRangeMax==null||downRange.compareTo(downRangeMax)==1){
                    downRangeMax = downRange;
                }
            }
            preKbar = next;
        }
        if(count>0){
            BigDecimal realRangeAvg = realRangeTotal.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            buyDTO.setRealRangeAvg(realRangeAvg);
            buyDTO.setHighRange(highRangeMax);
            buyDTO.setLowRange(downRangeMax);

        }
        return null;
    }

    public String findHighTime(StockKbar stockKbar){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        BigDecimal highPrice = null;
        String highTime = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(highPrice==null||data.getTradePrice().compareTo(highPrice)==1){
                highPrice = data.getTradePrice();
                highTime = data.getTradeTime();
            }
        }
        Date highDate = DateUtil.parseDate(highTime, DateUtil.HH_MM);
        Date tenDate = DateUtil.parseDate("10:00", DateUtil.HH_MM);
        if(highDate.before(tenDate)){
            return highTime;
        }
        return null;
    }

    public Integer calPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<20){
            return 0;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int planks  = 0;
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!upperPrice) {
                    upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(!upperPrice){
                    return planks;
                }else{
                    planks++;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;

    }

    public void calProfit(List<StockKbar> stockKbars,FirstMinuteBuyDTO buyDTO){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyDTO.getStockKbar().getAdjHighPrice()), buyDTO.getStockKbar().getAdjHighPrice());
                buyDTO.setProfit(profit);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                flag = true;
            }
        }
    }



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            //stockKbars = stockKbars.subList(20, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            return result;
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
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11);
        List<Integer> integers = list.subList(5, list.size());
        System.out.println(integers);

    }


}
