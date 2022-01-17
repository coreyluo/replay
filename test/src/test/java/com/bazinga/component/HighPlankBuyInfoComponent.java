package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FirstMinuteBuyDTO;
import com.bazinga.dto.HighPositionDTO;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class HighPlankBuyInfoComponent {
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
        List<HighPositionDTO> highPositionDTOS = judgePlankInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(HighPositionDTO dto:highPositionDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketAmount());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankTime());
            list.add(dto.isEndPlankFlag());
            list.add(dto.getHighPlanks());
            list.add(dto.getEndPlanks());
            list.add(dto.getYesterdayHighPlankFlag());
            list.add(dto.getYesterdayEndPlankFlag());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","流通市值","交易日期","买入时间","尾盘是否封住","前10日上板未封住次数","前10日上板且尾盘封住次数","昨日是否触碰板","昨日尾盘是否封住","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("10天5板数据",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("10天5板数据");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<HighPositionDTO> judgePlankInfo(){
        List<HighPositionDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("002694")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(12);
            StockKbar preKbar = null;
            Boolean yesterdayHighPlankFlag = null;
            Boolean yesterdayEndPlankFlag = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Boolean highPlank = null;
                Boolean endPlank = null;
                if(preKbar!=null) {
                    highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    if(highPlank && stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0){
                        HighPositionDTO buyDTO = new HighPositionDTO();
                        String plankTime = isPlank(stockKbar, preKbar.getClosePrice());
                        if(plankTime!=null){
                            Integer endPlanks = calEndPlanks(limitQueue);
                            Integer highPlanks = calHighPlanks(limitQueue);
                            if(endPlanks!=null && endPlanks>=5) {
                                buyDTO.setStockKbar(stockKbar);
                                buyDTO.setStockCode(circulateInfo.getStockCode());
                                buyDTO.setStockName(circulateInfo.getStockName());
                                buyDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000), 2, BigDecimal.ROUND_HALF_UP));
                                buyDTO.setMarketAmount(new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getHighPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
                                buyDTO.setTradeDate(stockKbar.getKbarDate());
                                buyDTO.setPlankTime(plankTime);
                                buyDTO.setHighPlanks(highPlanks);
                                buyDTO.setEndPlanks(endPlanks);
                                buyDTO.setEndPlankFlag(endPlank);
                                buyDTO.setYesterdayHighPlankFlag(yesterdayHighPlankFlag);
                                buyDTO.setYesterdayEndPlankFlag(yesterdayEndPlankFlag);
                                calProfit(stockKbars, buyDTO);
                                list.add(buyDTO);
                            }
                        }
                    }

                }
                yesterdayEndPlankFlag  = endPlank;
                yesterdayHighPlankFlag = highPlank;
                preKbar = stockKbar;
            }
        }
        return list;
    }

    public String isPlank(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean isUpper = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeTime().equals("09:25")&&!upperPrice){
                isUpper = false;
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=1||!upperPrice){
                isUpper = false;
            }
            if(tradeType==1&&upperPrice){
                if(!isUpper){
                    return data.getTradeTime();
                }
            }
        }
        return null;
    }



    public Integer calEndPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<12){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int i = 1;
        int planks  = 0;
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(i>=2){
                    if(endUpper){
                        planks++;
                    }
                }
            }
            nextKbar = stockKbar;
            i++;
        }
        return planks;
    }

    public Integer calHighPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<12){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int i = 1;
        int planks  = 0;
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean highUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getHighPrice(), stockKbar.getClosePrice());
                if (!highUpper) {
                    highUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjHighPrice(), stockKbar.getAdjClosePrice());
                }
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(i>=2){
                    if(highUpper&&!endUpper){
                        planks++;
                    }
                }
            }
            nextKbar = stockKbar;
            i++;
        }
        return planks;
    }

    public void calProfit(List<StockKbar> stockKbars,HighPositionDTO buyDTO){
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



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<20){
                return null;
            }
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

    }


}
