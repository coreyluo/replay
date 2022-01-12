package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BigOrderBuyDTO;
import com.bazinga.dto.DongBeiStockDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.BlockInfoQuery;
import com.bazinga.replay.query.BlockStockDetailQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.handler.ClientMessageHandlerTube;
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
public class BigBuyComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private BlockInfoService blockInfoService;
    @Autowired
    private BlockStockDetailService blockStockDetailService;
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
    public void dongBeiInfo(){
        List<BigOrderBuyDTO> dtos = highProfitBlock();
        List<Object[]> datas = Lists.newArrayList();
        for(BigOrderBuyDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getTradeTime());
            list.add(dto.getBuyPrice());
            list.add(dto.getBuyRate());
            list.add(dto.getBuyAmount());
            list.add(dto.getBuyTimeAvgPrice());
            list.add(dto.getAvgTradeAmountDay5());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","买入日期","买入时间","买入价格","买入涨幅","买入时候成交额","买入时候均价","买入前5日成交额","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("18个点",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("大单跟随");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<BigOrderBuyDTO>  highProfitBlock(){
        List<BigOrderBuyDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        int i = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            if(i>=500){
                break;
            }
            System.out.println(i);
            List<BigOrderBuyDTO> buys = judgePlankInfo(circulateInfo);
            list.addAll(buys);
            /*if(list.size()>0){
                break;
            }*/
        }
        return list;
    }

    public List<BigOrderBuyDTO>  judgePlankInfo(CirculateInfo circulateInfo){
        List<BigOrderBuyDTO> list = Lists.newArrayList();
        System.out.println(circulateInfo.getStockCode());
        List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
        if(CollectionUtils.isEmpty(stockKbars)){
            return list;
        }
        StockKbar preKbar = null;
        int i = 0;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(5);
        LimitQueue<StockKbar> limitQueuePlanks = new LimitQueue<>(10);
        for (StockKbar stockKbar:stockKbars){
            BigDecimal beforeDay5Avg = beforeAvgAmount(limitQueue);
            limitQueue.offer(stockKbar);
            limitQueuePlanks.offer(stockKbar);
            List<BigOrderBuyDTO> buys = Lists.newArrayList();
            if(preKbar!=null) {
                boolean isUpper = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                if(isUpper) {
                    List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), stockKbar.getKbarDate());
                    buys = nextDayTransactionDatas(datas, circulateInfo, preKbar, stockKbar);
                }
            }
            preKbar = stockKbar;
            i++;
            if(i==stockKbars.size()-1){
                break;
            }
            if(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20211130",DateUtil.yyyyMMdd))||
                    DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).after(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd))){
                continue;
            }
            if(!CollectionUtils.isEmpty(buys)) {
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(circulateInfo.getStockCode(), stockKbars.get(i).getKbarDate());
                Integer planks = planks(limitQueuePlanks);
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                for (BigOrderBuyDTO buy : buys) {
                    avgPrice = chuQuanAvgPrice(avgPrice, stockKbars.get(i));
                    BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(buy.getBuyPrice(), stockKbars.get(i));
                    BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
                    buy.setProfit(profit);
                    buy.setAvgTradeAmountDay5(beforeDay5Avg);
                    buy.setPlanks(planks);
                    buy.setOpenRate(openRate);
                    list.add(buy);
                }
            }
        }
        return list;
    }

    public Integer planks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<=2){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar stockKbar = iterator.next();
            list.add(stockKbar);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int planks = 0;
        int i = 0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i>=2){
                boolean isUpper = PriceUtil.isUpperPrice(stockKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if(isUpper){
                    planks++;
                }else{
                    return planks;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;
    }

    public BigDecimal beforeAvgAmount(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()==0){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            count++;
            total  = total.add(next.getTradeAmount());
        }
        BigDecimal divide = total.divide(new BigDecimal(4800 * count), 2, BigDecimal.ROUND_HALF_UP);
        return divide;
    }
    public List<BigOrderBuyDTO> nextDayTransactionDatas(List<ThirdSecondTransactionDataDTO> datas,CirculateInfo circulateInfo,StockKbar preKbar,StockKbar stockKbar){
        List<BigOrderBuyDTO> list = Lists.newArrayList();
        if(CollectionUtils.isEmpty(datas)){
            return list;
        }
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(6);
        BigDecimal total = BigDecimal.ZERO;
        int count= 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            total = total.add(data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity()*100)));
            count = count+data.getTradeQuantity();
            BigDecimal avgPrice = total.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            limitQueue.offer(data);
            BigOrderBuyDTO buyDTO = new BigOrderBuyDTO();
            boolean flag = judgeBigOrder(limitQueue,buyDTO);
            if(flag) {
                BigDecimal chuQuanPrice = chuQuanAvgPrice(data.getTradePrice(), stockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanPrice.subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                buyDTO.setStockCode(circulateInfo.getStockCode());
                buyDTO.setStockName(circulateInfo.getStockName());
                buyDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000),2,BigDecimal.ROUND_HALF_UP));
                buyDTO.setTradeDate(stockKbar.getKbarDate());
                buyDTO.setTradeTime(data.getTradeTime());
                buyDTO.setBuyPrice(data.getTradePrice());
                buyDTO.setBuyRate(rate);
                buyDTO.setBuyTimeAvgPrice(avgPrice);
                list.add(buyDTO);
            }
        }
        return list;
    }
    public boolean judgeBigOrder(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigOrderBuyDTO buyDTO){
        if(limitQueue.size()<6){
            return false;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        int i = 0;
        BigDecimal totalTradeAmount = BigDecimal.ZERO;
        BigDecimal prePrice = null;
        while(iterator.hasNext()){
            i++;
            ThirdSecondTransactionDataDTO data = iterator.next();
            BigDecimal tradeAmount = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            if(i<=5){
                totalTradeAmount = totalTradeAmount.add(tradeAmount);
            }
            if(i==6){
                if(totalTradeAmount.compareTo(new BigDecimal(150000))!=1){
                    return false;
                }
                if(tradeAmount.compareTo(new BigDecimal(500000))!=1){
                    return false;
                }
                if(tradeAmount.compareTo(totalTradeAmount.multiply(new BigDecimal(3)))!=1){
                    return false;
                }
                if(data.getTradePrice().subtract(prePrice).compareTo(new BigDecimal("0.03")) !=1){
                    return false;
                }
                buyDTO.setBuyAmount(tradeAmount);
                buyDTO.setChangePrice(data.getTradePrice().subtract(prePrice));
                buyDTO.setDirect(data.getTradeType());
            }
            prePrice = data.getTradePrice();
        }
        return true;
    }

    public BigDecimal calAvgPrice(List<ThirdSecondTransactionDataDTO> datas) {
        try{
            if(CollectionUtils.isEmpty(datas)){
                return null;
            }
            Float price = historyTransactionDataComponent.calAveragePrice(datas);
            BigDecimal avgPrice = new BigDecimal(price).setScale(2,BigDecimal.ROUND_HALF_UP);
            return avgPrice;
        } catch (Exception e) {
            return null;
        }
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

    public BigDecimal calProfit(List<StockKbar> stockKbars,StockKbar kbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(kbar.getAdjHighPrice()), kbar.getAdjHighPrice());
                return profit;
            }
            if(stockKbar.getKbarDate().equals(kbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
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
