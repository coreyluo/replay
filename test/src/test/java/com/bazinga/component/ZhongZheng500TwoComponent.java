package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BestBuyDTO;
import com.bazinga.dto.StockKbarRateDTO;
import com.bazinga.dto.ZhongZhengBestDTO;
import com.bazinga.dto.Zz500BuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
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
public class ZhongZheng500TwoComponent {
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
    public void zz500BuyTwo(){
        List<ZhongZhengBestDTO> zhongZhengBestDTOS = zz500KbarInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(ZhongZhengBestDTO dto:zhongZhengBestDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getFitDate());
            list.add(dto.getPoLineAvgMoney());
            list.add(dto.getNeLineAvgMoney());
            list.add(dto.getRateDay10());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","符合條件的日期","阳线平均成交额","阴线平均成交额","10日涨幅","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("中证500",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("中证500");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<ZhongZhengBestDTO> zz500KbarInfo(){
        List<ZhongZhengBestDTO> dtos = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            if(!circulateInfo.getStockCode().equals("300514")){
                continue;
            }
            Map<String,ZhongZhengBestDTO> map = new HashMap<>();
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 150);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue11 = new LimitQueue<>(11);
            for (StockKbar stockKbar:stockKbars){
                limitQueue11.offer(stockKbar);
                ZhongZhengBestDTO bestDTO = fitCondition(limitQueue11);
                if(bestDTO!=null){
                    bestDTO.setStockCode(stockKbar.getStockCode());
                    bestDTO.setStockName(stockKbar.getStockName());
                    bestDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    bestDTO.setFitDate(stockKbar.getKbarDate());
                    findBuyDay(stockKbars,bestDTO);
                    if(bestDTO.getTradeDate()!=null){
                        map.put(stockKbar.getKbarDate(),bestDTO);
                    }
                }
            }
            selectBestKbarInfo(stockKbars,map,dtos);
        }
        return dtos;
    }

    public void selectBestKbarInfo(List<StockKbar> stockKbars, Map<String, ZhongZhengBestDTO> map,List<ZhongZhengBestDTO> dtos){
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            ZhongZhengBestDTO bestDTO = map.get(stockKbar.getKbarDate());
            if(bestDTO!=null){
                if(i>11){
                    dtos.add(bestDTO);
                    i=0;
                }
            }
            i++;
        }
    }


    public void findBuyDay(List<StockKbar> stockKbars,ZhongZhengBestDTO bestDTO){
        int i=0;
        int j = 0;
        boolean flag = false;
        StockKbar preKbar = null;
        boolean buyFlag  = false;
        BigDecimal raiseBigMoney = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(bestDTO.getTradeDate()!=null){
                j++;
            }
            if(j>=1&&j<=5){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                if(rate.compareTo(BigDecimal.ZERO)==1){
                    if(raiseBigMoney==null||stockKbar.getTradeAmount().compareTo(raiseBigMoney)==1){
                        raiseBigMoney = stockKbar.getTradeAmount();
                    }
                }else{
                    if(raiseBigMoney!=null && (stockKbar.getTradeAmount().divide(raiseBigMoney,2,BigDecimal.ROUND_HALF_UP)).compareTo(new BigDecimal("0.8"))==1){
                        BigDecimal profit = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(bestDTO.getStockKbar().getAdjOpenPrice()), bestDTO.getStockKbar().getAdjOpenPrice());
                        bestDTO.setProfit(profit);
                        return;
                    }
                }
                if(j==5&&bestDTO.getProfit()==null){
                    BigDecimal profit = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(bestDTO.getStockKbar().getAdjOpenPrice()), bestDTO.getStockKbar().getAdjOpenPrice());
                    bestDTO.setProfit(profit);
                    return;
                }
            }
            if(i>=1&&i<=10&&bestDTO.getTradeDate()==null){
                if(buyFlag){
                    bestDTO.setTradeDate(stockKbar.getKbarDate());
                    bestDTO.setStockKbar(stockKbar);
                }
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                if(endRate.compareTo(new BigDecimal("-3"))==-1){
                    buyFlag = true;
                }
            }
            if(i==20){
                return;
            }
            if(stockKbar.getKbarDate().equals(bestDTO.getFitDate())){
                flag = true;
            }
            preKbar = stockKbar;
        }
    }

    public ZhongZhengBestDTO fitCondition(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<11){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        BigDecimal raiseMoneyTotal = BigDecimal.ZERO;
        int raiseCount = 0;
        BigDecimal dropMoneyTotal = BigDecimal.ZERO;
        int dropCount = 0;
        BigDecimal raiseRate = null;
        StockKbar first = null;
        StockKbar preKbar = null;
        int i = 0;
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            if(i==0){
                first = next;
            }
            if(i>=1) {
                BigDecimal endRate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                boolean isUpper = PriceUtil.isUpperPrice(next.getClosePrice(), preKbar.getClosePrice());
                if(!isUpper){
                    if(endRate.compareTo(BigDecimal.ZERO)==1){
                        raiseMoneyTotal = raiseMoneyTotal.add(next.getTradeAmount());
                        raiseCount = raiseCount+1;
                    }else{
                        dropMoneyTotal = dropMoneyTotal.add(next.getTradeAmount());
                        dropCount  = dropCount+1;
                    }
                }
            }
            if(i==10){
                raiseRate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(first.getAdjClosePrice()), first.getAdjClosePrice());
            }
            i++;
            preKbar = next;
        }
        if(raiseCount<=dropCount){
            return null;
        }
        BigDecimal dropAvg  = null;
        if(dropCount!=0){
            dropAvg = dropMoneyTotal.divide(new BigDecimal(dropCount), 2, BigDecimal.ROUND_HALF_UP);
        }
        BigDecimal raiseAvg  = null;
        if(raiseCount!=0){
            raiseAvg = raiseMoneyTotal.divide(new BigDecimal(raiseCount), 2, BigDecimal.ROUND_HALF_UP);
        }
        if (raiseRate.compareTo(new BigDecimal("-5"))==1&&raiseRate.compareTo(new BigDecimal("25"))==-1){
            if(dropAvg==null||raiseAvg.compareTo(dropAvg)==1) {
                ZhongZhengBestDTO bestDTO = new ZhongZhengBestDTO();
                bestDTO.setNeLineAvgMoney(dropAvg);
                bestDTO.setPoLineAvgMoney(raiseAvg);
                bestDTO.setRateDay10(raiseRate);
                return bestDTO;
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
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);
    }


}
