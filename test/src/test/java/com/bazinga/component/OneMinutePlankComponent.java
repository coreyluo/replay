package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.FastRaiseBankerDTO;
import com.bazinga.dto.FirstMinuteBuyDTO;
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
public class OneMinutePlankComponent {
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
    public void firstMinutePlankInfo(){
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
            list.add(dto.getGatherQuantity());
            list.add(dto.getGatherPercent());
            list.add(dto.getGatherAmount());
            list.add(dto.getBeforeBuyQuantity());
            list.add(dto.getBeforeBuyPercent());
            list.add(dto.getBeforeBuyAmount());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","开盘涨幅","尾盘是否封住","上板时间","连板高度(10为反包板)","集合成交量","集合成交换手","集合成交额","上板前成交量","上板前成交换手","上板前成交额","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("前一分钟买的票",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("前一分钟买的票");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<FirstMinuteBuyDTO> judgePlankInfo(){
        List<FirstMinuteBuyDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
           /* if(!circulateInfo.getStockCode().equals("300612")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 380);
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
                            calProfit(stockKbars,buyDTO);
                            if(planks!=10) {
                                buyDTO.setPlanks(planks + 1);
                            }else{
                                buyDTO.setPlanks(10);
                            }
                            buyDTO.setOpenRate(openRate);
                            if(!DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))) {
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
        boolean isUpper = false;
        Integer gatherCount = null;
        BigDecimal gatherAmount = null;
        Integer beforeBuyCount = null;
        BigDecimal beforeBuyTradeAmount = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            BigDecimal tradeAmount = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100));
            if(data.getTradeTime().equals("09:25")){
                gatherCount = data.getTradeQuantity();
                gatherAmount = tradeAmount;
                beforeBuyCount = data.getTradeQuantity();
                beforeBuyTradeAmount = tradeAmount;
            }
            if(data.getTradeTime().equals("09:25")&&upperPrice){
                isUpper = true;
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            if(!data.getTradeTime().equals("09:30")){
                return null;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=1||!upperPrice){
                isUpper = false;
            }
            if(!isUpper&&(tradeType==1&&upperPrice)){
                buyDTO.setGatherAmount(gatherAmount);
                buyDTO.setGatherQuantity(gatherCount);
                buyDTO.setBeforeBuyAmount(beforeBuyTradeAmount);
                buyDTO.setBeforeBuyQuantity(beforeBuyCount);
                if(gatherCount!=null){
                    BigDecimal divide = new BigDecimal(gatherCount ).multiply(new BigDecimal(10000)).divide(new BigDecimal(circulateInfo.getCirculateZ()), 2, BigDecimal.ROUND_HALF_UP);
                    buyDTO.setGatherPercent(divide);
                }
                if(beforeBuyCount!=null){
                    BigDecimal divide = new BigDecimal(beforeBuyCount).multiply(new BigDecimal(10000)).divide(new BigDecimal(circulateInfo.getCirculateZ()), 2, BigDecimal.ROUND_HALF_UP);
                    buyDTO.setBeforeBuyPercent(divide);
                }
                return data.getTradeTime();
            }
            if(beforeBuyCount==null) {
                beforeBuyCount = data.getTradeQuantity();
            }else{
                beforeBuyCount = beforeBuyCount+data.getTradeQuantity();
            }
            if(beforeBuyTradeAmount==null) {
                beforeBuyTradeAmount = tradeAmount;
            }else{
                beforeBuyTradeAmount = beforeBuyTradeAmount.add(tradeAmount);
            }
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