package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
public class HotBlockDropBuyScoreComponent {
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

    public static Map<String,BlockLevelDTO> levelMap = new ConcurrentHashMap<>(8192);



    public void hotDrop(){
        Map<String, CirculateInfo> circulateMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            circulateMap.put(circulateInfo.getStockCode(),circulateInfo);
        }
        List<HotBlockDropBuyDTO> dailys = Lists.newArrayList();
        Map<String, List<HotBlockDropBuyDTO>> blockDayLevelMap = getBlockDayLevel();
        for (String tradeDate:blockDayLevelMap.keySet()){
            List<HotBlockDropBuyDTO> dtos = blockDayLevelMap.get(tradeDate);
            Map<String,HotBlockDropBuyDTO> map  = new HashMap<>();
            for (HotBlockDropBuyDTO dto:dtos) {
                List<HotBlockDropBuyDTO> dragons = getDragons(dto,circulateMap);
                if(dragons.size()==0){
                    continue;
                }
                for (HotBlockDropBuyDTO buyDto:dragons){
                    String key = buyDto.getStockCode() + buyDto.getTradeDate();
                    HotBlockDropBuyDTO hotBlockDropBuyDTO = map.get(key);
                    if(hotBlockDropBuyDTO==null){
                        map.put(key,buyDto);
                    }else{
                        if(dto.getBlockRaiseLevel()>=hotBlockDropBuyDTO.getBlockRaiseLevel()){
                            map.put(key,buyDto);
                        }
                    }
                }
            }
            for (String key:map.keySet()){
                dailys.add(map.get(key));
            }
        }
        dailys = score(dailys);
        List<Object[]> datas = Lists.newArrayList();
        for(HotBlockDropBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getBlockRaiseRate());
            list.add(dto.getBlockRaiseLevel());
            list.add(dto.getRaiseDays());
            list.add(dto.getBlockDropRate());
            list.add(dto.getOpenBlockRate());
            list.add(dto.getOpenBlockLevel());
            list.add(dto.getOpenRate());
            list.add(dto.getBuyDayLevel());
            list.add(dto.getRaiseDayRate());
            list.add(dto.getRaiseDayLevel());
            list.add(dto.getDropDayRate());
            list.add(dto.getDropDayLevel());
            list.add(dto.getDropDayExchange());
            list.add(dto.getBeforeRate3());
            list.add(dto.getBeforeRate5());
            list.add(dto.getBeforeRate10());
            list.add(dto.getRaiseDayPlankTime());
            list.add(dto.getBeforeAvgExchangeDay5());
            list.add(dto.getDropDayReds());
            list.add(dto.getDropDayGreens());
            list.add(dto.getDropDayBlockPlanks());
            list.add(dto.getBeforePlankDay5());
            list.add(dto.getRaiseNextDayOpenPlankFlag());
            list.add(dto.getRaiseNextDayOpenRate());
            list.add(dto.getRaiseDayBlockRate5());
            list.add(dto.getRaiseDayBlockRate10());
            list.add(dto.getBeforeOpenPlankDay5());
            list.add(dto.isDropDayHavePlank());
            list.add(dto.isDropDayEndPlank());
            list.add(dto.isBeforePlankDay3());
            list.add(dto.getScore());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","流通z","交易日期","板块代码","板块名称","大涨涨幅","大涨时排名","大涨间隔日期","大跌幅度","买入日开盘板块涨幅","买入日开盘板块排名",
                "股票买入日开盘涨幅","股票买入日开盘排名","股票大涨日涨幅","股票大涨日排名",
                "股票大跌日涨幅","股票大跌日排名","股票大跌日成交量","买入前3日涨跌幅","买入前5日涨跌幅","买入前10日涨跌幅","大涨日上板时间","买入前5日平均成交量","大跌日板块内收盘上涨数量","大跌日板块内收盘下跌数量","大跌日板块内板数",
                "买入前5日板数","大涨次日是否开一字","大涨次日开盘涨幅","大涨日板块5日涨幅","大涨日板块10日涨幅","买入前5日开一字次数","大跌日是否触及涨停","大跌日尾盘是否封住","买入前3天是否涨停","得分","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("热门大跌",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("热门大跌");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<HotBlockDropBuyDTO> score(List<HotBlockDropBuyDTO> dailys){
        Map<String,List<HotBlockDropBuyDTO>>  map = new HashMap<>();
        for (HotBlockDropBuyDTO daily:dailys){
            String raiseDayPlankTime = daily.getRaiseDayPlankTime();
            if(StringUtils.isBlank(raiseDayPlankTime)){
                continue;
            }
            String key = daily.getTradeDate()+daily.getBlockCode();
            List<HotBlockDropBuyDTO> dtos = map.get(key);
            if(dtos==null){
                dtos=Lists.newArrayList();
                map.put(key,dtos);
            }
            dtos.add(daily);
        }
        for (String mapKey:map.keySet()){
            List<HotBlockDropBuyDTO> stocks = map.get(mapKey);
            HotBlockDropBuyDTO.beforeRate5Sort(stocks);
            int score = stocks.get(0).getScore();
            stocks.get(0).setScore(score+1);
            HotBlockDropBuyDTO.dropDayExchangeSort(stocks);
            score = stocks.get(0).getScore();
            stocks.get(0).setScore(score+1);
            HotBlockDropBuyDTO.beforePlankDay5Sort(stocks);
            score = stocks.get(0).getScore();
            stocks.get(0).setScore(score+1);
            HotBlockDropBuyDTO.plankTimeSort(stocks);
            score = stocks.get(0).getScore();
            stocks.get(0).setScore(score+1);
        }
        List<HotBlockDropBuyDTO> list = Lists.newArrayList();
        for (String mapKey:map.keySet()){
            List<HotBlockDropBuyDTO> stocks = map.get(mapKey);
            list.addAll(stocks);
        }
        return list;
    }

    public List<HotBlockDropBuyDTO> getDragons(HotBlockDropBuyDTO dto,Map<String, CirculateInfo> circulateMap){
        List<HotBlockDropBuyDTO> datas = Lists.newArrayList();
        BigDecimal raiseDay5Total = BigDecimal.ZERO;
        int raiseDay5Count = 0;
        BigDecimal raiseDay10Total = BigDecimal.ZERO;
        int raiseDay10Count = 0;

        ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
        query.setBlockCode(dto.getBlockCode());
        List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(query);
        List<LevelDTO> buyDayRates = Lists.newArrayList();
        List<LevelDTO> dropDayRates = Lists.newArrayList();
        List<LevelDTO> raiseDayRates = Lists.newArrayList();
        int planks = 0;
        for (ThsBlockStockDetail detail:details){
            if(detail.getStockCode().startsWith("68")){
                continue;
            }
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setStockCode(detail.getStockCode());
            stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
            stockKbarQuery.setLimit(300);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            List<StockKbar> kbars = Lists.reverse(stockKbars);
            HotBlockDropBuyDTO buyDTO = new HotBlockDropBuyDTO();
            buyDTO.setStockCode(detail.getStockCode());
            buyDTO.setStockName(detail.getStockName());
            buyDTO.setTradeDate(dto.getTradeDate());
            buyDTO.setBlockCode(dto.getBlockCode());
            buyDTO.setBlockName(dto.getBlockName());
            buyDTO.setOpenBlockLevel(dto.getOpenBlockLevel());
            buyDTO.setOpenBlockRate(dto.getOpenBlockRate());
            buyDTO.setBlockRaiseRate(dto.getBlockRaiseRate());
            buyDTO.setBlockRaiseLevel(dto.getBlockRaiseLevel());
            buyDTO.setRaiseDays(dto.getRaiseDays());
            buyDTO.setBlockDropRate(dto.getBlockDropRate());
            openRate(buyDTO,kbars);
            highRateInfo(buyDTO,kbars);
            if(buyDTO.getRaiseDayRate5()!=null){
                raiseDay5Count++;
                raiseDay5Total = raiseDay5Total.add(buyDTO.getRaiseDayRate5());
            }
            if(buyDTO.getRaiseDayRate10()!=null){
                raiseDay10Count++;
                raiseDay10Total = raiseDay10Total.add(buyDTO.getRaiseDayRate10());
            }
            if(buyDTO.getOpenRate()!=null) {
                datas.add(buyDTO);
                if(buyDTO.isDropDayPlankFlag()){
                    planks++;
                }
                if(circulateMap.get(buyDTO.getStockCode())!=null){
                    buyDTO.setCirculateZ(circulateMap.get(buyDTO.getStockCode()).getCirculateZ());
                }
                LevelDTO openLevel = new LevelDTO();
                openLevel.setKey(buyDTO.getStockCode());
                openLevel.setRate(buyDTO.getOpenRate());
                buyDayRates.add(openLevel);
                LevelDTO dropDayLevel = new LevelDTO();
                dropDayLevel.setKey(buyDTO.getStockCode());
                dropDayLevel.setRate(buyDTO.getDropDayRate());
                dropDayRates.add(dropDayLevel);
                LevelDTO raiseDayLevel = new LevelDTO();
                raiseDayLevel.setKey(buyDTO.getStockCode());
                raiseDayLevel.setRate(buyDTO.getRaiseDayRate());
                raiseDayRates.add(raiseDayLevel);
            }
        }
        Integer dropDayReds = null;
        Integer dropDayGreens = null;
        for (LevelDTO levelDTO:dropDayRates){
            if(levelDTO.getRate().compareTo(BigDecimal.ZERO)==1){
                if(dropDayReds==null) {
                    dropDayReds = 1;
                }else{
                    dropDayReds++;
                }
            }
            if(levelDTO.getRate().compareTo(BigDecimal.ZERO)==-1){
                if(dropDayGreens==null) {
                    dropDayGreens = 1;
                }else{
                    dropDayGreens++;
                }
            }
        }
        Collections.sort(buyDayRates);
        Collections.sort(dropDayRates);
        Collections.sort(raiseDayRates);
        BigDecimal blockRate5 = null;
        BigDecimal blockRate10 = null;
        if(raiseDay5Count>0){
            blockRate5 = raiseDay5Total.divide(new BigDecimal(raiseDay5Count), 2, BigDecimal.ROUND_HALF_UP);
        }
        if(raiseDay10Count>0){
            blockRate10 = raiseDay10Total.divide(new BigDecimal(raiseDay10Count), 2, BigDecimal.ROUND_HALF_UP);
        }
        for (HotBlockDropBuyDTO data:datas){
            data.setDropDayReds(dropDayReds);
            data.setDropDayGreens(dropDayGreens);
            data.setDropDayBlockPlanks(planks);
            data.setRaiseDayBlockRate5(blockRate5);
            data.setRaiseDayBlockRate10(blockRate10);
            int i = 0;
            for (LevelDTO levelDTO:buyDayRates){
                i++;
                if(data.getStockCode().equals(levelDTO.getKey())){
                    data.setBuyDayLevel(i);
                }
            }

            int j = 0;
            for (LevelDTO levelDTO:raiseDayRates){
                j++;
                if(data.getStockCode().equals(levelDTO.getKey())){
                    data.setRaiseDayLevel(j);
                }
            }

            int k = 0;
            for (LevelDTO levelDTO:dropDayRates){
                k++;
                if(data.getStockCode().equals(levelDTO.getKey())){
                    data.setDropDayLevel(k);
                }
            }
        }

        return datas;

    }

    public void highRateInfo(HotBlockDropBuyDTO buyDTO,List<StockKbar> bars){
        List<StockKbar> reverse = Lists.reverse(bars);
        StockKbar raiseKbar = null;
        boolean raiseDayFlag = false;
        int raiseDay = 0;

        StockKbar nextKbar = null;
        boolean flagBuy = false;
        int j = 0;
        for (StockKbar bar:reverse){
            if(flagBuy){
                j++;
            }
            if(bar.getKbarDate().equals(buyDTO.getTradeDate())){
                flagBuy = true;
            }
            if(j==2){
                boolean highUpper = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getHighPrice(), bar.getClosePrice());
                boolean endUpper = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getHighPrice(), bar.getClosePrice());
                buyDTO.setDropDayHavePlank(highUpper);
                buyDTO.setDropDayEndPlank(endUpper);
            }
            if(j>=2&&j<=4){
                boolean endUpper = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getHighPrice(), bar.getClosePrice());
                BigDecimal endRate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                if(endUpper){
                    buyDTO.setBeforePlankDay3(true);
                }else if(MarketUtil.isChuangYe(buyDTO.getStockCode())&&endRate.compareTo(new BigDecimal(10))==1){
                    buyDTO.setBeforePlankDay3(true);
                }
            }
            if(buyDTO.getRaiseDays()==1){
                if(j==2){
                    raiseDayFlag  = true;
                    raiseKbar = bar;
                    buyDTO.setBlockRaiseDateStr(bar.getKbarDate());
                    boolean upperPrice = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getOpenPrice(), bar.getClosePrice());
                    buyDTO.setRaiseNextDayOpenPlankFlag(upperPrice);
                    BigDecimal openRate = PriceUtil.getPricePercentRate(nextKbar.getAdjOpenPrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                    buyDTO.setRaiseNextDayOpenRate(openRate);

                }
            }else if(buyDTO.getRaiseDays()==2){
                if(j==3){
                    raiseDayFlag  = true;
                    raiseKbar = bar;
                    buyDTO.setBlockRaiseDateStr(bar.getKbarDate());
                    boolean upperPrice = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getOpenPrice(), bar.getClosePrice());
                    buyDTO.setRaiseNextDayOpenPlankFlag(upperPrice);
                    BigDecimal openRate = PriceUtil.getPricePercentRate(nextKbar.getAdjOpenPrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                    buyDTO.setRaiseNextDayOpenRate(openRate);
                }
            }
            if(raiseDayFlag){
                raiseDay++;
            }
            if(raiseDay==6){
                BigDecimal rate5 = PriceUtil.getPricePercentRate(raiseKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setRaiseDayRate5(rate5);
            }
            if(raiseDay==11){
                BigDecimal rate11 = PriceUtil.getPricePercentRate(raiseKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setRaiseDayRate10(rate11);
            }

            nextKbar = bar;
        }
    }



    public void openRate(HotBlockDropBuyDTO buyDTO,List<StockKbar> bars){
        StockKbar preKbar = null;
        int i = 0;
        boolean flag = false;
        for (StockKbar bar:bars){
            if(flag){
                i++;
            }
            if(preKbar!=null){
                if(bar.getKbarDate().equals(buyDTO.getTradeDate())){
                    BigDecimal openRate = PriceUtil.getPricePercentRate(bar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    buyDTO.setOpenRate(openRate);
                    flag = true;
                }
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(bar.getStockCode(), DateUtil.parseDate(bar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, bar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(preKbar.getAdjOpenPrice()), preKbar.getAdjOpenPrice());
                buyDTO.setProfit(profit);
                break;
            }
            preKbar  = bar;
        }
        List<StockKbar> reverse = Lists.reverse(bars);
        Long totalExchangeDay5 = 0l;
        int exchangeDay5 = 0;
        int beforePlankDay5 = 0;
        int beforeOpenPlankDay5 = 0;
        StockKbar buyBeforeKbar = null;
        StockKbar nextKbar = null;
        boolean flagPlank = false;
        int j = 0;
        for (StockKbar bar:reverse){
            if(flagPlank){
                j++;
            }
            if(bar.getKbarDate().equals(buyDTO.getTradeDate())){
                flagPlank = true;
            }
            if(j==1){
                buyBeforeKbar = bar;
                buyDTO.setDropDayExchange(bar.getTradeQuantity());
            }
            if(j==2){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyBeforeKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeCloseRate(rate);
                buyDTO.setDropDayRate(rate);
                boolean upperPrice = PriceUtil.isUpperPrice(buyDTO.getStockCode(), buyBeforeKbar.getClosePrice(), bar.getClosePrice());
                buyDTO.setDropDayPlankFlag(upperPrice);
            }
            if(buyDTO.getRaiseDays()==1){
                if(j==3){
                    BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                    buyDTO.setRaiseDayRate(rate);
                    boolean upperPrice = PriceUtil.isUpperPrice(bar.getStockCode(), nextKbar.getHighPrice(), bar.getClosePrice());
                    if(upperPrice) {
                        String plankTime = getPlankTime(bar.getStockCode(), nextKbar.getKbarDate(), bar.getClosePrice());
                        buyDTO.setRaiseDayPlankTime(plankTime);
                    }

                }
            }
            if(buyDTO.getRaiseDays()==2){
                if(j==4){
                    BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                    buyDTO.setRaiseDayRate(rate);
                    boolean upperPrice = PriceUtil.isUpperPrice(bar.getStockCode(), nextKbar.getHighPrice(), bar.getClosePrice());
                    if(upperPrice) {
                        String plankTime = getPlankTime(bar.getStockCode(), nextKbar.getKbarDate(), bar.getClosePrice());
                        buyDTO.setRaiseDayPlankTime(plankTime);
                    }
                }
            }
            if(j==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyBeforeKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeRate3(rate);
            }
            if(j<=5 && j>=1){
                totalExchangeDay5 = totalExchangeDay5+bar.getTradeQuantity();
                exchangeDay5 = exchangeDay5+1;
                long avgExchange = totalExchangeDay5 / exchangeDay5;
                buyDTO.setBeforeAvgExchangeDay5(avgExchange);
            }
            if(j<=6 && j>1){
                boolean upperPrice = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getClosePrice(), bar.getClosePrice());
                if(upperPrice){
                    beforePlankDay5++;
                }
                buyDTO.setBeforePlankDay5(beforePlankDay5);
                boolean openUpper = PriceUtil.isUpperPrice(buyDTO.getStockCode(), nextKbar.getOpenPrice(), bar.getClosePrice());
                if(openUpper){
                    beforeOpenPlankDay5++;
                }
                buyDTO.setBeforeOpenPlankDay5(beforeOpenPlankDay5);
            }
            if(j==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyBeforeKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeRate5(rate);
            }
            if(j==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyBeforeKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeRate10(rate);
            }
            nextKbar = bar;
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

    public Map<String,List<HotBlockDropBuyDTO>> getBlockDayLevel(){
        Map<String,List<HotBlockDropBuyDTO>> resultMap = new HashMap<>();
        Map<String,Map<String, BlockLevelDTO>> allCloseLevelMap = new HashMap<>();
        Map<String,Map<String, BlockLevelDTO>> allOpenLevelMap = new HashMap<>();

        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-01-01",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("2021-09-15",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        Map<String, StockKbar> preEndPriceMap = new HashMap<>();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(tradeDateStr);
            StockKbarQuery query = new StockKbarQuery();
            query.setKbarDate(tradeDateStr);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockRateDTO> openRates = Lists.newArrayList();
            List<StockRateDTO> closeRates = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                StockKbar preStockKbar = preEndPriceMap.get(stockKbar.getStockCode());
                if(preStockKbar!=null){
                    BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    StockRateDTO closeRateDTO = new StockRateDTO();
                    closeRateDTO.setStockCode(stockKbar.getStockCode());
                    closeRateDTO.setStockName(stockKbar.getStockName());
                    closeRateDTO.setRate(closeRate);

                    StockRateDTO openRateDTO  = new StockRateDTO();
                    openRateDTO.setStockCode(stockKbar.getStockCode());
                    openRateDTO.setStockName(stockKbar.getStockName());
                    openRateDTO.setRate(openRate);
                    openRates.add(openRateDTO);
                    closeRates.add(closeRateDTO);
                }
                preEndPriceMap.put(stockKbar.getStockCode(),stockKbar);
            }
            Map<String, BlockLevelDTO> closeLevelMap = blockLevelReplayComponent.calBlockLevelDTO(closeRates);
            allCloseLevelMap.put(tradeDateStr,closeLevelMap);
            Map<String, BlockLevelDTO> openLevelMap = blockLevelReplayComponent.calBlockLevelDTO(openRates);
            allOpenLevelMap.put(tradeDateStr,openLevelMap);
        }
        List<BlockLevelDTO> day3Levels = Lists.newArrayList();
        List<BlockLevelDTO> day2Levels = Lists.newArrayList();
        List<BlockLevelDTO> day1Levels = Lists.newArrayList();
        List<BlockLevelDTO> day1Greens = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            Map<String, BlockLevelDTO> closeLevelMap = allCloseLevelMap.get(tradeDateStr);
            Map<String, BlockLevelDTO> openLevelMap = allOpenLevelMap.get(tradeDateStr);
            for (BlockLevelDTO blockLevelDTO:day1Greens){
                BlockLevelDTO raiseLevelDTO = null;
                int days = 0;
                int i = 0;
                boolean flag2 = false;
                for (BlockLevelDTO day2LeveDTO:day2Levels){
                    i++;
                    if(i<=3&&day2LeveDTO.getBlockCode().equals(blockLevelDTO.getBlockCode())){
                        flag2 = true;
                        raiseLevelDTO  = day2LeveDTO;
                        days = 1;
                    }
                    if(i>=3){
                        break;
                    }
                }
                boolean flag3 = false;
                if(!flag2) {
                    int j = 0;
                    for (BlockLevelDTO day3LeveDTO : day3Levels) {
                        j++;
                        if (j <= 3 && day3LeveDTO.getBlockCode().equals(blockLevelDTO.getBlockCode())) {
                            flag3 = true;
                            raiseLevelDTO  = day3LeveDTO;
                            days = 2;
                        }
                        if (j >= 3) {
                            break;
                        }
                    }
                }
                if(flag2||flag3){
                    List<HotBlockDropBuyDTO> hotBlockDropBuyDTOS = resultMap.get(tradeDateStr);
                    if(CollectionUtils.isEmpty(hotBlockDropBuyDTOS)){
                        hotBlockDropBuyDTOS = new ArrayList<>();
                        resultMap.put(tradeDateStr,hotBlockDropBuyDTOS);
                    }
                    BlockLevelDTO currentOpenDayDto = openLevelMap.get(blockLevelDTO.getBlockCode());
                    HotBlockDropBuyDTO hotBlockDropBuyDTO = new HotBlockDropBuyDTO();
                    hotBlockDropBuyDTO.setTradeDate(tradeDateStr);
                    hotBlockDropBuyDTO.setBlockCode(blockLevelDTO.getBlockCode());
                    hotBlockDropBuyDTO.setBlockName(blockLevelDTO.getBlockName());
                    if(currentOpenDayDto!=null) {
                        hotBlockDropBuyDTO.setOpenBlockLevel(currentOpenDayDto.getLevel());
                        hotBlockDropBuyDTO.setOpenBlockRate(currentOpenDayDto.getAvgRate());
                    }

                    hotBlockDropBuyDTO.setBlockDropRate(blockLevelDTO.getAvgRate());
                    hotBlockDropBuyDTO.setBlockRaiseRate(raiseLevelDTO.getAvgRate());
                    hotBlockDropBuyDTO.setBlockRaiseLevel(raiseLevelDTO.getLevel());
                    hotBlockDropBuyDTO.setRaiseDays(days);
                    hotBlockDropBuyDTOS.add(hotBlockDropBuyDTO);
                }

            }
            List<BlockLevelDTO> list = Lists.newArrayList();
            if(closeLevelMap==null||closeLevelMap.size()==0){
                continue;
            }
            for (String key:closeLevelMap.keySet()){
                list.add(closeLevelMap.get(key));
            }
            day1Greens.clear();
            Collections.sort(list);
            for (BlockLevelDTO dto:list){
                if(dto.getAvgRate().compareTo(new BigDecimal(-1))==-1){
                    day1Greens.add(dto);
                }
            }
            day3Levels  = day2Levels;
            day2Levels = day1Levels;
            day1Levels = list;
        }
        return resultMap;
    }


    public static void main(String[] args) {
        HotBlockDropBuyDTO bestBuyStockDTO1 = new HotBlockDropBuyDTO();
        bestBuyStockDTO1.setRaiseDayPlankTime("09:31");

        HotBlockDropBuyDTO bestBuyStockDTO2 = new HotBlockDropBuyDTO();
        bestBuyStockDTO2.setRaiseDayPlankTime("09:31");

        HotBlockDropBuyDTO bestBuyStockDTO3 = new HotBlockDropBuyDTO();
        bestBuyStockDTO3.setRaiseDayPlankTime("09:33");

        HotBlockDropBuyDTO bestBuyStockDTO4 = new HotBlockDropBuyDTO();
        bestBuyStockDTO4.setRaiseDayPlankTime("09:34");

        HotBlockDropBuyDTO bestBuyStockDTO5 = new HotBlockDropBuyDTO();
        bestBuyStockDTO5.setRaiseDayPlankTime("09:35");

        List<HotBlockDropBuyDTO> list = Lists.newArrayList();
        list.add(bestBuyStockDTO5);
        list.add(bestBuyStockDTO1);
        list.add(bestBuyStockDTO2);
        list.add(bestBuyStockDTO4);
        list.add(bestBuyStockDTO3);

        List<HotBlockDropBuyDTO> bestBuyStockDTOS = HotBlockDropBuyDTO.plankTimeSort(list);

        System.out.println(bestBuyStockDTOS);


    }


}
