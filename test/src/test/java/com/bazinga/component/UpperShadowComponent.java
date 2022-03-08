package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.FastPlankDTO;
import com.bazinga.dto.HotBlockDropBuyDTO;
import com.bazinga.dto.ShadowKbarDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import javafx.scene.effect.Shadow;
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
public class UpperShadowComponent {
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


    public void upperShadowBuy(){
        List<ShadowKbarDTO> dailys = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(ShadowKbarDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketMoney());
            list.add(dto.getStockKbar().getKbarDate());

            list.add(dto.getRateDay5());
            list.add(dto.getRateDay10());
            list.add(dto.getRateDay15());
            list.add(dto.getShadowDayDealMoney());
            list.add(dto.getBuyDayOpenDealMoney());
            list.add(dto.getBuyBeforeDealMoney());
            list.add(dto.getBuyDayOPenRate());
            list.add(dto.getBuyTimeRate());
            list.add(dto.getShadowBefore10DealMoney());
            list.add(dto.getRateThanAvg5());
            list.add(dto.getShadowBefore30AvgQuantity());
            list.add(dto.getBuyRateThanHigh());
            list.add(dto.getShadowLength());
            list.add(dto.getBuySize());
            list.add(dto.getLevel());
            list.add(dto.isHaveHighSell());
            list.add(dto.isHaveBestSell());
            list.add(dto.getTwoPointFiveProfit());
            list.add(dto.getPlankTimes());
            list.add(dto.getOpenExchangeMoneyLevel());
            list.add(dto.getOpenExchangeMoneyRateLevel());
            list.add(dto.getPreDateEndPlanks());
            list.add(dto.getShadowTime());

            list.add(dto.getBuyPercent());
            list.add(dto.getProfit());
            list.add(dto.getMoneyProfit());
            list.add(dto.getAfterProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","市值","买入日期",
                "5日涨幅","10日涨幅","15日涨幅","上引线日成交金额","买入日开盘成交额","买入日买入前成交额","买入日开盘涨幅","买入时候涨幅",
                "上影线前10日平均成交额","买入时相对5日均线距离","上引线前30天平均成交量","上引线日收盘相对前30日最高点涨幅","上引线长度",
                "买入数量","排名","是否存在2.5卖出","出现2.5卖出后是否新高","使用2.5卖出法盈利","10天内封板次数","开盘成交额排名","开盘成交额除以前一天成交额比例排名","前一天封板数量","上引线日最高点时间","买入相对单笔比例","单笔盈利","买入比例盈利","买入两跳不降低盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("上引线买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("上引线买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<ShadowKbarDTO> getStockUpperShowInfo(){
        List<ShadowKbarDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, Date> startDateMap = kbarStartDate(circulateInfos);
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("20190101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        TradeDatePool preTradeDatePool = null;
        TradeDatePool prePreTradeDatePool = null;
        for(TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
            if(prePreTradeDatePool!=null) {
                List<ShadowKbarDTO> shadows = getStockKbarByDate(circulateInfos, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),
                        DateUtil.format(preTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),DateUtil.format(prePreTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd), startDateMap);
                List<ShadowKbarDTO> partList = Lists.newArrayList();
                if(shadows.size()>3){
                    int useSize = shadows.size() / 3;
                    ShadowKbarDTO.marketMoneySort(shadows);
                    shadows = shadows.subList(0,useSize);
                    partList.addAll(shadows);
                }
                List<ShadowKbarDTO> needLists = calShadowLength(partList, DateUtil.format(prePreTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                buyMoneyAndCalProfit(needLists);
                list.addAll(needLists);
            }
            prePreTradeDatePool = preTradeDatePool;
            preTradeDatePool = tradeDatePool;
        }
        return list;
    }
    public void buyMoneyAndCalProfit(List<ShadowKbarDTO> list){
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        BigDecimal buyPercent = new BigDecimal(1);
        if(list.size()<=100){
            buyPercent = new BigDecimal(100).divide(new BigDecimal(list.size()), 4, BigDecimal.ROUND_HALF_UP);
        }
        int i = 0;
        for (ShadowKbarDTO shadowKbarDTO:list){
            i++;
            /*if(shadowKbarDTO.getStockKbar().getKbarDate().equals("20220209")&&shadowKbarDTO.getStockCode().equals("600123")){
                System.out.println(12);
            }*/
            shadowKbarDTO.setBuyPercent(buyPercent);
            shadowKbarDTO.setBuySize(list.size());
            shadowKbarDTO.setLevel(i);
            calProfit(shadowKbarDTO);
            calShadowDayHighTime(shadowKbarDTO);
        }
        ShadowKbarDTO.buyDayOpenDealMoneySort(list);
        int j =0;
        for (ShadowKbarDTO shadowKbarDTO:list){
            j++;
            shadowKbarDTO.setOpenExchangeMoneyLevel(j);
        }
        ShadowKbarDTO.openExchangeMoneyRateSort(list);
        int k =0;
        for (ShadowKbarDTO shadowKbarDTO:list){
            k++;
            shadowKbarDTO.setOpenExchangeMoneyRateLevel(k);
        }
    }

    public void shadowLength(ShadowKbarDTO shadow){
        StockKbar preStockKbar = shadow.getPreStockKbar();
        StockKbar prePreStockKbar = shadow.getPrePreStockKbar();
        BigDecimal twoPrice = preStockKbar.getAdjClosePrice();
        if(preStockKbar.getAdjClosePrice().compareTo(preStockKbar.getAdjOpenPrice())==-1){
            twoPrice = preStockKbar.getAdjOpenPrice();
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(preStockKbar.getAdjHighPrice().subtract(twoPrice), prePreStockKbar.getAdjClosePrice());
        shadow.setShadowLength(rate);
    }
    public void calShadowDayHighTime(ShadowKbarDTO buyDTO){
        if(buyDTO.getPreStockKbar()==null){
            return;
        }
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(buyDTO.getStockCode(), buyDTO.getPreStockKbar().getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        String highTime = null;
        BigDecimal highPrice = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(highPrice==null||data.getTradePrice().compareTo(highPrice)==1){
                highPrice = data.getTradePrice();
                highTime = data.getTradeTime();
            }
        }
        buyDTO.setShadowTime(highTime);
    }
    public void calProfit(ShadowKbarDTO buyDTO){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(buyDTO.getStockCode());
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        boolean buyFlag = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(buyFlag){
                i++;
            }
            if(i==1){
                buyDTO.setNextStockKbar(stockKbar);
                break;
            }
            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                buyFlag  = true;
            }
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean reverseFlag = false;
        int days = 0;
        BigDecimal shadowBefore10DealMoney = BigDecimal.ZERO;
        int shadowBeforeDays = 0;
        Long shadowBefore30Quantity = 0L;
        int quantityDays = 0;
        BigDecimal highPrice = null;
        int planks = 0;
        StockKbar nextKbar = null;
        for (StockKbar stockKbar:reverse){
            if(reverseFlag){
                days++;
            }
            if(days==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPrePreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(days==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPrePreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(days==16){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPrePreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay15(rate);
            }
            if(days>1&&days<=11){
                shadowBeforeDays++;
                shadowBefore10DealMoney = shadowBefore10DealMoney.add(stockKbar.getTradeAmount());
                boolean isUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextKbar.getAdjHighPrice(), stockKbar.getAdjClosePrice(), nextKbar.getKbarDate());
                if(isUpper){
                    planks++;
                }
            }
            if(days>1&&days<=31){
                quantityDays++;
                shadowBefore30Quantity = shadowBefore30Quantity+stockKbar.getTradeQuantity();
                if(highPrice==null){
                    highPrice = stockKbar.getAdjHighPrice();
                }else{
                    if(stockKbar.getAdjHighPrice().compareTo(highPrice)==1){
                        highPrice = stockKbar.getAdjHighPrice();
                    }
                }
            }

            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                reverseFlag  = true;
            }
            nextKbar = stockKbar;
        }
        buyDTO.setPlankTimes(planks);
        if(shadowBeforeDays>0){
            BigDecimal avg = shadowBefore10DealMoney.divide(new BigDecimal(shadowBeforeDays), 2, BigDecimal.ROUND_HALF_UP);
            buyDTO.setShadowBefore10DealMoney(avg);
        }
        if(quantityDays>0){
            long avg = new BigDecimal(shadowBefore30Quantity).divide(new BigDecimal(quantityDays), 2, BigDecimal.ROUND_HALF_UP).longValue();
            buyDTO.setShadowBefore30AvgQuantity(avg);
        }
        if(highPrice!=null){
            BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreStockKbar().getAdjClosePrice().subtract(highPrice), highPrice);
            buyDTO.setBuyRateThanHigh(rate);
        }
        if(buyDTO.getNextStockKbar()==null){
            return;
        }
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(buyDTO.getStockCode(), buyDTO.getNextStockKbar().getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        BigDecimal totalMoney  = BigDecimal.ZERO;
        Integer totalCount = 0;
        boolean flag = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            String tradeTime = data.getTradeTime();
            if(flag){
                totalCount = totalCount+data.getTradeQuantity();
                BigDecimal money = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity())).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalMoney = totalMoney.add(money);
            }
            if(tradeTime.startsWith("13")){
                flag = false;
            }
        }
        BigDecimal avgTotal = BigDecimal.ZERO;
        int avgCount = 0;
        boolean highThanAvg = false;
        BigDecimal sellPrice = null;
        boolean bestSell = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            avgCount = avgCount+data.getTradeQuantity();
            BigDecimal money = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity())).setScale(2, BigDecimal.ROUND_HALF_UP);
            avgTotal = avgTotal.add(money);
            if(avgCount==0) {
                continue;
            }
            BigDecimal avgPrice = avgTotal.divide(new BigDecimal(avgCount), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal chuQuanSubPrice = chuQuanAvgPrice(data.getTradePrice().subtract(avgPrice), buyDTO.getNextStockKbar());
            BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanSubPrice, buyDTO.getStockKbar().getAdjClosePrice());
            if(rate.compareTo(new BigDecimal(2.5))==1){
                highThanAvg = true;
                if(sellPrice==null){
                    sellPrice = data.getTradePrice();
                    buyDTO.setHaveHighSell(true);
                }
            }
            if(highThanAvg){
                if(data.getTradePrice().compareTo(sellPrice)==1){
                    bestSell = true;
                    buyDTO.setHaveBestSell(true);
                }
            }
        }
        if(sellPrice==null){
            sellPrice  = buyDTO.getNextStockKbar().getAdjClosePrice();
            BigDecimal profit = PriceUtil.getPricePercentRate(sellPrice.subtract(buyDTO.getBuyPrice()), buyDTO.getBuyPrice());
            buyDTO.setTwoPointFiveProfit(profit);
        }else{
            BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(sellPrice, buyDTO.getNextStockKbar());
            BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyDTO.getBuyPrice()), buyDTO.getBuyPrice());
            buyDTO.setTwoPointFiveProfit(profit);
        }

        if(totalCount>0){
            BigDecimal avgPrice = totalMoney.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, buyDTO.getNextStockKbar());
            BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyDTO.getBuyPrice()), buyDTO.getBuyPrice());
            buyDTO.setProfit(profit);
            buyDTO.setMoneyProfit(profit.multiply(buyDTO.getBuyPercent()).setScale(2,BigDecimal.ROUND_HALF_UP));
            if(buyDTO.getAfterBuyPrice()!=null) {
                BigDecimal afterProfit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyDTO.getAfterBuyPrice()), buyDTO.getAfterBuyPrice());
                buyDTO.setAfterProfit(afterProfit);
            }
        }
    }

    public List<ShadowKbarDTO> calShadowLength(List<ShadowKbarDTO> shadows,String preTradeDate){
        List<ShadowKbarDTO> list = Lists.newArrayList();
        StockAverageLineQuery query = new StockAverageLineQuery();
        query.setKbarDate(preTradeDate);
        List<StockAverageLine> stockAverageLines = stockAverageLineService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockAverageLines)){
            return list;
        }
        Map<String, StockAverageLine> map = new HashMap<>();
        for (StockAverageLine stockAverageLine:stockAverageLines){
            map.put(stockAverageLine.getStockCode(),stockAverageLine);
        }
        for (ShadowKbarDTO shadow:shadows){
            StockKbar preStockKbar = shadow.getPreStockKbar();
            StockKbar prePreStockKbar = shadow.getPrePreStockKbar();
            BigDecimal upperShadow = (preStockKbar.getAdjHighPrice().subtract(preStockKbar.getAdjClosePrice())).divide(preStockKbar.getAdjClosePrice(), 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal upperRange = (preStockKbar.getAdjHighPrice().subtract(prePreStockKbar.getAdjClosePrice())).divide(prePreStockKbar.getAdjClosePrice(), 4, BigDecimal.ROUND_HALF_UP);
            if(upperShadow.compareTo(new BigDecimal(0.005))>0&&upperRange.compareTo(new BigDecimal(0.065))>0){
                StockAverageLine stockAverageLine = map.get(shadow.getStockCode());
                if(stockAverageLine!=null&&stockAverageLine.getAveragePrice()!=null) {
                    buyPriceRate(shadow);
                    if(shadow.getBuyPrice()!=null) {
                        BigDecimal rateThanAvg5 = (shadow.getBuyPrice().subtract(stockAverageLine.getAveragePrice())).divide(stockAverageLine.getAveragePrice(), 4, BigDecimal.ROUND_HALF_UP);
                        shadow.setRateThanAvg5(rateThanAvg5);
                        shadowLength(shadow);
                        list.add(shadow);
                    }
                }
            }
        }
        ShadowKbarDTO.RateThanAvgSort(list);
        System.out.println("拿取数量===="+list.size());
        if(list.size()>100){
            list= list.subList(0, 100);
        }
        List<ShadowKbarDTO> needs = Lists.newArrayList();
        for (ShadowKbarDTO dto:list){
            BigDecimal rate = PriceUtil.getPricePercentRate(dto.getBuyPrice().subtract(dto.getPreStockKbar().getAdjClosePrice()), dto.getPrePreStockKbar().getAdjClosePrice());
            Date chuangYeDate = DateUtil.parseDate("20190824", DateUtil.yyyyMMdd);
            Date buyDate = DateUtil.parseDate(dto.getStockKbar().getKbarDate(), DateUtil.yyyyMMdd);
            BigDecimal upperRate = new BigDecimal(9);
            BigDecimal downRate = new BigDecimal(-9);
            if(MarketUtil.isChuangYe(dto.getStockCode()) && !buyDate.before(chuangYeDate)){
                upperRate = new BigDecimal(18);
                downRate = new BigDecimal(-18);
            }
            if(rate.compareTo(upperRate)==-1&&rate.compareTo(downRate)==1) {
                needs.add(dto);
            }
        }
        return needs;
    }

    public void buyPriceRate(ShadowKbarDTO shadow){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(shadow.getStockCode(), shadow.getStockKbar().getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        Date buyTime = DateUtil.parseDate("09:33", DateUtil.HH_MM);
        BigDecimal tradePrice = null;
        BigDecimal gatherMoney = null;
        BigDecimal beforeBuyMoney = null;
        for (ThirdSecondTransactionDataDTO data:datas){
            Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
            BigDecimal tradeMoney = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            if(data.getTradeTime().equals("09:25")) {
                gatherMoney = tradeMoney;
            }
            if(beforeBuyMoney == null){
                beforeBuyMoney = tradeMoney;
            }else{
                beforeBuyMoney = beforeBuyMoney.add(tradeMoney);
            }
            tradePrice = data.getTradePrice();
            if(date.after(buyTime)){
                break;
            }
        }
        BigDecimal afterBuyPrice = null;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(3);
        for (ThirdSecondTransactionDataDTO data:datas){
            limitQueue.offer(data);
            boolean noDropFlag = noDrop(limitQueue, buyTime);
            if(noDropFlag){
                afterBuyPrice = data.getTradePrice();
                break;
            }
        }
        if(afterBuyPrice !=null){
            BigDecimal chuQuanAfterBuyPrice = chuQuanAvgPrice(afterBuyPrice, shadow.getStockKbar());
            shadow.setAfterBuyPrice(chuQuanAfterBuyPrice);
        }
        BigDecimal chuQuanTradePrice = chuQuanAvgPrice(tradePrice, shadow.getStockKbar());
        shadow.setBuyPrice(chuQuanTradePrice);
        shadow.setBuyDayOpenDealMoney(gatherMoney);
        if(gatherMoney!=null&&shadow.getPreStockKbar().getTradeAmount()!=null){
            BigDecimal openExchangeMoneyRate = gatherMoney.divide(shadow.getPreStockKbar().getTradeAmount(), 6, BigDecimal.ROUND_HALF_UP);
            shadow.setOpenExchangeMoneyRate(openExchangeMoneyRate);
        }
        shadow.setBuyBeforeDealMoney(beforeBuyMoney);
        BigDecimal openRate = PriceUtil.getPricePercentRate(shadow.getStockKbar().getAdjOpenPrice().subtract(shadow.getPreStockKbar().getAdjClosePrice()), shadow.getPreStockKbar().getAdjClosePrice());
        shadow.setBuyDayOPenRate(openRate);
        BigDecimal buyTimeRate = PriceUtil.getPricePercentRate(chuQuanTradePrice.subtract(shadow.getPreStockKbar().getAdjClosePrice()), shadow.getPreStockKbar().getAdjClosePrice());
        shadow.setBuyTimeRate(buyTimeRate);
        shadow.setShadowDayDealMoney(shadow.getPreStockKbar().getTradeAmount());
    }

    public boolean noDrop(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,Date buyTime){
        if(limitQueue.size()<3){
            return false;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        BigDecimal prePrice  = null;
        while (iterator.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator.next();
            Date date = DateUtil.parseDate(next.getTradeTime(), DateUtil.HH_MM);
            if(prePrice!=null){
                if(!date.after(buyTime)){
                    return false;
                }
                if(next.getTradePrice().compareTo(prePrice)==-1){
                    return false;
                }
            }
            prePrice  = next.getTradePrice();
        }
        return true;
    }

    public Map<String, Date> kbarStartDate(List<CirculateInfo> circulateInfos){
        Map<String, Date> map = new HashMap<>();
        /*if(true){
            return map;
        }*/
        int i = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println("开始日期"+circulateInfo.getStockCode()+"==="+i);
            List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKBars)){
                continue;
            }
            List<StockKbar> list = Lists.newArrayList();
            if(stockKBars.size()<=180){
                continue;
            }
            list = stockKBars.subList(180, stockKBars.size());
            Date date = DateUtil.parseDate(list.get(0).getKbarDate(), DateUtil.yyyyMMdd);
            map.put(circulateInfo.getStockCode(),date);
        }
        return map;
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

    public List<ShadowKbarDTO> getStockKbarByDate(List<CirculateInfo> circulateInfos,String dateStr,String preDateStr,String prePreDateStr,Map<String,Date> map){
        List<ShadowKbarDTO> list = new ArrayList<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(dateStr);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbarQuery preKbarQuery = new StockKbarQuery();
        preKbarQuery.setKbarDate(preDateStr);
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preKbarQuery);
        StockKbarQuery prePreKbarQuery = new StockKbarQuery();
        prePreKbarQuery.setKbarDate(prePreDateStr);
        List<StockKbar> prePreStockKbars = stockKbarService.listByCondition(prePreKbarQuery);

        Map<String, StockKbar> tradeDateMap = new HashMap<>();
        Map<String, StockKbar> preTradeDateMap = new HashMap<>();
        Map<String, StockKbar> prePreTradeDateMap = new HashMap<>();
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
        for (StockKbar stockKbar:prePreStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                prePreTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        int planks  = 0;
        for (StockKbar preStockKbar:preStockKbars){
            StockKbar prePreStockKbar = prePreTradeDateMap.get(preStockKbar.getStockCode());
            if(prePreStockKbar!=null){
                boolean isUpper = PriceUtil.isHistoryUpperPrice(preStockKbar.getStockCode(), preStockKbar.getClosePrice(), prePreStockKbar.getClosePrice(), preStockKbar.getKbarDate());
                if(isUpper){
                    planks++;
                }
            }
        }
        for(CirculateInfo circulateInfo:circulateInfos){
            Date date = map.get(circulateInfo.getStockCode());
            if(date==null||DateUtil.parseDate(dateStr,DateUtil.yyyyMMdd).before(date)){
                continue;
            }
            StockKbar stockKbar = tradeDateMap.get(circulateInfo.getStockCode());
            StockKbar preStockKbar = preTradeDateMap.get(circulateInfo.getStockCode());
            StockKbar prePreStockKbar = prePreTradeDateMap.get(circulateInfo.getStockCode());
            if(stockKbar!=null&&preStockKbar!=null&&prePreStockKbar!=null) {
                ShadowKbarDTO shadowKbarDTO = new ShadowKbarDTO();
                shadowKbarDTO.setStockCode(circulateInfo.getStockCode());
                shadowKbarDTO.setStockName(circulateInfo.getStockName());
                shadowKbarDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000),2,BigDecimal.ROUND_HALF_UP));
                shadowKbarDTO.setStockKbar(stockKbar);
                shadowKbarDTO.setPreStockKbar(preStockKbar);
                shadowKbarDTO.setPrePreStockKbar(prePreStockKbar);
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculate()).multiply(shadowKbarDTO.getPreStockKbar().getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
                shadowKbarDTO.setMarketMoney(marketMoney);
                shadowKbarDTO.setPreDateEndPlanks(planks);
                list.add(shadowKbarDTO);
            }
        }
        return list;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            //query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> list = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>=100){
                    list.add(stockKbar);
                }
            }
            return stockKbars;
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

    public static void main(String[] args) {
        ArrayList<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);

    }


}
