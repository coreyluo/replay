package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.BlockDaDieBestDTO;
import com.bazinga.dto.BlockStockBestDTO;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.TbondUseMainDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
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
public class BlockDropOpenHighComponent {
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
    private TransferableBondInfoService transferableBondInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private BlockKbarSelfService blockKbarSelfService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    public void chaoDie(){
        List<TbondUseMainDTO> dtos = choaDieInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(TbondUseMainDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getMainCode());
            list.add(dto.getMainName());
            list.add(dto.getTradeDate());
            list.add(dto.getTradeTime());
            list.add(dto.getTbondTradeTime());
            list.add(dto.getBuyRate());
            list.add(dto.getTradePrice());
            list.add(dto.getBeforeSellQuantity());
            list.add(dto.getBeforeTradeDeal());
            list.add(dto.getSellTime());
            list.add(dto.getTbondSellTime());
            list.add(dto.getSellRate());
            list.add(dto.getSellPrice());
            list.add(dto.getAvg10TradeDeal());
            list.add(dto.getBuyTimeRate());
            list.add(dto.getTbondBuyTimeRate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","主板股票代码","主板股票名称","买入日期","主板买入时间","转载买入时间","买入时涨速","买入时价格","买入前总卖","买入前成交",
                "主板卖出时间","转债卖出时间","卖出时涨速","卖出时价格","买入前10跳平均成交","主板买入时候涨幅","转载买入时候涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("主板转债",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("主板转债");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<TbondUseMainDTO> choaDieInfo(){
        List<TbondUseMainDTO> list = new ArrayList<>();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-09-01",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("2021-11-10",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<TransferableBondInfo> tbInfos = transferableBondInfoService.listByCondition(new TransferableBondInfoQuery());
        for (TransferableBondInfo tbInfo:tbInfos){
            String mainCode = tbInfo.getMainCode();
            /*if(!mainCode.equals("600483")){
                continue;
            }*/
            List<StockKbar> kbars = getKbars(tbInfo.getStockCode(), tbInfo.getStockName());
            System.out.println(tbInfo.getStockCode());
            TradeDatePool preTradeDate   = null;
            for (TradeDatePool tradeDatePool:tradeDatePools){
                BigDecimal tbondPreEndPrice = getPreEndPrice(kbars, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(preTradeDate!=null){
                    String key = mainCode+"_"+DateUtil.format(preTradeDate.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar stockKbar = stockKbarService.getByUniqueKey(key);
                    if(stockKbar!=null){
                        List<TbondUseMainDTO> tbondUseMainDTOS = blockDropInfo(tbInfo,mainCode, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd), stockKbar.getClosePrice());
                        if(!CollectionUtils.isEmpty(tbondUseMainDTOS)){
                            List<TbondUseMainDTO> buys = getBuyInfo(tbondUseMainDTOS, tbInfo.getStockCode(), DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),tbondPreEndPrice);
                            list.addAll(buys);
                           /* if(list.size()>5){
                                return list;
                            }*/
                        }
                    }
                }
                preTradeDate = tradeDatePool;
            }
        }
        return list;
    }

    public BigDecimal getPreEndPrice(List<StockKbar> kbars,String tradeDate){
        if(CollectionUtils.isEmpty(kbars)){
            return null;
        }
        BigDecimal preEndPrice = null;
        for (StockKbar stockKbar:kbars){
            if(stockKbar.getKbarDate().equals(tradeDate)){
                return preEndPrice;
            }
            preEndPrice = stockKbar.getClosePrice();
        }
        return null;
    }

    public List<TbondUseMainDTO> getBuyInfo(List<TbondUseMainDTO> list,String tbondCode,String tradeDate,BigDecimal tbondPreEndPrice){
        List<TbondUseMainDTO> datas = Lists.newArrayList();
        List<ThsQuoteInfo> quotes = quotes(tbondCode, tradeDate);
        if(CollectionUtils.isEmpty(quotes)){
            return datas;
        }
        int preSellTimeInt = 92500;
        for (TbondUseMainDTO dto:list){
            String mainStockTime = dto.getTradeTime().replace(":", "");
            int mainTimeInt = getIntTime(mainStockTime);
            int mainSellTimeInt = 150003;
            if(!StringUtils.isBlank(dto.getSellTime())) {
                String mainStockSellTime = dto.getSellTime().replace(":", "");
                mainSellTimeInt = getIntTime(mainStockSellTime);
            }
            if(mainTimeInt<=preSellTimeInt){
                continue;
            }
            int beforeTimeInt = 0;
            ThsQuoteInfo beforeQuote  = null;
            LimitQueue<ThsQuoteInfo> limitQueue = new LimitQueue<>(10);
            LimitQueue<ThsQuoteInfo> limitQueueSell = new LimitQueue<>(10);
            int i = 0;
            for (ThsQuoteInfo quote:quotes){
                int intTime = getIntTime(quote.getQuoteTime());
                limitQueueSell.offer(quote);
                if(beforeQuote!=null) {
                    if (mainTimeInt > beforeTimeInt && mainTimeInt < intTime) {
                        //System.out.println(dto.getStockCode()+"==="+dto.getMainCode());
                        dto.setTbondTradeTime(beforeQuote.getQuoteTime());
                        dto.setTradePrice(beforeQuote.getCurrentPrice());
                        dto.setBeforeTradeDeal(beforeQuote.getVol());
                        dto.setBeforeSellQuantity(beforeQuote.getTotalSellVolume());
                        before10AvgInfo(limitQueue, dto);
                    }
                    if (mainTimeInt > beforeTimeInt && mainTimeInt == intTime) {
                        limitQueue.offer(quote);
                        dto.setTbondTradeTime(quote.getQuoteTime());
                        dto.setTradePrice(quote.getCurrentPrice());
                        dto.setBeforeTradeDeal(quote.getVol());
                        dto.setBeforeSellQuantity(quote.getTotalSellVolume());
                        before10AvgInfo(limitQueue, dto);
                    }

                    if (mainSellTimeInt > beforeTimeInt && mainSellTimeInt < intTime) {
                        dto.setTbondSellTime(beforeQuote.getQuoteTime());
                        dto.setSellPrice(beforeQuote.getCurrentPrice());
                        if (dto.getTradePrice() != null && dto.getSellPrice() != null) {
                            BigDecimal rate = PriceUtil.getPricePercentRate(dto.getSellPrice().subtract(dto.getTradePrice()), dto.getTradePrice());
                            dto.setProfit(rate);
                        }
                        break;
                    }
                    if (mainSellTimeInt > beforeTimeInt && mainSellTimeInt == intTime) {
                        dto.setTbondSellTime(quote.getQuoteTime());
                        dto.setSellPrice(quote.getCurrentPrice());
                        if (dto.getTradePrice() != null && dto.getSellPrice() != null) {
                            BigDecimal rate = PriceUtil.getPricePercentRate(dto.getSellPrice().subtract(dto.getTradePrice()), dto.getTradePrice());
                            dto.setProfit(rate);
                        }
                        break;
                    }
                    if(dto.getTbondTradeTime() != null){
                        i++;
                    }
                    if(dto.getTbondTradeTime()!=null && i>=5){
                        BigDecimal rate = before30SecondRate(limitQueueSell, dto);
                        if(rate.compareTo(new BigDecimal("0"))==-1){
                            dto.setTbondSellTime(beforeQuote.getQuoteTime());
                            dto.setSellPrice(quote.getCurrentPrice());
                            if (dto.getTradePrice() != null && dto.getSellPrice() != null) {
                                BigDecimal profit = PriceUtil.getPricePercentRate(dto.getSellPrice().subtract(dto.getTradePrice()), dto.getTradePrice());
                                dto.setProfit(profit);
                            }
                            break;
                        }
                    }
                }
                beforeTimeInt = intTime;
                beforeQuote = quote;
                limitQueue.offer(quote);
            }
            if(dto.getSellPrice()==null){
                dto.setTbondSellTime(beforeQuote.getQuoteTime());
                dto.setSellPrice(beforeQuote.getCurrentPrice());
            }
            if(dto.getProfit()!=null) {
                BigDecimal rate = PriceUtil.getPricePercentRate(dto.getTradePrice().subtract(tbondPreEndPrice), tbondPreEndPrice);
                dto.setTbondBuyTimeRate(rate);
                datas.add(dto);
                int intTime = getIntTime(dto.getTbondSellTime());
                preSellTimeInt = intTime;
            }
        }
        return datas;
    }
    public BigDecimal before30SecondRate(LimitQueue<ThsQuoteInfo> limitQueue,TbondUseMainDTO dto){
        if(limitQueue.size()<3){
            return null;
        }
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        ThsQuoteInfo first = null;
        ThsQuoteInfo last = null;
        while (iterator.hasNext()){
            ThsQuoteInfo quote = iterator.next();
            last = quote;
            if(first==null){
                first = quote;
            }
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(last.getCurrentPrice().subtract(first.getCurrentPrice()), dto.getPreEndPrice());
        return rate;
    }

    public void before10AvgInfo(LimitQueue<ThsQuoteInfo> limitQueue,TbondUseMainDTO dto){
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        int count = 0;
        long total = 0l;
        while (iterator.hasNext()){
            ThsQuoteInfo quote = iterator.next();
            count++;
            total = total+quote.getVol();
        }
        if(count>0){
            long avg = total / count;
            dto.setAvg10TradeDeal(avg);
        }
    }

    public int getIntTime(String timeStamp){
        String time  = timeStamp;
        if(timeStamp.startsWith("0")){
            time = time.substring(1);
        }
        Integer integer = Integer.valueOf(time);
        return integer;
    }

    public List<TbondUseMainDTO>  blockDropInfo(TransferableBondInfo tbInfo,String stockCode,String tradeDate,BigDecimal preEndPrice){
        List<TbondUseMainDTO> list = new ArrayList<>();
        /*if(!tradeDate.equals("20211101")){
            return list;
        }*/
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue  = new LimitQueue(10);
        boolean buyFlag = false;
        String timeStamp = null;
        int seconds = 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            if (data.getTradeTime().equals("09:25")){
                continue;
            }
            if(data.getTradeTime().equals(timeStamp)){
                seconds = seconds+3;
                if(seconds>=60){
                    seconds = 59;
                }
            }else{
                timeStamp = data.getTradeTime();
                seconds = 3;
            }
            limitQueue.offer(data);
            BigDecimal raiseRate = calRaise(limitQueue, preEndPrice);
            if(raiseRate!=null&&raiseRate.compareTo(new BigDecimal(0.5))==1){
                TbondUseMainDTO tbondBuy = new TbondUseMainDTO();
                tbondBuy.setTradeDate(tradeDate);
                tbondBuy.setStockCode(tbInfo.getStockCode());
                tbondBuy.setStockName(tbInfo.getStockName());
                tbondBuy.setMainCode(tbInfo.getMainCode());
                tbondBuy.setMainName(tbInfo.getMainName());
                tbondBuy.setPreEndPrice(preEndPrice);
                if(seconds>=10) {
                    tbondBuy.setTradeTime(data.getTradeTime() + ":" + seconds);
                }else{
                    tbondBuy.setTradeTime(data.getTradeTime() + ":0" + seconds);
                }
                tbondBuy.setBuyRate(raiseRate);
                BigDecimal buyTimeRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(preEndPrice), preEndPrice);
                tbondBuy.setBuyTimeRate(buyTimeRate);
                list.add(tbondBuy);
            }
            if(raiseRate!=null&&raiseRate.compareTo(new BigDecimal("0"))==-1){
                for(TbondUseMainDTO dto:list){
                    if(dto.getSellTime()==null) {
                        dto.setSellRate(raiseRate);
                        if (seconds >= 10) {
                            dto.setSellTime(data.getTradeTime() + ":" + seconds);
                        } else {
                            dto.setSellTime(data.getTradeTime() + ":0" + seconds);
                        }
                    }
                }
            }
        }
        return list;
    }
    public BigDecimal calRaise(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal preEndPrice){
        if(limitQueue==null||limitQueue.size()<2){
            return null;
        }
        ThirdSecondTransactionDataDTO first = null;
        ThirdSecondTransactionDataDTO last = null;
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            ThirdSecondTransactionDataDTO data = iterator.next();
            if(first==null){
                first = data;
            }
            last = data;
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(last.getTradePrice().subtract(first.getTradePrice()), preEndPrice);
        return rate;
    }

