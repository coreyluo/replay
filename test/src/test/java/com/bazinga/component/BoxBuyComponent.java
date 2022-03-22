package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.BoxBuyDTO;
import com.bazinga.dto.DaPanDropDTO;
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

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BoxBuyComponent {
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


    public void xiangTi(){
        List<BoxBuyDTO> dailys = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(BoxBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getBoxRate());
            list.add(dto.getBoxPercent());
            list.add(dto.getBoxMaxExchangeMoney());
            list.add(dto.getAvgExchangeMoneyDay10());
            list.add(dto.getBuyDayExchangeMoney());
            list.add(dto.getBuyDayCloseRate());
            list.add(dto.getSellDate());
            list.add(dto.getHandleDays());
            list.add(dto.getProfit());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","tradeDate","箱体振幅","箱体中间段比例","箱体内最大成交额","箱体10天平均成交额","买入日成交额","买入日收盘涨幅","卖出日期","距离卖出天数","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("箱体",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("箱体");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<BoxBuyDTO> getStockUpperShowInfo(){
        List<BoxBuyDTO> results = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(11);
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))){
                    continue;
                }
                limitQueue.offer(stockKbar);
                if(preStockKbar!=null) {
                    BoxBuyDTO boxBuyDTO = calBoxBuy(stockKbar, preStockKbar, limitQueue);
                    if(boxBuyDTO!=null){
                        calProfit(stockKbars,stockKbar,boxBuyDTO);
                        results.add(boxBuyDTO);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return results;
    }

    public void calProfit(List<StockKbar> stockKbars,StockKbar buyKbar,BoxBuyDTO boxBuyDTO){
        StockKbar preStockKbar = null;
        boolean flag = false;
        int i = 0;
        int totalGreen = 0;
        int continueGreen = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i>0){
                if(stockKbar.getAdjClosePrice().compareTo(preStockKbar.getAdjClosePrice())==-1){
                    totalGreen++;
                    continueGreen++;
                }else{
                    continueGreen = 0;
                }
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                if(rate.compareTo(new BigDecimal(-6))<=0){
                    boxBuyDTO.setHandleDays(i);
                    boxBuyDTO.setProfit(rate);
                    boxBuyDTO.setSellDate(stockKbar.getKbarDate());
                    return;
                }
                if(totalGreen==3){
                    boxBuyDTO.setHandleDays(i);
                    boxBuyDTO.setProfit(rate);
                    boxBuyDTO.setSellDate(stockKbar.getKbarDate());
                    return;
                }
                if(continueGreen==2){
                    boxBuyDTO.setHandleDays(i);
                    boxBuyDTO.setProfit(rate);
                    boxBuyDTO.setSellDate(stockKbar.getKbarDate());
                    return;
                }

            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            preStockKbar = stockKbar;
        }
    }

    public BoxBuyDTO calBoxBuy(StockKbar stockKbar,StockKbar preStockKbar, LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<11){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        int i = 0;
        BigDecimal highPrice = null;
        BigDecimal lowPrice = null;
        BigDecimal maxExchangeMoney = null;
        BigDecimal totalExchangeMoney = BigDecimal.ZERO;
        while (iterator.hasNext()){
            i++;
            StockKbar kbar = iterator.next();
            if(i<=10) {
                if (highPrice == null || kbar.getAdjHighPrice().compareTo(highPrice) == 1) {
                    highPrice = kbar.getAdjHighPrice();
                }
                if (lowPrice == null || kbar.getAdjLowPrice().compareTo(lowPrice) == -1) {
                    lowPrice = kbar.getAdjLowPrice();
                }
                if(maxExchangeMoney==null||kbar.getTradeAmount().compareTo(maxExchangeMoney)==1){
                    maxExchangeMoney = kbar.getTradeAmount();
                }
                totalExchangeMoney = totalExchangeMoney.add(kbar.getTradeAmount());
                list.add(kbar);
            }

        }
        if(highPrice!=null&&lowPrice!=null){
            BigDecimal boxRate = PriceUtil.getPricePercentRate(highPrice.subtract(lowPrice), lowPrice);
            BigDecimal avgExchangeMoneyDay11 = totalExchangeMoney.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
            if(boxRate.compareTo(new BigDecimal("8"))<0 && stockKbar.getAdjClosePrice().compareTo(highPrice)==1 && stockKbar.getAdjClosePrice().compareTo(preStockKbar.getAdjClosePrice())==1){
                BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                BoxBuyDTO boxBuyDTO = new BoxBuyDTO();
                boxBuyDTO.setBoxRate(boxRate);
                boxBuyDTO.setStockCode(stockKbar.getStockCode());
                boxBuyDTO.setStockName(stockKbar.getStockName());
                boxBuyDTO.setBuyKbar(stockKbar);
                boxBuyDTO.setTradeDate(stockKbar.getKbarDate());
                boxBuyDTO.setBoxMaxExchangeMoney(maxExchangeMoney);
                boxBuyDTO.setAvgExchangeMoneyDay10(avgExchangeMoneyDay11);
                boxBuyDTO.setBuyDayExchangeMoney(stockKbar.getTradeAmount());
                BigDecimal totalBoxPercent = boxPercent(highPrice, lowPrice, list);
                boxBuyDTO.setBoxPercent(totalBoxPercent);
                boxBuyDTO.setBuyDayCloseRate(closeRate);
                if(!historyUpperPrice) {
                    return boxBuyDTO;
                }
            }
        }
        return null;
    }

    public BigDecimal boxPercent(BigDecimal highPrice,BigDecimal lowPrice,List<StockKbar> list){
        BigDecimal subPrice = (highPrice.subtract(lowPrice)).multiply(new BigDecimal(0.25)).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal halfPrice = (highPrice.add(lowPrice)).divide(new BigDecimal(2), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal upperPrice = halfPrice.add(subPrice).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal downPrice = halfPrice.subtract(subPrice).setScale(2,BigDecimal.ROUND_HALF_UP);
        if(upperPrice.compareTo(downPrice)==0){
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (StockKbar stockKbar:list){
            BigDecimal adjHighPrice = stockKbar.getAdjHighPrice();
            BigDecimal adjLowPrice = stockKbar.getAdjLowPrice();
            if(adjHighPrice.compareTo(downPrice)<=0){
                continue;
            }
            if(adjHighPrice.compareTo(downPrice)==1&&adjHighPrice.compareTo(upperPrice)<=0){
                if(adjLowPrice.compareTo(downPrice)<=0){
                    BigDecimal percent = (adjHighPrice.subtract(downPrice)).divide(upperPrice.subtract(downPrice), 4, BigDecimal.ROUND_HALF_UP);
                    total = total.add(percent);
                    continue;
                }else {
                    BigDecimal percent = (adjHighPrice.subtract(adjLowPrice)).divide(upperPrice.subtract(downPrice), 4, BigDecimal.ROUND_HALF_UP);
                    total = total.add(percent);
                    continue;
                }
            }
            if(adjHighPrice.compareTo(upperPrice)>=0){
                if(adjLowPrice.compareTo(downPrice)<=0){
                    BigDecimal percent = new BigDecimal(1);
                    total = total.add(percent);
                    continue;
                }else if(adjLowPrice.compareTo(downPrice)>0&&adjLowPrice.compareTo(upperPrice)<=0){
                    BigDecimal percent = (upperPrice.subtract(adjLowPrice)).divide(upperPrice.subtract(downPrice), 4, BigDecimal.ROUND_HALF_UP);
                    total = total.add(percent);
                    continue;
                }else{
                    continue;
                }
            }
        }
        return total;
    }


}
