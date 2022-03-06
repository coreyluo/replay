package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.ShadowKbarDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.security.provider.SHA;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ChungYeBugComponent {
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


    public void chuangYeBuy(){
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
            list.add(dto.getOpenRate());
            list.add(dto.getOpenAmount());
            list.add(dto.getOpenAmountRate());
            //list.add(dto.getOpenAmountRateLevel());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","市值","买入日期",
                "5日涨幅","10日涨幅","15日涨幅","开盘涨幅","开盘涨幅","开盘成交额相对昨天比例","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("创业板价格笼子",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("创业板价格笼子");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<ShadowKbarDTO> getStockUpperShowInfo(){
        List<ShadowKbarDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        TradeDatePool preTradeDatePool = null;
        for(TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
            if(preTradeDatePool!=null) {
                List<ShadowKbarDTO> shadows = getStockKbarByDate(circulateInfos, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd), DateUtil.format(preTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd));
                List<ShadowKbarDTO> realShadows = realShadows(shadows);
                for (ShadowKbarDTO shadow:realShadows){
                    calProfit(shadow);
                    if(!shadow.isNewFlag()){
                        list.add(shadow);
                    }
                }
            }
            preTradeDatePool = tradeDatePool;
        }
        return list;
    }
    public List<ShadowKbarDTO> realShadows(List<ShadowKbarDTO> list){
        if(CollectionUtils.isEmpty(list)){
            return list;
        }
        List<ShadowKbarDTO> shadows = Lists.newArrayList();
        for (ShadowKbarDTO shadow:list){
            if(shadow.isBuyFlag()&&shadow.getOpenRate().compareTo(new BigDecimal("18"))==-1){
                shadows.add(shadow);
            }
        }
        return shadows;
    }

    public void calProfit(ShadowKbarDTO buyDTO){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(buyDTO.getStockCode());
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        boolean buyFlag = false;
        int i = 0;
        int newTime = 0;
        for (StockKbar stockKbar:stockKbars){
            newTime++;
            if(buyFlag){
                i++;
            }
            if(i==1){
                buyDTO.setNextStockKbar(stockKbar);
                break;
            }
            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                buyFlag  = true;
                if(newTime<=5){
                    buyDTO.setNewFlag(true);
                    return;
                }
            }
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean reverseFlag = false;
        int days = 0;
        for (StockKbar stockKbar:reverse){
            if(reverseFlag){
                days++;
            }
            if(days==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(days==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(days==16){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyDTO.getPreStockKbar().getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay15(rate);
            }
            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                reverseFlag  = true;
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
            if(buyDTO.getBuyPrice()!=null&&chuQuanAvgPrice!=null) {
                BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyDTO.getBuyPrice()), buyDTO.getBuyPrice());
                buyDTO.setProfit(profit);
            }
        }
    }


    public void openInfo(ShadowKbarDTO shadow){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(shadow.getStockCode(), shadow.getStockKbar().getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        int i = 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(!data.getTradeTime().equals("09:25")){
                i++;
            }
            BigDecimal tradeMoney = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            if(data.getTradeTime().equals("09:25")) {
                shadow.setOpenAmount(tradeMoney);
                BigDecimal divide = tradeMoney.divide(shadow.getPreStockKbar().getTradeAmount(), 6, BigDecimal.ROUND_HALF_UP);
                shadow.setOpenAmountRate(divide);
            }
            if(i>0&&i<=10){
               if(data.getTradePrice().compareTo(shadow.getStockKbar().getOpenPrice())!=-1){
                    shadow.setBuyFlag(true);
                    break;
               }
            }
            if(i>10){
                break;
            }
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

    public List<ShadowKbarDTO> getStockKbarByDate(List<CirculateInfo> circulateInfos,String dateStr,String preDateStr){
        List<ShadowKbarDTO> list = new ArrayList<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(dateStr);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbarQuery preKbarQuery = new StockKbarQuery();
        preKbarQuery.setKbarDate(preDateStr);
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preKbarQuery);

        Map<String, StockKbar> tradeDateMap = new HashMap<>();
        Map<String, StockKbar> preTradeDateMap = new HashMap<>();
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
        int index = 0;
        for(CirculateInfo circulateInfo:circulateInfos){
            if(!MarketUtil.isChuangYe(circulateInfo.getStockCode())){
                continue;
            }
            /*index++;
            if(index>=30){
                continue;
            }*/
            StockKbar stockKbar = tradeDateMap.get(circulateInfo.getStockCode());
            StockKbar preStockKbar = preTradeDateMap.get(circulateInfo.getStockCode());
            if(stockKbar!=null&&preStockKbar!=null) {
                ShadowKbarDTO shadowKbarDTO = new ShadowKbarDTO();
                shadowKbarDTO.setStockCode(circulateInfo.getStockCode());
                shadowKbarDTO.setStockName(circulateInfo.getStockName());
                shadowKbarDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000),2,BigDecimal.ROUND_HALF_UP));
                shadowKbarDTO.setStockKbar(stockKbar);
                shadowKbarDTO.setPreStockKbar(preStockKbar);
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculate()).multiply(shadowKbarDTO.getPreStockKbar().getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
                shadowKbarDTO.setMarketMoney(marketMoney);
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                shadowKbarDTO.setBuyPrice(stockKbar.getAdjOpenPrice().add(new BigDecimal("0.01")));
                shadowKbarDTO.setOpenRate(openRate);
                openInfo(shadowKbarDTO);
                list.add(shadowKbarDTO);
            }
        }
        List<ShadowKbarDTO> shadows = new ArrayList<>();
        if(!CollectionUtils.isEmpty(list)){
            /*List<ShadowKbarDTO> shadowKbarDTOS = ShadowKbarDTO.openAmountRateSort(list);
            int i = 1;
            for (ShadowKbarDTO dto:shadowKbarDTOS){
                dto.setOpenAmountRateLevel(i);
                i++;
            }*/
            shadows.addAll(list);
        }
        return shadows;
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

    }


}
