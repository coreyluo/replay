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
            list.add(dto.getBuyPercent());
            list.add(dto.getProfit());
            list.add(dto.getMoneyProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","市值","买入日期","买入相对单笔比例","单笔盈利","买入比例盈利"};
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
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
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
        if(list.size()<=50){
            buyPercent = new BigDecimal(50).divide(new BigDecimal(list.size()), 4, BigDecimal.ROUND_HALF_UP);
        }
        for (ShadowKbarDTO shadowKbarDTO:list){
            /*if(shadowKbarDTO.getStockKbar().getKbarDate().equals("20220209")&&shadowKbarDTO.getStockCode().equals("600123")){
                System.out.println(12);
            }*/
            shadowKbarDTO.setBuyPercent(buyPercent);
            shadowKbarDTO.setBuyPrice(shadowKbarDTO.getStockKbar().getAdjOpenPrice());
            calProfit(shadowKbarDTO);
        }
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

        if(totalCount>0){
            BigDecimal avgPrice = totalMoney.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, buyDTO.getNextStockKbar());
            BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyDTO.getStockKbar().getAdjOpenPrice()), buyDTO.getStockKbar().getAdjClosePrice());
            buyDTO.setProfit(profit);
            buyDTO.setMoneyProfit(profit.multiply(buyDTO.getBuyPercent()).setScale(2,BigDecimal.ROUND_HALF_UP));
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
                    BigDecimal rateThanAvg5 = (shadow.getStockKbar().getAdjOpenPrice().subtract(stockAverageLine.getAveragePrice())).divide(stockAverageLine.getAveragePrice(), 4, BigDecimal.ROUND_HALF_UP);
                    shadow.setRateThanAvg5(rateThanAvg5);
                    list.add(shadow);
                }
            }
        }
        ShadowKbarDTO.RateThanAvgSort(list);
        if(list.size()>50){
            list= list.subList(0, 50);
        }
        List<ShadowKbarDTO> needs = Lists.newArrayList();
        for (ShadowKbarDTO dto:list){
            BigDecimal rate = PriceUtil.getPricePercentRate(dto.getStockKbar().getAdjOpenPrice().subtract(dto.getPreStockKbar().getAdjClosePrice()), dto.getPrePreStockKbar().getAdjClosePrice());
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
