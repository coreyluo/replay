package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.BigExchangeTestBuyDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ShStockOrderQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.ShStockOrderService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
public class BigAmountTestComponent {
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
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ShStockOrderService shStockOrderService;
    public void plankExchangeAmountInfo(){
        List<BigExchangeTestBuyDTO> dtos = judgePlankInfo();
        List<Object[]> datas = Lists.newArrayList();

        for (BigExchangeTestBuyDTO dto : dtos) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getEndFlag());
            list.add(dto.getPlanks());
            list.add(dto.getBigOrderAmount());
            list.add(dto.getBigOrderTimes());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","尾盘是否封住（1封住 0未封住）","板高","大单金额","大单数量","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("大单信息",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("大单信息");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<BigExchangeTestBuyDTO> judgePlankInfo(){
        List<BigExchangeTestBuyDTO> list = new ArrayList<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        int m = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            m++;
            if(m>100){
                continue;
            }
            System.out.println(circulateInfo.getStockCode());
            /*if(!circulateInfo.getStockCode().equals("605319")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(20);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20220410", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(date.after(DateUtil.parseDate("20220401", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(preKbar!=null) {
                    BigExchangeTestBuyDTO buyDTO = new BigExchangeTestBuyDTO();
                    buyDTO.setStockCode(circulateInfo.getStockCode());
                    buyDTO.setStockName(circulateInfo.getStockName());
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    boolean endPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(),stockKbar.getKbarDate());
                    if(endPlank) {
                        buyDTO.setEndFlag(1);
                    }else{
                        buyDTO.setEndFlag(0);
                    }
                    buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    if(endPlank && stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                        String planks = calPlanks(limitQueue);
                       // String buyTime = firstBuyTime(stockKbar, preKbar.getClosePrice());
                        String buyTime = "09:35";
                        if (buyTime != null) {
                            if (!StringUtils.isBlank(buyTime)) {
                                buyDTO.setPlanks(planks);
                                calProfit(stockKbars, buyDTO, stockKbar);
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

    public void getStockOrder(BigExchangeTestBuyDTO buyDTO){
        if(buyDTO.getStockCode().startsWith("6")) {
            ShStockOrderQuery query = new ShStockOrderQuery();
            query.setDateTrade(DateUtil.parseDate(buyDTO.getStockCode(), DateUtil.yyyyMMdd));
            query.setThscode(buyDTO.getStockCode() + ".SH");
            List<ShStockOrder> shStockOrders = shStockOrderService.listByCondition(query);
        }
    }


    public String firstBuyTime(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean isPlank = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            if(data.getTradeTime().equals("09:25")){
                if(!upperPrice){
                    isPlank = false;
                }
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
                if(!isPlank){
                    return data.getTradeTime();
                }
                isPlank = true;
            }else{
                isPlank = false;
            }
        }
        return null;
    }




    public String calPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<3){
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
        int afterPlanks  = 1;
        int beforePlanks = 0;
        int  noPlankTime = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i<=1){
                continue;
            }
            if(nextKbar!=null) {
                boolean endPlank = PriceUtil.isHistoryUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice(),nextKbar.getKbarDate());
                if(!endPlank){
                    noPlankTime++;
                }else{
                    if(noPlankTime==0){
                        afterPlanks++;
                    }else if(noPlankTime == 1){
                        beforePlanks++;
                    }
                }
            }
            if(noPlankTime>=2){
                break;
            }
            nextKbar = stockKbar;
        }
        if(beforePlanks==0){
            return afterPlanks+"连板";
        }else{
            return beforePlanks+"+"+afterPlanks+"板";
        }
    }

    public void calProfit(List<StockKbar> stockKbars,BigExchangeTestBuyDTO buyDTO,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjHighPrice()), buyStockKbar.getAdjHighPrice());
                buyDTO.setProfit(profit);
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public void calBeforeRateDay(List<StockKbar> stockKbars,BigExchangeTestBuyDTO buyDTO,StockKbar buyStockKbar){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        StockKbar endStockKbar = null;
        boolean flag = false;
        int i=0;
        StockKbar highStockKbar = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endStockKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                //buyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                //buyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                //buyDTO.setRateDay10(rate);
            }
            if(i>0&&i<=30){
                if(highStockKbar==null){
                    highStockKbar = stockKbar;
                }else{
                    if(stockKbar.getTradeAmount().compareTo(highStockKbar.getTradeAmount())==1){
                        highStockKbar = stockKbar;
                    }
                }
            }
            if(i>30){
                break;
            }
            if(stockKbar.getKbarDate().equals(buyStockKbar.getKbarDate())){
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
           // stockKbars = stockKbars.subList(20, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<StockKbar> best = deleteNewStockTimes(stockKbars, 2000);
            return best;
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

    public List<StockKbar> deleteNewStockTimes(List<StockKbar> list,int size){
        List<StockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        StockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }

}