    public List<ThsQuoteInfo> quotes(String stockCode,String tradeDate){
        ThsQuoteInfoQuery query = new ThsQuoteInfoQuery();
        query.setStockCode(stockCode);
        query.setQuoteDate(tradeDate);
        query.addOrderBy("quote_time", Sort.SortType.ASC);
        List<ThsQuoteInfo> quotes = thsQuoteInfoService.listByCondition(query);
        if(CollectionUtils.isEmpty(quotes)){
            return quotes;
        }
        List<ThsQuoteInfo> list = Lists.newArrayList();
        for (ThsQuoteInfo quote:quotes){
            if(StringUtils.isBlank(quote.getQuoteTime())){
                continue;
            }
            if(quote.getTotalSellVolume()==null||quote.getTotalSellVolume()==0){
                continue;
            }
            if(quote.getCurrentPrice()==null||quote.getTotalSellVolume()==0){
                continue;
            }
            String quoteTime = quote.getQuoteTime();
            if(quoteTime.equals("092500")){
                list.add(quote);
                continue;
            }
            if(quoteTime.startsWith("0")){
                quoteTime = quoteTime.substring(1);
            }
            Integer timeInt = Integer.valueOf(quoteTime);
            if(timeInt<93000 && quote.getVol()>0){
                if(quote.getStockCode().startsWith("11")&&quote.getVol()!=null){
                    quote.setVol(quote.getVol()*10);
                }
                if(quote.getStockCode().startsWith("11")&&quote.getTotalSellVolume()!=null){
                    quote.setTotalSellVolume(quote.getTotalSellVolume()*10);
                }
                list.add(quote);
            }
            if(timeInt>=93000&&timeInt<=150006){
                if(quote.getStockCode().startsWith("11")&&quote.getVol()!=null){
                    quote.setVol(quote.getVol()*10);
                }
                if(quote.getStockCode().startsWith("11")&&quote.getTotalSellVolume()!=null){
                    quote.setTotalSellVolume(quote.getTotalSellVolume()*10);
                }
                list.add(quote);
            }
        }
        return list;
    }

    public List<StockKbar> getKbars(String stockCode, String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 100);
        List<StockKbar> stockKbars = StockKbarConvert.convert(securityBars,stockCode,stockName);
        return stockKbars;
    }

    public static void main(String[] args) {
        LevelDTO levelDTO1 = new LevelDTO();
        levelDTO1.setRate(new BigDecimal(1));
        LevelDTO levelDTO2 = new LevelDTO();
        levelDTO2.setRate(new BigDecimal(2));
        LevelDTO levelDTO3 = new LevelDTO();
        levelDTO3.setRate(new BigDecimal(3));
        LevelDTO levelDTO4 = new LevelDTO();
        levelDTO4.setRate(new BigDecimal(4));

        List<LevelDTO> list = Lists.newArrayList();
        /*list.add(levelDTO3);
        list.add(levelDTO4);
        list.add(levelDTO2);
        list.add(levelDTO1);*/
        Collections.sort(list);
        System.out.println(11);
    }


}
