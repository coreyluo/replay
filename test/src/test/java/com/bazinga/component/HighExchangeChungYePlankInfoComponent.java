package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FirstMinuteBuyDTO;
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
public class HighExchangeChungYePlankInfoComponent {
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
        List<FirstMinuteBuyDTO> firstMinuteBuyDTOS = judgePlankInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(FirstMinuteBuyDTO dto:firstMinuteBuyDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getOpenRate());
            list.add(dto.isEndPlank());
            list.add(dto.getPlankTime());
            list.add(dto.getPlanks());
            list.add(dto.getStockKbar().getHighPrice());
            list.add(dto.getMaxTradeMoney());
            list.add(dto.getMaxTradeQuantity());
            list.add(dto.getAvgTradeMoney());
            list.add(dto.getAvgTradeQuantity());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","开盘涨幅","尾盘是否封住","上板时间","连板高度(10为反包板)","涨停价","18个点前最大成交额","18个点前最大成交手数","18个点前平均成交金额","18个点前平均成交手数","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("18个点",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("18个点");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<FirstMinuteBuyDTO> judgePlankInfo(){
        List<FirstMinuteBuyDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("300809")){
                continue;
            }*/
            if(!circulateInfo.getStockCode().startsWith("30")){
                continue;
            }
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            /*if(list.size()>2){
                break;
            }*/
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(preKbar!=null) {
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    if(highPlank && stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0){
                        FirstMinuteBuyDTO buyDTO = new FirstMinuteBuyDTO();
                        String plankTime = isPlank(stockKbar, preKbar.getClosePrice(),buyDTO,circulateInfo);
                        if(plankTime!=null){
                            Integer planks = calPlanks(limitQueue);
                            buyDTO.setStockCode(stockKbar.getStockCode());
                            buyDTO.setStockName(circulateInfo.getStockName());
                            buyDTO.setPlankTime(plankTime);
                            buyDTO.setTradeDate(stockKbar.getKbarDate());
                            buyDTO.setEndPlank(endPlank);
                            buyDTO.setStockKbar(stockKbar);
                            buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                            calProfit(stockKbars, buyDTO);
                            if (planks != 10) {
                                buyDTO.setPlanks(planks + 1);
                            } else {
                                buyDTO.setPlanks(10);
                            }
                            buyDTO.setOpenRate(openRate);
                            if (!DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))) {
                                list.add(buyDTO);
                            }
                        }
                    }

                }
                preKbar = stockKbar;
            }
        }
        return list;
    }

    public String isPlank(StockKbar stockKbar,BigDecimal preEndPrice,FirstMinuteBuyDTO buyDTO,CirculateInfo circulateInfo){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        BigDecimal maxTradeMoney = null;
        Integer maxTradeQuantity = null;
        int totalCount = 0;
        BigDecimal totalTradeMoney = BigDecimal.ZERO;
        Integer totalTradeQuantity = 0;
        boolean calFlag = true;
        boolean isUpper = false;
        int i = 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            i++;
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            BigDecimal rate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(preEndPrice), preEndPrice);
            if(rate.compareTo(new BigDecimal(18))==1){
                calFlag = false;
            }
            if(i>2&&calFlag){
                BigDecimal tradeMoney = tradePrice.multiply(new BigDecimal(data.getTradeQuantity()).multiply(new BigDecimal(100)));
                Integer tradeQuantity = data.getTradeQuantity();
                if(maxTradeMoney==null||tradeMoney.compareTo(maxTradeMoney)==1){
                    maxTradeMoney = tradeMoney;
                }
                if(maxTradeQuantity==null||tradeQuantity.compareTo(maxTradeQuantity)==1){
                    maxTradeQuantity = tradeQuantity;
                }
                totalTradeMoney  = totalTradeMoney.add(tradeMoney);
                totalTradeQuantity = totalTradeQuantity+tradeQuantity;
                totalCount = totalCount+1;
            }
            if(data.getTradeTime().equals("09:25")&&upperPrice){
                isUpper = true;
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=1||!upperPrice){
                isUpper = false;
            }
            if(!isUpper&&(tradeType==1&&upperPrice)){
                buyDTO.setMaxTradeMoney(maxTradeMoney);
                buyDTO.setMaxTradeQuantity(maxTradeQuantity);
                if(totalCount>0){
                    BigDecimal avgTradeMoney = totalTradeMoney.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
                    int avgTradeQuantity = new BigDecimal(totalTradeQuantity).divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP).intValue();
                    buyDTO.setAvgTradeMoney(avgTradeMoney);
                    buyDTO.setAvgTradeQuantity(avgTradeQuantity);
                }
                return data.getTradeTime();
            }
        }
        return null;
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
        if(limitQueue==null||limitQueue.size()<3){
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
        int i = 1;
        int planks  = 0;
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!upperPrice) {
                    upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if (i == 3) {
                    if(upperPrice){
                        planks++;
                    }else{
                        planks = 0;
                    }
                }
                if(i == 4){
                    if(upperPrice){
                        if(planks==0){
                            planks = 10;
                            return planks;
                        }else{
                            planks++;
                        }
                    }else{
                        return planks;
                    }
                }
                if(i>4){
                    if(upperPrice){
                        planks++;
                    }else{
                        return planks;
                    }
                }
            }
            nextKbar = stockKbar;
            i++;
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
