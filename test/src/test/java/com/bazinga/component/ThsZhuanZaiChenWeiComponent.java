package com.bazinga.component;


import com.bazinga.base.Sort;
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
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.StockUtils;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
public class ThsZhuanZaiChenWeiComponent {
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
            list.add(dto.getGatherFenShi());
            list.add(dto.getRaiseVolFlag());
            list.add(dto.getIsGatherFlag());
            list.add(dto.getIsUpperAvg());
            list.add(dto.getSellType());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","市值","交易日期","买入价格","买入时间","买出价格","买出时间","集合成交量","成交是否递增","开盘是否新高","买入时是否在均价线上","卖出类型（1 下降 2 缩量）","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("转债陈威",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("转债陈威");
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
            /*if(!zhuanZai.getStockCode().equals("113619")){
                continue;
            }*/
            List<StockKbar> kbars = getKbars(zhuanZai.getStockCode(), zhuanZai.getStockName());
            for(TradeDatePool tradeDatePool:tradeDatePools){
                System.out.println(zhuanZai.getStockCode()+"======"+DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(zhuanZai.getStockCode().equals("113619")&&DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd).equals("20211101")){
                    System.out.println(11111111);
                }
                BigDecimal preEndPrice = getPreEndPrice(kbars, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(preEndPrice==null){
                    continue;
                }
                /*if(!(zhuanZai.getStockCode().equals("113619")&&DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd).equals("20211101"))){
                    continue;
                }*/
                List<ThsQuoteInfo> quotes = quotes(zhuanZai.getStockCode(), DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(CollectionUtils.isEmpty(quotes)){
                    continue;
                }
                List<ZhuanZaiBuyDTO> buyDTOS = quoteBuyInfo(quotes, preEndPrice, zhuanZai, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                if(!CollectionUtils.isEmpty(buyDTOS)){
                    datas.addAll(buyDTOS);
                }
            }
            /*if(datas.size()>=10){
                return datas;
            }*/
        }
        return datas;
    }

    public List<ZhuanZaiBuyDTO> quoteBuyInfo(List<ThsQuoteInfo> list,BigDecimal preEndPrice,ZhuanZaiExcelDTO excelDTO,String tradeDate){
        List<ZhuanZaiBuyDTO> datas = Lists.newArrayList();
        LimitQueue<ThsQuoteInfo> limitQueue = new LimitQueue<>(4);
        LimitQueue<ThsQuoteInfo> limitQueueSell = new LimitQueue<>(3);
        boolean buyFlag = false;
        int gatherHigh  = 0;
        ThsQuoteInfo quoteLast = null;
        Long gatherChenJiao =null;
        Date date930 = DateUtil.parseDate("093000", DateUtil.HHMMSS);
        int i=0;
        ThsQuoteInfo gather = null;
        for (ThsQuoteInfo quote:list){
            i++;
            if(StringUtils.isBlank(quote.getQuoteTime())){
                continue;
            }
            if(gather==null){
                gather = quote;
            }
            if(i==3){
                if(quote.getCurrentPrice().compareTo(gather.getCurrentPrice())>=0){
                    gatherHigh = 1;
                }
            }
            Date date = DateUtil.parseDate(quote.getQuoteTime(), DateUtil.HHMMSS);
            if(date.before(date930)&&(gatherChenJiao==null||gatherChenJiao<=0)){
                if(excelDTO.getStockCode().startsWith("12")) {
                    gatherChenJiao = quote.getVol() / 10;
                }else{
                    gatherChenJiao = quote.getVol();
                }
            }
            if(quote.getCurrentPrice()==null||quote.getTotalSellVolume()==null||quote.getTotalSellVolume()==0){
                continue;
            }
            quoteLast = quote;
            limitQueue.offer(quote);
            ZhuanZaiBuyDTO buyDTO = new ZhuanZaiBuyDTO();
            buyDTO.setStockCode(excelDTO.getStockCode());
            boolean haveRaise = calRate(limitQueue,buyDTO);
            if (haveRaise && !buyFlag) {
                buyDTO.setStockName(excelDTO.getStockName());
                buyDTO.setMarketAmount(excelDTO.getMarketAmount());
                buyDTO.setGatherFenShi(gatherChenJiao);
                buyDTO.setTradeDate(tradeDate);
                buyDTO.setBuyTime(quote.getQuoteTime());
                buyDTO.setBuyPrice(quote.getCurrentPrice().add(new BigDecimal("0.01")));
                buyDTO.setIsGatherFlag(gatherHigh);
                BigDecimal avgPrice = quote.getAmount().divide(new BigDecimal(quote.getVolume()), 2, BigDecimal.ROUND_HALF_UP);
                if(quote.getCurrentPrice().compareTo(avgPrice)==1){
                    buyDTO.setIsUpperAvg(1);
                }
                datas.add(buyDTO);
                buyFlag = true;
            }
            limitQueueSell.offer(quote);
            if(buyFlag){
                Integer continueDrop = haveContinueDrop(limitQueueSell,buyDTO);
                if(continueDrop!=null){
                    buyFlag = false;
                    ZhuanZaiBuyDTO buy = datas.get(datas.size() - 1);
                    buy.setSellTime(quote.getQuoteTime());
                    buy.setSellType(continueDrop);
                    buy.setSellPrice(quote.getCurrentPrice().add(new BigDecimal("-0.01")));
                    BigDecimal profit = PriceUtil.getPricePercentRate(quote.getCurrentPrice().subtract(buy.getBuyPrice()), preEndPrice);
                    buy.setProfit(profit);
                }
            }
        }
        if(datas.size()>0) {
            ZhuanZaiBuyDTO buy = datas.get(datas.size() - 1);
            if (buy.getProfit() == null) {
                buy.setSellTime(quoteLast.getQuoteTime());
                buy.setSellPrice(quoteLast.getCurrentPrice().add(new BigDecimal("-0.01")));
                BigDecimal profit = PriceUtil.getPricePercentRate(quoteLast.getCurrentPrice().subtract(buy.getBuyPrice()), preEndPrice);
                buy.setProfit(profit);
            }
        }
        return datas;
    }

    public boolean  calRate(LimitQueue<ThsQuoteInfo> limitQueue,ZhuanZaiBuyDTO buyDTO){
        if(limitQueue==null||limitQueue.size()<4){
            return false;
        }
        ThsQuoteInfo preThsQuoteInfo = null;
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        boolean isRaiseVol = true;
        int i = 0;
        while (iterator.hasNext()){
            i++;
            ThsQuoteInfo next = iterator.next();
            if(i<=3){
                if(buyDTO.getStockCode().startsWith("11")) {
                    if (next.getVol() < 100) {
                        return false;
                    }
                }else{
                    if (next.getVol() < 1000) {
                        return false;
                    }
                }
                if(i>1){
                    if(next.getVol()<=preThsQuoteInfo.getVol()){
                        isRaiseVol = false;
                    }
                    if(next.getCurrentPrice().compareTo(preThsQuoteInfo.getCurrentPrice())!=1){
                        return false;
                    }
                }
            }
            preThsQuoteInfo = next;
        }
        if(isRaiseVol) {
            buyDTO.setRaiseVolFlag(1);
        }
        return true;
    }


    public Integer  haveContinueDrop(LimitQueue<ThsQuoteInfo> limitQueue,ZhuanZaiBuyDTO buyDTO){
        if(limitQueue==null||limitQueue.size()<3){
            return null;
        }
        Iterator<ThsQuoteInfo> iterator = limitQueue.iterator();
        ThsQuoteInfo preQuote = null;
        int littleVolTime = 0;
        int i=0;
        while (iterator.hasNext()){
            i++;
            ThsQuoteInfo next = iterator.next();
            if(buyDTO.getStockCode().startsWith("11")) {
                if (next.getVol() < 10) {
                    littleVolTime++;
                }else{
                    littleVolTime = 0;
                }
            }else{
                if (next.getVol() < 100) {
                    littleVolTime++;
                }else{
                    littleVolTime = 0;
                }
            }
            if(littleVolTime>=2){
                return 2;
            }
            if(i>=2 && next.getCurrentPrice().compareTo(preQuote.getCurrentPrice())!=-1){
                return null;
            }
            preQuote = next;
        }
        return 1;
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
            if(quote.getBid1()==null&&quote.getAsk1()==null){
                continue;
            }
            String quoteTime = quote.getQuoteTime();
            /*if(quoteTime.equals("092500")){
                list.add(quote);
                continue;
            }*/
            if(quoteTime.startsWith("0")){
                quoteTime = quoteTime.substring(1);
            }
            Integer timeInt = Integer.valueOf(quoteTime);
            if(timeInt>93000&&timeInt<=150006){
                if(quote.getCurrentPrice()!=null){
                    list.add(quote);
                }
            }else{
                if(quote.getVol()>=1&&quote.getCurrentPrice()!=null){
                    list.add(quote);
                }
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