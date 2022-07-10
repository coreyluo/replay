package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.MacdBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class MacdCalComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private StockKbarService stockKbarService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void macdExcel(){
        List<MacdBuyDTO> buys = calMacdBuyThree();
        List<Object[]> datas = Lists.newArrayList();

        for (MacdBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getRedirect());
            Date date = DateUtil.parseDate(dto.getBuyTime(), DateUtil.yyyyMMddHHmmss);
            list.add(DateUtil.format(date,DateUtil.yyyyMMdd));
            list.add(DateUtil.format(date,DateUtil.HHmmss_DEFALT));
            list.add(dto.getBuyPrice());
            list.add(dto.getSellTime());
            list.add(dto.getSellPrice());
            list.add(dto.getPreClosePrice());
            list.add(dto.getBuyKbarAmount());
            list.add(dto.getPreKbarAmount());
            list.add(dto.getRaiseRateDay5());
            list.add(dto.getBar());
            list.add(dto.getPreBar());
            list.add(dto.getSellBar());
            list.add(dto.getHighBar());
            list.add(dto.getDropPercent());
            list.add(dto.getSellIntelTime());
            list.add(dto.getSellDropBuy());
            if(dto.getSellPrice()!=null) {
                if (dto.getRedirect() == 1) {
                    list.add(dto.getSellPrice().subtract(dto.getBuyPrice()));
                } else {
                    list.add(dto.getBuyPrice().subtract(dto.getSellPrice()));
                }
            }else{
                list.add(null);
            }

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易方向（0 空 1多）","交易日期","交易时间","买入价格","卖出时间","卖出价格","前一条k线收盘价","前一条k线成交额","当前k线成交额","5条k线涨幅","bar值","前一条bar值",
                "卖出bar","高点bar","下跌比例","卖出间隔买入天数","卖出类型（0下跌卖出，1跌破买入卖出，2收盘平仓）","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("macd买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("macd买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<MacdBuyDTO> calMacdBuyThree(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(100000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(6);
        LimitQueue<MacdIndexDTO> sellLimitQueue = new LimitQueue<>(1000);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            i++;
            limitQueue.offer(stockKbar);
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }
                if(haveBuy){
                    sellLimitQueue.offer(macdIndexDTO);
                    calSellInfo(sellLimitQueue,buys.get(buys.size()-1),stockKbar);
                }
                Integer redirect = null;
                if(macdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==1&&preMacdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==-1){
                    redirect = 1;
                }
                if(macdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==-1&&preMacdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==1){
                    redirect = 0;
                }
                if(redirect!=null){
                    if(!stockKbar.getKbarDate().endsWith("150000")) {
                        MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                        macdBuyDTO.setStockCode("ICZL");
                        macdBuyDTO.setStockCode("中证主连");
                        macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                        macdBuyDTO.setBar(macdIndexDTO.getBar());
                        macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                        macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                        macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                        macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                        macdBuyDTO.setRedirect(redirect);
                        BigDecimal raiseRate = getRaiseRate(limitQueue);
                        macdBuyDTO.setRaiseRateDay5(raiseRate);
                        buys.add(macdBuyDTO);
                        sellLimitQueue.clear();
                        sellLimitQueue.add(macdIndexDTO);
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        return buys;
    }

    public boolean calSellInfo(LimitQueue<MacdIndexDTO> limitQueue,MacdBuyDTO buyDTO,StockKbar stockKbar){
        Iterator<MacdIndexDTO> iterator = limitQueue.iterator();
        int i = 0;
        MacdIndexDTO buyMacd = null;
        MacdIndexDTO highMacd = null;
        MacdIndexDTO lowMacd = null;
        while (iterator.hasNext()){
            i++;
            MacdIndexDTO indexDTO = iterator.next();
            if(i==1){
                buyMacd = indexDTO;
            }
            if(buyDTO.getRedirect()==1) {
                if(highMacd==null||indexDTO.getBar().compareTo(highMacd.getBar())==1){
                    highMacd = indexDTO;
                }
                if (i == limitQueue.size()) {
                    BigDecimal absValue = highMacd.getBar().subtract(indexDTO.getBar());
                    BigDecimal percent = absValue.divide(highMacd.getBar(),3,BigDecimal.ROUND_HALF_UP);
                    boolean sellFlag = false;
                    if(absValue.compareTo(new BigDecimal("0.15"))==1&&percent.compareTo(new BigDecimal("0.15"))==1){
                        sellFlag = true;
                    }
                    if(sellFlag){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setHighBar(highMacd.getBar());
                        buyDTO.setDropPercent(percent);
                        buyDTO.setSellDropBuy(0);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if (indexDTO.getBar().compareTo(buyMacd.getBar())==-1){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(1);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(2);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                }
            }
            if(buyDTO.getRedirect()==0) {
                if(lowMacd==null||indexDTO.getBar().compareTo(lowMacd.getBar())==-1){
                    lowMacd = indexDTO;
                }
                if (i == limitQueue.size()) {
                    BigDecimal absValue = lowMacd.getBar().subtract(indexDTO.getBar());
                    BigDecimal percent = absValue.divide(lowMacd.getBar(),3,BigDecimal.ROUND_HALF_UP);
                    boolean sellFlag = false;
                    if(absValue.compareTo(new BigDecimal("-0.15"))==-1&&percent.compareTo(new BigDecimal("0.15"))==1){
                        sellFlag = true;
                    }
                    if(sellFlag){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setHighBar(lowMacd.getBar());
                        buyDTO.setDropPercent(percent);
                        buyDTO.setSellDropBuy(0);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if (indexDTO.getBar().compareTo(buyMacd.getBar())==1){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(1);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(2);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<MacdBuyDTO> calMacdBuyTwo(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(100000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(6);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            limitQueue.offer(stockKbar);
            if(stockKbar.getKbarDate().startsWith("20220425")){
                System.out.println(1);
            }
            i++;
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }

                if(haveBuy){
                    MacdBuyDTO macdBuyDTO = buys.get(buys.size() - 1);
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                        haveBuy = false;
                    }else {
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                            if (macdBuyDTO.getRedirect() == 0) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                            if (macdBuyDTO.getRedirect() == 1) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                    }
                }

                if(!haveBuy&&!(stockKbar.getKbarDate().endsWith("150000"))&&!(stockKbar.getKbarDate().endsWith("093000"))) {
                    MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                    macdBuyDTO.setStockCode("ICZL");
                    macdBuyDTO.setStockCode("中证主连");
                    macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                    macdBuyDTO.setBar(macdIndexDTO.getBar());
                    macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                    macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                    macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                    macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                    macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                        macdBuyDTO.setRedirect(1);
                    }
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                        macdBuyDTO.setRedirect(0);
                    }
                    BigDecimal raiseRate = getRaiseRate(limitQueue);
                    macdBuyDTO.setRaiseRateDay5(raiseRate);
                    buys.add(macdBuyDTO);
                }

            }
            preStockKbar = stockKbar;
        }
        return buys;
    }

    public BigDecimal getRaiseRate(LimitQueue<StockKbar> limitQueue){
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar first = null;
        StockKbar last = null;
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            if(first==null){
                first = next;
            }
            last = next;
        }
        BigDecimal pricePercentRate = PriceUtil.getPricePercentRate(last.getClosePrice().subtract(first.getClosePrice()), first.getClosePrice());
        return pricePercentRate;
    }


    public List<MacdBuyDTO> calMacdBuy(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(30000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }

                if(haveBuy){
                    MacdBuyDTO macdBuyDTO = buys.get(buys.size() - 1);
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                        haveBuy = false;
                    }else {
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                            if (macdBuyDTO.getRedirect() == 0) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                            if (macdBuyDTO.getRedirect() == 1) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                    }
                }

                if(!haveBuy&&!(stockKbar.getKbarDate().endsWith("150000"))&&!(stockKbar.getKbarDate().endsWith("093020"))) {
                    MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                    macdBuyDTO.setStockCode("ICZL");
                    macdBuyDTO.setStockCode("中证主连");
                    macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                    macdBuyDTO.setBar(macdIndexDTO.getBar());
                    macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                    macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                    macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                    macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                    macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                        macdBuyDTO.setRedirect(1);
                    }
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                        macdBuyDTO.setRedirect(0);
                    }
                    buys.add(macdBuyDTO);
                }

            }
            preStockKbar = stockKbar;
        }
        return buys;
    }

    public MacdIndexDTO macdIndex(MacdIndexDTO preMacdIndexDTO,StockKbar stockKbar,StockKbar preStockKbar,int i){
        if(i==1){
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setDea(BigDecimal.ZERO);
            macdIndexDTO.setDiff(BigDecimal.ZERO);
            macdIndexDTO.setBar(BigDecimal.ZERO);
            return macdIndexDTO;
        } else if(i==2){
            BigDecimal ema12 = (
                    (preStockKbar.getClosePrice().multiply(new BigDecimal(11))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).
                    divide(new BigDecimal(13),2,BigDecimal.ROUND_HALF_UP);
            BigDecimal ema26 = ((preStockKbar.getClosePrice().multiply(new BigDecimal(25))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(27),2,BigDecimal.ROUND_HALF_UP);
            BigDecimal diff = ema12.subtract(ema26).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal dea = diff.multiply(new BigDecimal("0.2")).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal bar = (diff.subtract(dea)).multiply(new BigDecimal(2)).setScale(2,BigDecimal.ROUND_HALF_UP);
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setEma12(ema12);
            macdIndexDTO.setEma26(ema26);
            macdIndexDTO.setDiff(diff);
            macdIndexDTO.setDea(dea);
            macdIndexDTO.setBar(bar);
            return macdIndexDTO;
        }else{
            BigDecimal ema12 = ((preMacdIndexDTO.getEma12().multiply(new BigDecimal(11))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(13),2,BigDecimal.ROUND_HALF_UP);
            BigDecimal ema26 = ((preMacdIndexDTO.getEma26().multiply(new BigDecimal(25))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(27),2,BigDecimal.ROUND_HALF_UP);
            BigDecimal diff = ema12.subtract(ema26).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal dea = ((preMacdIndexDTO.getDea().multiply(new BigDecimal(8))).add(diff.multiply(new BigDecimal(2)))).divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal bar = (diff.subtract(dea)).multiply(new BigDecimal(2)).setScale(2,BigDecimal.ROUND_HALF_UP);
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setEma12(ema12);
            macdIndexDTO.setEma26(ema26);
            macdIndexDTO.setDiff(diff);
            macdIndexDTO.setDea(dea);
            macdIndexDTO.setBar(bar);
            return  macdIndexDTO;
        }
    }

    /**
     * 上证macd diff dea
     */
    public void quoteToKbar(){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        boolean flag = false;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            /*if(!yyyyMMdd.equals("20220526")){
                continue;
            }*/
            if(yyyyMMdd.equals("20210813")){
                flag = true;
            }
            if(flag) {
                ThsQuoteInfoQuery thsQuoteInfoQuery = new ThsQuoteInfoQuery();
                thsQuoteInfoQuery.setQuoteDate(yyyyMMdd);
                thsQuoteInfoQuery.setStockCode("ICZL");
                thsQuoteInfoQuery.setQuoteDate(yyyyMMdd);
                thsQuoteInfoQuery.addOrderBy("quote_time", Sort.SortType.ASC);
                thsQuoteInfoQuery.setLimit(40000);
                List<ThsQuoteInfo> thsQuoteInfos = thsQuoteInfoService.listByCondition(thsQuoteInfoQuery);
                if (CollectionUtils.isEmpty(thsQuoteInfos)) {
                    continue;
                }
                for (ThsQuoteInfo thsQuoteInfo:thsQuoteInfos){
                    String quoteTime = thsQuoteInfo.getQuoteTime();
                    String substring = quoteTime.substring(0, 6);
                    thsQuoteInfo.setQuoteTime(substring);
                }
                Map<String, List<ThsQuoteInfo>> stringListMap = calSecondKbar(thsQuoteInfos, 20, yyyyMMdd);
                System.out.println(yyyyMMdd);
            }
        }
    }

    public Map<String,List<ThsQuoteInfo>> calSecondKbar(List<ThsQuoteInfo> quotes,int seconds,String tradeDate){
        Map<String,List<ThsQuoteInfo>> map = new HashMap<>();
        List<Date> kbarSeconds = getKbarSeconds(seconds);
        Date preDatePoint = DateUtil.parseDate("092859",DateUtil.HHmmss);
        BigDecimal preEnd = quotes.get(0).getCurrentPrice();
        for (Date datePoint:kbarSeconds){
            String datePointStr = DateUtil.format(datePoint, DateUtil.HHmmss);
            if(datePointStr.equals("113000")||datePointStr.equals("150000")){
                datePoint = DateUtil.addSeconds(datePoint,1);
            }
            List<ThsQuoteInfo> kbarQuotes = Lists.newArrayList();
            BigDecimal start = null;
            BigDecimal end = null;
            BigDecimal high = null;
            BigDecimal low = null;
            long quantity = 0l;
            BigDecimal amount = BigDecimal.ZERO;
            for (ThsQuoteInfo quoteInfo:quotes){
                if(quoteInfo.getQuoteTime().equals("092500")){
                    continue;
                }
                Date quoteTime = DateUtil.parseDate(quoteInfo.getQuoteTime(), DateUtil.HHmmss);

                if(quoteTime.before(datePoint)&&!quoteTime.before(preDatePoint)){
                    if(high==null||quoteInfo.getCurrentPrice().compareTo(high)==1){
                        high = quoteInfo.getCurrentPrice();
                    }
                    if(low==null||quoteInfo.getCurrentPrice().compareTo(low)==-1){
                        low = quoteInfo.getCurrentPrice();
                    }
                    if(start==null){
                        start = quoteInfo.getCurrentPrice();
                    }
                    end = quoteInfo.getCurrentPrice();
                    if(quoteInfo.getVol()!=null) {
                        if(quoteInfo.getVol()<0){
                            quantity =quantity- quoteInfo.getVol();
                        }else {
                            quantity = quoteInfo.getVol() + quantity;
                        }
                    }
                    if(quoteInfo.getAmt()!=null) {
                        if(quoteInfo.getAmt().compareTo(BigDecimal.ZERO)==-1){
                            amount = amount.subtract(quoteInfo.getAmt());
                        }else {
                            amount = amount.add(quoteInfo.getAmt());
                        }
                    }
                    kbarQuotes.add(quoteInfo);
                }
                if(!quoteTime.before(datePoint)||(quoteInfo.getQuoteTime().equals("150000")&&datePointStr.equals("150000"))){
                    StockKbar stockKbar = new StockKbar();
                    stockKbar.setStockCode("ICZL");
                    stockKbar.setStockName("中证500期货");
                    stockKbar.setKbarDate(tradeDate+datePointStr);
                    stockKbar.setUniqueKey(stockKbar.getStockCode()+"_"+stockKbar.getKbarDate());
                    if(quotes.size()<=0){
                        stockKbar.setOpenPrice(preEnd);
                        stockKbar.setClosePrice(preEnd);
                        stockKbar.setHighPrice(preEnd);
                        stockKbar.setLowPrice(preEnd);
                    }else {
                        stockKbar.setOpenPrice(start);
                        stockKbar.setClosePrice(end);
                        stockKbar.setHighPrice(high);
                        stockKbar.setLowPrice(low);
                    }
                    stockKbar.setTradeQuantity(quantity);
                    stockKbar.setTradeAmount(amount);
                    stockKbar.setCreateTime(new Date());
                    stockKbarService.save(stockKbar);
                    preEnd = stockKbar.getClosePrice();
                    map.put(datePointStr,kbarQuotes);
                    break;
                }
            }
            preDatePoint = datePoint;
        }
        return map;
    }


    public void saveGatherKbar(List<ThsQuoteInfo> quotes){
        ThsQuoteInfo thsQuoteInfo = quotes.get(0);
        if(thsQuoteInfo.getQuoteTime().equals("092500")){
            StockKbar stockKbar = new StockKbar();
            stockKbar.setStockCode("127046");
            stockKbar.setStockName("中证500期货");
            stockKbar.setKbarDate("20211110"+"093000");
            stockKbar.setUniqueKey(stockKbar.getStockCode()+"_"+stockKbar.getKbarDate());
            stockKbar.setOpenPrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setClosePrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setHighPrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setLowPrice(thsQuoteInfo.getCurrentPrice());

            stockKbar.setTradeQuantity(thsQuoteInfo.getVol());
            stockKbar.setTradeAmount(thsQuoteInfo.getAmt());
            stockKbar.setCreateTime(new Date());
            stockKbarService.save(stockKbar);
        }
    }

    public static Date timeAddSecond(String start,int seconds){
        Date startDate = DateUtil.parseDate(start, DateUtil.HHmmss);
        Date time113000 = DateUtil.parseDate("113000", DateUtil.HHmmss);
        Date time130000 = DateUtil.parseDate("130000", DateUtil.HHmmss);
        for (int i = 1;i<=seconds;i++){
            String startDateStr = DateUtil.format(startDate, DateUtil.HHmmss);
            if(startDateStr.equals("113000")){
                startDate = time130000;
            }
            startDate = DateUtil.addSeconds(startDate, 1);
        }
        return startDate;
    }

    public List<Date> getKbarSeconds(int seconds) {
        List<Date> list = Lists.newArrayList();
        Date date = DateUtil.parseDate("093000", DateUtil.HHmmss);
        for (int i=0;i<=1000;i++) {
            String timeStamp = DateUtil.format(date, DateUtil.HHmmss);
            date = timeAddSecond(timeStamp, seconds);
            String format = DateUtil.format(date, DateUtil.HHmmss);
            if(date.after(DateUtil.parseDate("150000", DateUtil.HHmmss))){
                break;
            }
            if(format.equals("113000")){
                date = DateUtil.parseDate("130000",DateUtil.HHmmss);
            }
            Date datePoint = date;
            list.add(datePoint);

        }
        return list;
    }

}
