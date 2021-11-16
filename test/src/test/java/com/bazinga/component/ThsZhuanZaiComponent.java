package com.bazinga.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.ZhuanZaiBuyDTO;
import com.bazinga.dto.ZhuanZaiExcelDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsQuoteInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsZhuanZaiComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public void zhuanZaiBuy(List<ZhuanZaiExcelDTO> zhuanZais){
        List<Object[]> datas = Lists.newArrayList();
        List<ZhuanZaiBuyDTO> buyDTOS = zhuanZaiStocks(zhuanZais);
        for(ZhuanZaiBuyDTO dto:buyDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getMarketAmount());
            list.add(dto.getTradeDate());
            list.add(dto.getBuyPrice());
            list.add(dto.getBuyTime());
            list.add(dto.getSellPrice());
            list.add(dto.getSellTime());
            list.add(dto.getBeforeBuyTotalSell());
            list.add(dto.getBeforeBuyFenShi());
            list.add(dto.getBuyTotalSell());
            list.add(dto.getBuyFenShi());
            list.add(dto.getBeforeAvgFenShi());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","市值","交易日期","买入价格","买入时间","买出价格","买出时间","买入前总卖","买入前分时","买入时总卖","买入时分时","买入前10跳平均分时","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("转债无敌",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("转债无敌");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<ZhuanZaiBuyDTO> zhuanZaiStocks(List<ZhuanZaiExcelDTO> dataList){
        List<ZhuanZaiBuyDTO> datas = Lists.newArrayList();
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateUtil.parseDate("2021-11-10 15:30:30",DateUtil.DEFAULT_FORMAT));
        query.setTradeDateFrom(DateUtil.parseDate("20210901",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        for(ZhuanZaiExcelDTO zhuanZai:dataList){
            List<StockKbar> kbars = getKbars(zhuanZai.getStockCode(), zhuanZai.getStockName());
            for(TradeDatePool tradeDatePool:tradeDatePools){
                System.out.println(zhuanZai.getStockCode()+"======"+DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                BigDecimal preEndPrice = getPreEndPrice(kbars, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(preEndPrice==null){
                    continue;
                }
                List<ThsQuoteInfo> quotes = quotes(zhuanZai.getStockCode(), DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(CollectionUtils.isEmpty(quotes)){
                    continue;
                }
                List<ZhuanZaiBuyDTO> buyDTOS = quoteBuyInfo(quotes, preEndPrice, zhuanZai, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(!CollectionUtils.isEmpty(buyDTOS)){
                    datas.addAll(buyDTOS);
                }
            }
           /* if(datas.size()>=10){
                return datas;
            }*/
        }
        return datas;
    }

    public List<ZhuanZaiBuyDTO> quoteBuyInfo(List<ThsQuoteInfo> list,BigDecimal preEndPrice,ZhuanZaiExcelDTO excelDTO,String tradeDate){
        List<ZhuanZaiBuyDTO> datas = Lists.newArrayList();
        LimitQueue<ThsQuoteInfo> limitQueue = new LimitQueue<>(3);
        LimitQueue<ThsQuoteInfo> limitQueue4 = new LimitQueue<>(5);
        LimitQueue<ThsQuoteInfo> limitQueue10 = new LimitQueue<>(10);
        boolean buyFlag = false;
        ThsQuoteInfo quoteLast = null;
        for (ThsQuoteInfo quote:list){
            Long totalSellVolume = quote.getTotalSellVolume();
            if(quote.getCurrentPrice()==null||quote.getTotalSellVolume()==null||quote.getTotalSellVolume()==0){
                continue;
            }
            quoteLast = quote;
            limitQueue.offer(quote);
            if (totalSellVolume != null && totalSellVolume > 50000 && !buyFlag) {
                ZhuanZaiBuyDTO buyDTO = new ZhuanZaiBuyDTO();
                BigDecimal rate = calRate(limitQueue, preEndPrice,buyDTO);
                if(rate!=null&&rate.compareTo(new BigDecimal("0.1"))>=0){
                    BigDecimal dropPercent = new BigDecimal(buyDTO.getBeforeBuyTotalSell() - buyDTO.getBuyTotalSell()).divide(new BigDecimal(buyDTO.getBeforeBuyTotalSell()),2,BigDecimal.ROUND_HALF_UP);
                    if(dropPercent.compareTo(new BigDecimal("0.08"))==1) {
                        buyDTO.setStockCode(excelDTO.getStockCode());
                        buyDTO.setStockName(excelDTO.getStockName());
                        buyDTO.setMarketAmount(excelDTO.getMarketAmount());
                        buyDTO.setTradeDate(tradeDate);
                        buyDTO.setBuyTime(quote.getQuoteTime());
                        buyDTO.setBuyPrice(quote.getCurrentPrice());
                        beforeAvgFenShi(limitQueue10,buyDTO);
                        //quoteBuyInfo(list,preEndPrice,buyDTO);
                        datas.add(buyDTO);
                        buyFlag = true;
                    }
                }

            }
            limitQueue10.offer(quote);
            if(buyFlag){
                limitQueue4.offer(quote);
                boolean continueDrop = haveContinueDrop(limitQueue4);
                if(continueDrop){
                    limitQueue4.clear();
                    buyFlag = false;
                    ZhuanZaiBuyDTO buy = datas.get(datas.size() - 1);
                    buy.setSellTime(quote.getQuoteTime());
                    buy.setSellPrice(quote.getCurrentPrice());
                    BigDecimal profit = PriceUtil.getPricePercentRate(quote.getCurrentPrice().subtract(buy.getBuyPrice()), preEndPrice);
                    buy.setProfit(profit);
                }
            }
        }
        if(datas.size()>0) {
            ZhuanZaiBuyDTO buy = datas.get(datas.size() - 1);
            if (buy.getProfit() == null) {
                buy.setSellTime(quoteLast.getQuoteTime());
                buy.setSellPrice(quoteLast.getCurrentPrice());
                BigDecimal profit = PriceUtil.getPricePercentRate(quoteLast.getCurrentPrice().subtract(buy.getBuyPrice()), preEndPrice);
                buy.setProfit(profit);
            }
        }
        return datas;
    }

    public BigDecimal  calRate(LimitQueue<ThsQuoteInfo> limitQueue,BigDecimal preEndPrice,ZhuanZaiBuyDTO buyDTO){
        if(limitQueue==null||limitQueue.size()<3){
            return null;
        }
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        ThsQuoteInfo lowQuoteInfo = null;
        ThsQuoteInfo lastQuoteInfo = null;
        int i = 0;
        while (iterator.hasNext()){
            i++;
            ThsQuoteInfo next = iterator.next();
            if(i<=2){
                if(lowQuoteInfo==null||next.getCurrentPrice().compareTo(lowQuoteInfo.getCurrentPrice())==-1) {
                    lowQuoteInfo = next;
                }
            }
            lastQuoteInfo = next;
        }
        buyDTO.setBeforeBuyTotalSell(lowQuoteInfo.getTotalSellVolume()/10);
        buyDTO.setBuyTotalSell(lastQuoteInfo.getTotalSellVolume()/10);
        buyDTO.setBeforeBuyFenShi(lowQuoteInfo.getVol()/10);
        buyDTO.setBuyFenShi(lastQuoteInfo.getVol()/10);
        BigDecimal rate = PriceUtil.getPricePercentRate(lastQuoteInfo.getCurrentPrice().subtract(lowQuoteInfo.getCurrentPrice()), preEndPrice);
        return rate;
    }
    public void  beforeAvgFenShi(LimitQueue<ThsQuoteInfo> limitQueue,ZhuanZaiBuyDTO buyDTO){
        if(limitQueue==null||limitQueue.size()<2){
            return;
        }
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        int i = 0;
        Long totalFenShi = 0L;
        while (iterator.hasNext()){
            ThsQuoteInfo next = iterator.next();
            Long vol = next.getVol();
            if(vol!=null) {
                i++;
                totalFenShi = totalFenShi + (vol/10);
            }
        }
        if(i>0){
            long avg = totalFenShi / i;
            buyDTO.setBeforeAvgFenShi(avg);
        }
    }

    public boolean  haveContinueDrop(LimitQueue<ThsQuoteInfo> limitQueue){
        if(limitQueue==null||limitQueue.size()<5){
            return false;
        }
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        ThsQuoteInfo preQuote = null;
        while (iterator.hasNext()){
            ThsQuoteInfo next = iterator.next();
            if(preQuote!=null && next.getCurrentPrice().compareTo(preQuote.getCurrentPrice())!=-1){
                return false;
            }
            preQuote = next;
        }
        return true;
    }

    public void quoteBuyInfo(List<ThsQuoteInfo> list,BigDecimal preEndPrice,ZhuanZaiBuyDTO buyDTO){
        ThsQuoteInfo preQuote = null;
        boolean flag = false;
        ThsQuoteInfo lastQuote = null;
        for (ThsQuoteInfo quote:list){
           if(quote.getCurrentPrice()==null||quote.getTotalSellVolume()==null||quote.getTotalSellVolume()==0){
               continue;
           }
           lastQuote = quote;
           if(flag){
               if(preQuote!=null){
                   BigDecimal percent = new BigDecimal(quote.getTotalSellVolume() - preQuote.getTotalSellVolume()).divide(new BigDecimal(preQuote.getTotalSellVolume()), 2, BigDecimal.ROUND_HALF_UP);
                   if(percent.compareTo(new BigDecimal(0.1))>0){
                       BigDecimal rate = PriceUtil.getPricePercentRate(quote.getCurrentPrice().subtract(buyDTO.getBuyPrice()), preEndPrice);
                       buyDTO.setProfit(rate);
                       buyDTO.setSellPrice(quote.getCurrentPrice());
                       buyDTO.setSellTime(quote.getQuoteTime());
                       return;
                   }
               }
               preQuote = quote;
           }
           if(quote.getQuoteTime().equals(buyDTO.getBuyTime())){
               flag = true;
           }
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(lastQuote.getCurrentPrice().subtract(buyDTO.getBuyPrice()), preEndPrice);
        buyDTO.setProfit(rate);
        buyDTO.setSellPrice(lastQuote.getCurrentPrice());
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
            String quoteTime = quote.getQuoteTime();
            if(quoteTime.equals("092500")){
                list.add(quote);
                continue;
            }
            if(quoteTime.startsWith("0")){
                quoteTime = quoteTime.substring(1);
            }
            Integer timeInt = Integer.valueOf(quoteTime);
            if(timeInt>=93000&&timeInt<=150006){
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


}
