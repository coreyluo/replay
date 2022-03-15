package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.DaPanDropDTO;
import com.bazinga.dto.DaPanDropPlankDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
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
public class GuaiLilvPlankComponent {
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


    public void guaiLiLv(){
        List<DaPanDropDTO> dailys = getStockUpperShowInfo();
        List<DaPanDropPlankDTO> buys = getGuaiLiLvDiDian(dailys);
        List<Object[]> datas = Lists.newArrayList();
        for(DaPanDropPlankDTO dto:buys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getTradeDate());
            list.add(dto.getBuyTime());
            list.add(dto.getProfit());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","买入时间","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("乖离率",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("乖离率");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<DaPanDropPlankDTO> getGuaiLiLvDiDian(List<DaPanDropDTO> list){
        List<DaPanDropPlankDTO> buyDtos = Lists.newArrayList();
        for (DaPanDropDTO dto:list){
            List<DaPanDropPlankDTO> buys = getStockKbars(dto);
            buyDtos.addAll(buys);
        }
        return buyDtos;
    }

    public List<DaPanDropPlankDTO> getStockKbars(DaPanDropDTO dto){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(dto.getTradeDate());
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbarQuery preKbarQuery = new StockKbarQuery();
        preKbarQuery.setKbarDate(dto.getPreTradeDate());
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preKbarQuery);
        StockKbarQuery nextKbarQuery = new StockKbarQuery();
        nextKbarQuery.setKbarDate(dto.getNextTradeDate());
        List<StockKbar> nextStockKbars = stockKbarService.listByCondition(nextKbarQuery);

        Map<String, StockKbar> tradeDateMap = new HashMap<>();
        Map<String, StockKbar> preTradeDateMap = new HashMap<>();
        Map<String, StockKbar> nextTradeDateMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getTradeQuantity()>=100) {
                tradeDateMap.put(stockKbar.getStockCode(), stockKbar);
            }
        }
        for (StockKbar stockKbar:preStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                preTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        for (StockKbar stockKbar:nextStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                nextTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        Date startTime = DateUtil.parseDate(dto.getTimeStamp(), DateUtil.HH_MM);
        Date endTime = DateUtil.addMinutes(startTime, 10);
        List<DaPanDropPlankDTO> list = Lists.newArrayList();
        for (String stockCode:tradeDateMap.keySet()){
            StockKbar stockKbar = tradeDateMap.get(stockCode);
            StockKbar preStockKbar = preTradeDateMap.get(stockCode);
            StockKbar nextStockKbar = nextTradeDateMap.get(stockCode);
            if(stockKbar==null||preStockKbar==null){
                continue;
            }
            boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
            if(historyUpperPrice) {
                String buyTime = buyTime(stockCode, stockKbar.getKbarDate(), startTime, endTime, preStockKbar);
                if(buyTime!=null){
                    DaPanDropPlankDTO buyDTO = new DaPanDropPlankDTO();
                    buyDTO.setStockCode(stockKbar.getStockCode());
                    buyDTO.setStockName(stockKbar.getStockName());
                    buyDTO.setBuyTime(buyTime);
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    BigDecimal profit = calProfit(stockKbar, nextStockKbar);
                    buyDTO.setProfit(profit);
                    list.add(buyDTO);
                }
            }

        }
        return list;
    }

    public String buyTime(String stockCode,String tradeDate,Date startTime,Date endTime,StockKbar preStockKbar){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean havePlank =false;
        boolean preIsPlank = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            Integer tradeType = data.getTradeType();
            if(tradeType==null){
                continue;
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
            boolean upperPice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
            if(havePlank && !preIsPlank && upperPice&&tradeType==1){
                if(date.after(startTime)&&!date.after(endTime)){
                    return data.getTradeTime();
                }
            }
            if(upperPice&&tradeType==1){
                  havePlank = true;
                  preIsPlank = true;
            }else{
                preIsPlank = false;
            }
        }
        return null;
    }


    public List<DaPanDropDTO> getStockUpperShowInfo(){
        List<DaPanDropDTO> list = Lists.newArrayList();
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(23);
        StockKbar preStockKbar = null;
        DaPanDropDTO buy = null;
        for (StockKbar stockKbar:stockKbars){
            if(preStockKbar!=null){
                DaPanDropDTO daPanDropDTO = getHistoryInfo(stockKbar, limitQueue,preStockKbar);
                if(daPanDropDTO!=null) {
                    list.add(daPanDropDTO);
                }
                if(buy!=null){
                    buy.setNextTradeDate(stockKbar.getKbarDate());
                }
                buy = daPanDropDTO;
            }
            limitQueue.offer(stockKbar);
            preStockKbar = stockKbar;
        }
        return list;
    }
    public DaPanDropDTO getHistoryInfo(StockKbar stockKbar,LimitQueue<StockKbar> limitQueue,StockKbar preStockKbar){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        Long totalGather = null;
        Long total0930 = null;
        Long totalCurrent = 0L;
        String preTimeStamp = "09:30";
        for(ThirdSecondTransactionDataDTO data:datas){
            if(data.getTradeTime().equals("09:25")){
                totalGather = data.getTradeQuantity().longValue();
            }
            if(data.getTradeTime().equals("09:30")){
                if(total0930==null){
                    total0930 = data.getTradeQuantity().longValue();
                }else{
                    total0930 = total0930+data.getTradeQuantity().longValue();
                }
            }
            if(!preTimeStamp.equals(data.getTradeTime())){
                totalCurrent = 0L;
                preTimeStamp = data.getTradeTime();
            }
            totalCurrent = totalCurrent+data.getTradeQuantity().longValue();
            if(data.getTradeTime().equals("09:25")||data.getTradeTime().equals("09:30")||data.getTradeTime().equals("13:00")||data.getTradeTime().startsWith("15")){
                continue;
            }
            BigDecimal avgPrice = calAvgPrice(limitQueue, data.getTradePrice());
            if(avgPrice==null){
                continue;
            }
            if(totalCurrent==0||total0930==null||totalGather==null){
                continue;
            }
            BigDecimal percent = new BigDecimal(totalCurrent).divide(new BigDecimal(total0930+totalGather), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal glv = (data.getTradePrice().subtract(avgPrice).divide(avgPrice,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))).setScale(2);
            BigDecimal relativeOpenRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(stockKbar.getOpenPrice()), preStockKbar.getClosePrice());
            if(glv.compareTo(new BigDecimal("-4.8"))==-1&&percent.compareTo(new BigDecimal("0.4"))==1&&relativeOpenRate.compareTo(new BigDecimal("-0.9"))==-1){
                    DaPanDropDTO daPanDropDTO = new DaPanDropDTO();
                    daPanDropDTO.setDropRate(glv);
                    daPanDropDTO.setPercent(percent);
                    daPanDropDTO.setTimeStamp(data.getTradeTime());
                    daPanDropDTO.setTradeDate(stockKbar.getKbarDate());
                    daPanDropDTO.setRelativeOpenRate(relativeOpenRate);
                    daPanDropDTO.setPreTradeDate(preStockKbar.getKbarDate());
                    return daPanDropDTO;
            }
        }
        return null;
    }

    public BigDecimal calAvgPrice(LimitQueue<StockKbar> limitQueue,BigDecimal currentPrice){
        if(limitQueue.size()<23){
            return null;
        }
        BigDecimal totalPrice = currentPrice;
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            totalPrice = totalPrice.add(next.getClosePrice());
        }
        BigDecimal rate = totalPrice.divide(new BigDecimal(24), 4, BigDecimal.ROUND_HALF_UP);
        return rate;
    }


    public BigDecimal calProfit(StockKbar buyKbar,StockKbar nextStockKbar){
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(nextStockKbar.getStockCode(), nextStockKbar.getKbarDate());
        if(avgPrice==null){
            return null;
        }
        BigDecimal reason = null;
        if(!(nextStockKbar.getClosePrice().equals(nextStockKbar.getAdjClosePrice()))&&!(nextStockKbar.getOpenPrice().equals(nextStockKbar.getAdjOpenPrice()))){
            reason = nextStockKbar.getAdjOpenPrice().divide(nextStockKbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            avgPrice = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(avgPrice.subtract(buyKbar.getAdjHighPrice()), buyKbar.getAdjHighPrice());
        return rate;
    }


    public static void main(String[] args) {

    }


}
