package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.BoxBuyDTO;
import com.bazinga.dto.BoxStockBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BoxOneStockComponent {
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
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private StockAverageLineService stockAverageLineService;

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void oneStockBox(){
        List<BoxStockBuyDTO> dailys = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(BoxStockBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTotalCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getBuyTime());
            list.add(dto.getBuyPrice());
            list.add(dto.getOpenRate());
            list.add(dto.getBuyTimeTradeAmount());
            list.add(dto.getFirstHighRate());
            list.add(dto.getFirstHighTime());
            list.add(dto.getPreTradeAmount());
            list.add(dto.getPrePlanks());
            list.add(dto.getBeforeHighLowRate());
            list.add(dto.getAfterHighLowRate());
            list.add(dto.getRateDay3());
            list.add(dto.getRateDay5());
            list.add(dto.getRateDay10());
            list.add(dto.getBetweenTime());
            list.add(dto.getProfit());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","总股本","tradeDate","买入时间","买入价格","开盘涨幅","买入时候成交额","第一次高点涨幅","第一次高点时间","前一日成交额","前一日几连板","买入前低点涨幅","中间低点幅度","3日涨幅","5日涨幅","10日涨幅","买入相对第一次高点时间","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("个股箱体",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("个股箱体");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<BoxStockBuyDTO> getStockUpperShowInfo(){
        List<BoxStockBuyDTO> results = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        int count = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            System.out.println(circulateInfo.getStockCode()+count);
            /*if(!circulateInfo.getStockCode().equals("000665")){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))){
                    continue;
                }
                if(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).after(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd))){
                    continue;
                }
                /*if(stockKbar.getKbarDate().equals("20220322")){
                    System.out.println(1111);
                }*/

                if(preStockKbar!=null) {
                    BoxStockBuyDTO buyDTO = calBoxBuy(stockKbar,circulateInfo, preStockKbar);
                    if(buyDTO!=null){
                        calProfit(stockKbars,stockKbar,buyDTO);
                        calBeforeRate(stockKbars,stockKbar,buyDTO);
                        results.add(buyDTO);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return results;
    }

    public void calBeforeRate(List<StockKbar> stockKbars,StockKbar buyKbar,BoxStockBuyDTO buyDTO){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i = 0;
        StockKbar lastKbar = null;
        StockKbar nextKbar = null;
        int planks = 0;
        boolean continueFlag = true;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i>=2){
                boolean upperFlag = PriceUtil.isHistoryUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice(), nextKbar.getKbarDate());
                if (upperFlag&&continueFlag){
                    planks++;
                }else{
                    buyDTO.setPrePlanks(planks);
                    continueFlag = false;
                }
            }
            if(i==1){
                lastKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            nextKbar = stockKbar;
        }
    }

    public void calProfit(List<StockKbar> stockKbars,StockKbar buyKbar,BoxStockBuyDTO buyDTO){
        boolean flag = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(!CollectionUtils.isEmpty(datas)){
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    Integer count = 0;
                    for (ThirdSecondTransactionDataDTO data:datas){
                        count = count+data.getTradeQuantity();
                        totalAmount = totalAmount.add(data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity())));
                        if(data.getTradeTime().startsWith("13")){
                            break;
                        }
                    }
                    if(count>0){
                        BigDecimal avgPrice = totalAmount.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                        BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                        BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(buyDTO.getBuyPrice(), buyKbar);
                        BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
                        buyDTO.setProfit(profit);
                        return;
                    }
                }

            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
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

    public BoxStockBuyDTO calBoxBuy(StockKbar stockKbar,CirculateInfo circulateInfo,StockKbar preStockKbar){
        BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
        if(highRate.compareTo(new BigDecimal(3))<=0){
            return null;
        }
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        BoxStockBuyDTO buyDTO = null;
        int index = 0;
        int firstHighIndex = 0;
        int buyIndex = 0;
        String firstHighTime = null;
        BigDecimal firstHighPrice = null;
        BigDecimal beforeBuyTradeAmount = BigDecimal.ZERO;
        for (ThirdSecondTransactionDataDTO data:datas){
            index++;
            BigDecimal tradePrice = data.getTradePrice();
            BigDecimal tradeAmount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            beforeBuyTradeAmount = beforeBuyTradeAmount.add(tradeAmount);
            BigDecimal rate = PriceUtil.getPricePercentRate(tradePrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            if(firstHighTime!=null){
                Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
                Date firstHighDate = DateUtil.parseDate(firstHighTime, DateUtil.HH_MM);
                Date newHighDate = DateUtil.addStockMarketMinutes(firstHighDate, 30);
                if(data.getTradePrice().compareTo(firstHighPrice)>0){
                    if(date.after(newHighDate)){
                        buyIndex = index;
                        BigDecimal firtHighRate = PriceUtil.getPricePercentRate(firstHighPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                        BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                        Integer betweenMinute = DateUtil.calBetweenStockMinute(firstHighTime, data.getTradeTime());
                        buyDTO = new BoxStockBuyDTO();
                        buyDTO.setStockCode(stockKbar.getStockCode());
                        buyDTO.setStockName(stockKbar.getStockName());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setBuyPrice(data.getTradePrice());
                        buyDTO.setBuyTime(data.getTradeTime());
                        buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                        buyDTO.setTotalCirculateZ(circulateInfo.getCirculate());
                        buyDTO.setFirstHighRate(firtHighRate);
                        buyDTO.setFirstHighTime(firstHighTime);
                        buyDTO.setBuyTimeTradeAmount(beforeBuyTradeAmount);
                        buyDTO.setPreTradeAmount(preStockKbar.getTradeAmount());
                        buyDTO.setBetweenTime(betweenMinute);
                        buyDTO.setOpenRate(openRate);
                        break;
                    }else{
                        firstHighTime = data.getTradeTime();
                        firstHighPrice = data.getTradePrice();
                        firstHighIndex = index;
                    }
                }
            }
            if(firstHighTime==null && rate.compareTo(new BigDecimal("3"))>0){
                firstHighTime = data.getTradeTime();
                firstHighPrice = tradePrice;
                firstHighIndex = index;
            }
        }
        if(buyDTO!=null){
            int i = 0;
            BigDecimal beforeLowPrice = null;
            BigDecimal betweenLowPrice = null;
            for (ThirdSecondTransactionDataDTO data:datas){
                i++;
                if(i<=firstHighIndex){
                    if(beforeLowPrice==null||data.getTradePrice().compareTo(beforeLowPrice)<0){
                        beforeLowPrice = data.getTradePrice();
                    }
                }
                if(i>firstHighIndex&&i<buyIndex){
                    if(betweenLowPrice==null||data.getTradePrice().compareTo(betweenLowPrice)<0){
                        betweenLowPrice = data.getTradePrice();
                    }
                }
                if(i>=buyIndex){
                    break;
                }
            }
            BigDecimal beforeLowRate = PriceUtil.getPricePercentRate(beforeLowPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            BigDecimal betweenLowRate = PriceUtil.getPricePercentRate(betweenLowPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            buyDTO.setBeforeHighLowRate(beforeLowRate);
            buyDTO.setAfterHighLowRate(betweenLowRate);
        }
        return buyDTO;
    }

    public static void main(String[] args) {
        /*Date date = DateUtil.addStockMarketMinutes(DateUtil.parseDate("11:29", DateUtil.HH_MM), 30);
        System.out.println(DateUtil.format(date,DateUtil.HH_MM));*/
        Integer integer = DateUtil.calBetweenStockMinute("11:23", "11:29");
        System.out.println(integer);
    }

}
