package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FirstMinuteBuyDTO;
import com.bazinga.dto.PlankExchangeAmountDTO;
import com.bazinga.dto.TwoToThreeDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.KBarDTO;
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
public class PlankExchangeAmountComponent {
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
    public void plankExchangeAmountInfo(){
        List<PlankExchangeAmountDTO> buys = judgePlankInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(PlankExchangeAmountDTO dto:buys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculate());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankPrice());
            list.add(dto.getPlankTradeAmount());
            list.add(dto.getRateDay3());
            list.add(dto.getRateDay5());
            list.add(dto.getRateDay10());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }
        String[] rowNames = {"index","股票代码","股票名称","总股本","流通z","交易日期","板价","成交额","3日涨幅","5日涨幅","10日涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板上成交额",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板上成交额");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<PlankExchangeAmountDTO> judgePlankInfo(){
        List<PlankExchangeAmountDTO> buys = new ArrayList<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            if(circulateInfo.getStockCode().equals("600383")){
                continue;
            }
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(20);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20190101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(date.after(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(preKbar!=null) {
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    boolean isBeautifulPlank = stockKbar.getHighPrice().equals(stockKbar.getLowPrice());
                    if(endPlank &&!isBeautifulPlank){
                        Integer planks = calPlanks(limitQueue);
                        BigDecimal totalAmount = getTradeAmount(stockKbar, preKbar.getClosePrice());
                        PlankExchangeAmountDTO amountDTO = new PlankExchangeAmountDTO();
                        amountDTO.setStockCode(circulateInfo.getStockCode());
                        amountDTO.setStockName(circulateInfo.getStockName());
                        amountDTO.setCirculate(circulateInfo.getCirculate());
                        amountDTO.setCirculateZ(circulateInfo.getCirculateZ());
                        amountDTO.setTradeDate(stockKbar.getKbarDate());
                        amountDTO.setPlankPrice(stockKbar.getHighPrice());
                        amountDTO.setPlankTradeAmount(totalAmount);
                        amountDTO.setStockKbar(stockKbar);
                        calProfit(stockKbars,amountDTO);
                        calBeforeRateDay(stockKbars,amountDTO);
                        buys.add(amountDTO);
                    }

                }
                preKbar = stockKbar;
            }
        }
        return buys;
    }

    public BigDecimal getTradeAmount(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        BigDecimal totalAmount = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
                BigDecimal tradeAmount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2,BigDecimal.ROUND_HALF_UP);
                if(totalAmount==null){
                    totalAmount = tradeAmount;
                }else{
                    totalAmount = totalAmount.add(tradeAmount);
                }
            }
            if(upperPrice&&data.getTradeTime().equals("15:00")){
                BigDecimal tradeAmount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2,BigDecimal.ROUND_HALF_UP);
                if(totalAmount==null){
                    totalAmount = tradeAmount;
                }else{
                    totalAmount = totalAmount.add(tradeAmount);
                }
            }
        }
        return totalAmount;
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

    public void calProfit(List<StockKbar> stockKbars,PlankExchangeAmountDTO buyDTO){
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

    public void calBeforeRateDay(List<StockKbar> stockKbars,PlankExchangeAmountDTO buyDTO){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        StockKbar endStockKbar = null;
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endStockKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(i>12){
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
            stockKbars = stockKbars.subList(20, stockKbars.size());
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
