package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
public class FastPlankComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void fastPlank(){
        List<FastPlankDTO> dailys = Lists.newArrayList();

        List<Object[]> datas = Lists.newArrayList();
        for(FastPlankDTO dto:dailys){
            BigDecimal openRate = PriceUtil.getPricePercentRate(dto.getBuyKbar().getAdjOpenPrice().subtract(dto.getPreKbar().getAdjClosePrice()), dto.getPreKbar().getAdjClosePrice());
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketMoney());
            list.add(dto.getBuyKbar().getKbarDate());
            list.add(openRate);
            list.add(dto.getPlankTimeStr());
            list.add(dto.getBuyKbar().getHighPrice());
            list.add(dto.getBeforeDay3());
            list.add(dto.getBeforeDay5());
            list.add(dto.getBeforeDay10());
            list.add(dto.getBeforeDay30());
            list.add(dto.getRateBeforeDay255());
            list.add(dto.getRateBeforeEnd());
            list.add(dto.getRateBefore255ToEnd());
            list.add(dto.getRateBeforeLower230ToEnd());
            list.add(dto.getPlanksDay10());
            list.add(dto.getLowerPriceDay10());
            list.add(dto.getRateBeforeAvgRate());
            list.add(dto.getAvgExchangeDay10());
            list.add(dto.getAvgExchangeDay11To50());
            list.add(dto.getPlankPriceDivideLowerDay10());
            list.add(dto.getAvgExchangePercentDay10());
            list.add(dto.getExchangeDay10DivideDay50());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","流通市值","买入日期","开盘涨幅","第一次能买入时间","买入价格",
                "3日涨幅","5日涨幅","10日涨幅", "30日涨幅","前一日255涨幅","前一日收盘涨幅","前一日收盘-255","前一日230至收盘最低点涨幅",
                "前10日板数","前10日最低价格","前一日均价涨幅","前10日平均成交量","前11日-50日平均成交量","板价/10日最低","前10日平均换手率","前10日平均成交量/前50日平均成交量","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("早上板股票",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("早上板股票");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<FastPlankDTO> getFastPlank(CirculateInfo circulateInfo){
        List<FastPlankDTO> list = Lists.newArrayList();
        List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode(),250);
        if(CollectionUtils.isEmpty(stockKBars)){
            return list;
        }
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(4);
        StockKbar preStockKbar = null;
        StockKbar prePreStockKbar = null;
        for (StockKbar stockKbar:stockKBars){
            limitQueue.offer(stockKbar);
            boolean firstPlank = isFirstPlank(limitQueue);
            if(firstPlank){
                List<ThirdSecondTransactionDataDTO> transactions = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                if(!CollectionUtils.isEmpty(transactions)){
                    String buyTime = getFirstPlankTime(circulateInfo.getStockCode(), preStockKbar.getClosePrice(), transactions);
                    if(buyTime!=null){
                        BigDecimal marketMoney = stockKbar.getHighPrice().multiply(new BigDecimal(circulateInfo.getCirculate())).divide(new BigDecimal("100000000"), 2, BigDecimal.ROUND_HALF_UP);
                        FastPlankDTO fastPlankDTO = new FastPlankDTO();
                        fastPlankDTO.setStockCode(circulateInfo.getStockCode());
                        fastPlankDTO.setStockName(circulateInfo.getStockName());
                        fastPlankDTO.setCirculateZ(circulateInfo.getCirculateZ());
                        fastPlankDTO.setMarketMoney(marketMoney);
                        fastPlankDTO.setBuyKbar(stockKbar);
                        fastPlankDTO.setPreKbar(preStockKbar);
                        fastPlankDTO.setPrePreKbar(prePreStockKbar);
                        fastPlankDTO.setPlankTimeStr(buyTime);
                        beforeRateAndProfit(fastPlankDTO,stockKBars);
                        beforeDateInfo(fastPlankDTO);



                    }
                }
            }
            prePreStockKbar = preStockKbar;
            preStockKbar = stockKbar;
        }
        return null;
    }
    public void beforeExchangeInfo(FastPlankDTO buyDTO, List<StockKbar> bars){
        List<StockKbar> reverse = Lists.reverse(bars);
        int planks = 0;
        BigDecimal lowestPriceDay10 = null;

        StockKbar nextKbar = null;
        boolean flagPlank = false;
        int j = 0;
        for (StockKbar bar:reverse){
            if(flagPlank){
                j++;
            }
            if(bar.getKbarDate().equals(buyDTO.getBuyKbar().getKbarDate())){
                flagPlank = true;
            }
            if(j>=2&&j<=11){
                boolean upperPrice = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getClosePrice(), bar.getClosePrice());
                if(upperPrice){
                    planks++;
                }
            }
            if(j>=1&&j<=10){
                if(lowestPriceDay10==null||bar.getAdjLowPrice().compareTo(lowestPriceDay10)==-1){
                    lowestPriceDay10 = bar.getAdjLowPrice();
                }
            }
            nextKbar = bar;
        }
        buyDTO.setPlanksDay10(planks);
        buyDTO.setLowerPriceDay10(lowestPriceDay10);
    }


    public void beforeDateInfo(FastPlankDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> transactions = historyTransactionDataComponent.getData(buyDTO.getStockCode(), DateUtil.parseDate(buyDTO.getPreKbar().getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(transactions)){
            return ;
        }
        if(buyDTO.getPrePreKbar()==null){
            return;
        }
        BigDecimal preEndRate = PriceUtil.getPricePercentRate(buyDTO.getPreKbar().getAdjClosePrice().subtract(buyDTO.getPrePreKbar().getAdjClosePrice()), buyDTO.getPrePreKbar().getAdjClosePrice());
        buyDTO.setRateBeforeEnd(preEndRate);
        BigDecimal lowestPrice = null;
        for (ThirdSecondTransactionDataDTO transaction:transactions){
            String tradeTime = transaction.getTradeTime();
            BigDecimal tradePrice = transaction.getTradePrice();
            if(buyDTO.getRateBeforeDay255()==null && !DateUtil.parseDate(tradeTime,DateUtil.HH_MM).before(DateUtil.parseDate("14:55",DateUtil.HH_MM))){
                BigDecimal chuQuanPrice = chuQuanAvgPrice(tradePrice, buyDTO.getPreKbar());
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanPrice.subtract(buyDTO.getPrePreKbar().getAdjClosePrice()), buyDTO.getPrePreKbar().getAdjClosePrice());
                buyDTO.setRateBeforeDay255(rate);
                buyDTO.setRateBefore255ToEnd(buyDTO.getRateBeforeEnd().subtract(rate));
            }
            if(DateUtil.parseDate(tradeTime,DateUtil.HH_MM).before(DateUtil.parseDate("14:30",DateUtil.HH_MM))) {
                if (lowestPrice == null || tradePrice.compareTo(lowestPrice) == -1) {
                    lowestPrice = tradePrice;
                }
            }
        }
        if(lowestPrice!=null){
            BigDecimal chuQuanPrice = chuQuanAvgPrice(lowestPrice, buyDTO.getPreKbar());
            BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanPrice.subtract(buyDTO.getPrePreKbar().getAdjClosePrice()), buyDTO.getPrePreKbar().getAdjClosePrice());
            buyDTO.setRateBeforeLower230ToEnd(rate);
        }
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(buyDTO.getStockCode(), DateUtil.parseDate(buyDTO.getPreKbar().getKbarDate(), DateUtil.yyyyMMdd));
        if(avgPrice!=null) {
            BigDecimal chuQuanPrice = chuQuanAvgPrice(avgPrice, buyDTO.getPreKbar());
            BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanPrice.subtract(buyDTO.getPrePreKbar().getAdjClosePrice()), buyDTO.getPrePreKbar().getAdjClosePrice());
            buyDTO.setRateBeforeAvgRate(rate);
        }

    }


    public String getFirstPlankTime(String stockCode,BigDecimal preEndPrice,List<ThirdSecondTransactionDataDTO> transactions){
        boolean openFlag = false;
        for (ThirdSecondTransactionDataDTO transaction:transactions){
            String tradeTime = transaction.getTradeTime();
            BigDecimal tradePrice = transaction.getTradePrice();
            Integer tradeType = transaction.getTradeType();
            boolean upperPrice = PriceUtil.isUpperPrice(stockCode, tradePrice, preEndPrice);
            if(tradeTime.equals("09:25")){
                if(!upperPrice) {
                    openFlag = true;
                }
                continue;
            }
            if(!(upperPrice&&tradeType==1)){
                openFlag = true;
            }
            if(openFlag&&upperPrice&&tradeType==1){
                return tradeTime;
            }
        }
        return null;
    }

    public boolean isFirstPlank(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<2){
            return false;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        int i = 0;
        StockKbar nextKbar = null;
        for (StockKbar kbar:reverse){
            boolean highUpper = PriceUtil.isUpperPrice(kbar.getStockCode(), nextKbar.getHighPrice(), kbar.getClosePrice());
            boolean endUpper = PriceUtil.isUpperPrice(kbar.getStockCode(), nextKbar.getClosePrice(), kbar.getClosePrice());
            if(i==1){
                if(!highUpper){
                    return false;
                }
            }
            if(i==2||i==3){
                if(endUpper){
                    return false;
                }
            }
            nextKbar = kbar;
            i++;
        }
        return true;
    }

    public void beforeRateAndProfit(FastPlankDTO buyDTO, List<StockKbar> bars){
        StockKbar preKbar = null;
        int i = 0;
        boolean flag = false;
        for (StockKbar bar:bars){
            if(flag){
                i++;
            }
            if(preKbar!=null){
                if(bar.getKbarDate().equals(buyDTO.getBuyKbar().getKbarDate())){
                    flag = true;
                }
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(bar.getStockCode(), DateUtil.parseDate(bar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, bar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(preKbar.getAdjHighPrice()), preKbar.getAdjHighPrice());
                buyDTO.setProfit(profit);
                break;
            }
            preKbar  = bar;
        }
        List<StockKbar> reverse = Lists.reverse(bars);
        boolean flagPlank = false;
        int j = 0;
        for (StockKbar bar:reverse){
            if(flagPlank){
                j++;
            }
            if(bar.getKbarDate().equals(buyDTO.getBuyKbar().getKbarDate())){
                flagPlank = true;
            }
            if(j==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreKbar().getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay3(rate);
            }
            if(j==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreKbar().getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay5(rate);
            }
            if(j==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreKbar().getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay10(rate);
            }
            if(j==31){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreKbar().getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay30(rate);
                break;
            }
        }
    }

    public String getPlankTime(String stockCode,String tradeDate,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData(stockCode, DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(data)){
            return null;
        }
        for (ThirdSecondTransactionDataDTO dto:data){
            boolean upperPrice = PriceUtil.isUpperPrice(stockCode, dto.getTradePrice(), preEndPrice);
            if(upperPrice&&dto.getTradeType()==1){
                return dto.getTradeTime();
            }
        }
        return null;
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

    public List<StockKbar> getStockKBars(String stockCode,int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            List<StockKbar> list = deleteNewStockTimes(reverse, size);
            return list;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
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
