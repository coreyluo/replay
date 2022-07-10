package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockBackRowComponent {
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
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired
    private StockIndexService stockIndexService;

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");



    public void backRowBuyExcel(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, CirculateInfo> circulateInfoMap = getCirculateInfoMap(circulateInfos);
        List<HistoryBlockInfo> blockInfos = getHistoryBlockInfo();
        Map<String, List<PlankTimePairDTO>> pairsMap = getPlankTimePairs(circulateInfos);
        List<BlocKFollowBuyDTO> buys = Lists.newArrayList();
        int count = pairsMap.size();
        int index = 0;
        for (String tradeDate:pairsMap.keySet()) {
            index++;
            System.out.println(index+"===="+count);
            THREAD_POOL_QUOTE.execute(() ->{
                List<PlankTimePairDTO> plankTimePairDTOS = pairsMap.get(tradeDate);
                List<BlocKFollowBuyDTO> dtos = blockBuys(blockInfos, plankTimePairDTOS, circulateInfoMap, tradeDate);
                if(!CollectionUtils.isEmpty(dtos)) {
                    buys.addAll(dtos);
                }
                //buyPriceCacheMap.remove(tradeDate);
            });

        }
        try {
            Thread.sleep(10000000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        /*List<BlocKFollowBuyDTO> buys = Lists.newArrayList();
        List<HistoryBlockInfo> blockInfos = getHistoryBlockInfo();
        for (HistoryBlockInfo blockInfo:blockInfos){
            TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
            tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
            List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(new TradeDatePoolQuery());
            Date preDate = null;
            for (TradeDatePool tradeDatePool:tradeDatePools){
                boolean before = tradeDatePool.getTradeDate().before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd));
                boolean after = tradeDatePool.getTradeDate().after(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd));
                if((!before)&&(!after)) {
                    String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
                    String key = blockInfo.getBlockCode() + "_" + format + "_" + "bkgs";
                    RedisMonior redisMonior = redisMoniorService.getByRedisKey(key);
                    if (redisMonior != null) {
                        String redisValue = redisMonior.getRedisValue();
                        BlocKFollowBuyDTO follow = JSONObject.parseObject(redisValue, BlocKFollowBuyDTO.class);
                        String preFormat = DateUtil.format(preDate, DateUtil.yyyyMMdd);
                        StockIndex stockIndex = stockIndexService.getByUniqueKey(follow.getBlockCode() + "_" + preFormat);
                        if (stockIndex != null) {
                            follow.setBiasDay6(stockIndex.getBias6());
                            follow.setBiasDay12(stockIndex.getBias12());
                            follow.setBiasDay24(stockIndex.getBias24());
                            follow.setMacd(stockIndex.getMacd());
                        }
                        buys.add(follow);
                    }
                }
                preDate = tradeDatePool.getTradeDate();
            }
        }

        List<Object[]> datas = Lists.newArrayList();
        for (BlocKFollowBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getBefore10Planks());
            list.add(dto.getBefore10OnePlanks());
            list.add(dto.getBeautifulPlanks10());
            list.add(dto.getBeautifulPlanks3());
            list.add(dto.getBeautifulPlanks3First());
            list.add(dto.getBiasDay6());
            list.add(dto.getBiasDay12());
            list.add(dto.getBiasDay24());
            list.add(dto.getMacd());

            list.add(dto.getProfit10First());
            list.add(dto.getCount10First());
            list.add(dto.getProfit10Two());
            list.add(dto.getCount10Two());
            list.add(dto.getProfit10Three());
            list.add(dto.getCount10Three());

            list.add(dto.getTimeStamp3());
            list.add(dto.getProfit3PlankFirst());
            list.add(dto.getCount3PlankFirst());
            list.add(dto.getProfit3PlankTwo());
            list.add(dto.getCount3PlankTwo());
            list.add(dto.getProfit3PlankThree());
            list.add(dto.getCount3PlankThree());

            list.add(dto.getTimeStamp3First());
            list.add(dto.getProfit3FirstPlankFirst());
            list.add(dto.getCount3FirstPlankFirst());
            list.add(dto.getProfit3FirstPlankTwo());
            list.add(dto.getCount3FirstPlankTwo());
            list.add(dto.getProfit3FirstPlankThree());
            list.add(dto.getCount3FirstPlankThree());


            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","交易日期","板块代码","板块名称","10点前涨停数量","10点前首板个数","1字板个数10点前","1字板个数3块板买入","1字板个数3首板板买入","乖离率6","乖离率12","乖离率24","macd26-12",
                "10点前买入小","10点前买入小count","10点前买入中","10点前买入中count","10点前买入大","10点前买入大count",
                "3块板买入时间","3块板买入小","3块板买入小count","3块板买入中","3块板买入中count","3块板买入大","3块板买入大count",
                "3块首板买入时间","3块首板买入小","3块首板买入小count","3块首板买入中","3块首板买入中count","3块首板买入大","3块首板买入大count"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板块跟随买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板块跟随买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }


    public Map<String, List<PlankTimePairDTO>> getPlankTimePairs(List<CirculateInfo> circulateInfos){
        Map<String, List<PlankTimePairDTO>> map = new HashMap<>();
        int m = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            m++;
            System.out.println(circulateInfo.getStockCode()+"======"+m);
           /* if(!circulateInfo.getStockCode().equals("001318")){
                continue;
            }*/
            /*if(m>=1000){
                return map;
            }
*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(date.after(DateUtil.parseDate("20220401", DateUtil.yyyyMMdd))){
                    continue;
                }

                if(preKbar!=null) {
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(highUpper){
                        int planks = calPlanks(limitQueue);
                        PlankBuyTimeDTO plankBuy = getPlankPairs(stockKbar, preKbar.getClosePrice(),planks);

                    }
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public List<HistoryBlockInfo> getHistoryBlockInfo(){
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        return historyBlockInfos;
    }

    public List<BlocKFollowBuyDTO> blockBuys(List<HistoryBlockInfo> blockInfos,List<PlankTimePairDTO> pairs,Map<String, CirculateInfo> circulateInfoMap,String tradeDate){
        List<BlocKFollowBuyDTO> buys = Lists.newArrayList();
        Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        Date nextTradeDate = commonComponent.afterTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        List<StockKbar> stockKbars = getStockKbarsByKBarDate(tradeDate);
        List<StockKbar> nextStockKbars = getStockKbarsByKBarDate(DateUtil.format(nextTradeDate,DateUtil.yyyyMMdd));
        List<StockKbar> preStockKbars = getStockKbarsByKBarDate(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(stockKbars)||CollectionUtils.isEmpty(preStockKbars)||CollectionUtils.isEmpty(nextStockKbars)){
            return buys;
        }
        Map<String, StockKbar> stockMap = new HashMap<>();
        Map<String, StockKbar> nestStockMap = new HashMap<>();
        Map<String, StockKbar> preStockMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            stockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:nextStockKbars){
            nestStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:preStockKbars){
            preStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (HistoryBlockInfo blockInfo:blockInfos){
            String rediesKey = blockInfo.getBlockCode() + "_" + tradeDate + "_" + "bkgs";
            RedisMonior redisMinor = redisMoniorService.getByRedisKey(rediesKey);
            if(redisMinor!=null){
                System.out.println(rediesKey);
                continue;
            }
            List<PlankTimePairDTO> blockPairs = Lists.newArrayList();
            List<String> stocks = getBlockStocks(blockInfo.getBlockCode(), tradeDate);
            if(CollectionUtils.isEmpty(stocks)){
                continue;
            }
            for (PlankTimePairDTO pair:pairs){
                if(stocks.contains(pair.getStockCode())){
                    blockPairs.add(pair);
                }
            }
            if(blockPairs.size()<3){
                continue;
            }
            Map<String, List<MarketMoneyDTO>> threeBuyLevelStocks = getThreeBuyLevelStocks(stocks, circulateInfoMap, stockMap);
            BlocKFollowBuyDTO blocKFollowBuyDTO = new BlocKFollowBuyDTO();
            blocKFollowBuyDTO.setBlockCode(blockInfo.getBlockCode());
            blocKFollowBuyDTO.setBlockName(blockInfo.getBlockName());
            blocKFollowBuyDTO.setTradeDate(tradeDate);
            List<PlankTimePairDTO> plankTens = judgePlanks100000(blockPairs);
            List<PlankTimePairDTO> firstPlankTens = judgeFirstPlanks100000(blockPairs);
            blocKFollowBuyDTO.setBefore10Planks(plankTens.size());
            blocKFollowBuyDTO.setBefore10OnePlanks(firstPlankTens.size());
            Long threePlankTime = judgeThreePlanks(blockPairs);
            Long threeFirstPlankTime = judgeThreeFirstPlanks(blockPairs);
            List<String> buyTimes = Lists.newArrayList();
            if(plankTens.size()>=3){
                buyTimes.add("100000");
            }
            if(threePlankTime!=null){
                String times = threePlankTime.toString();
                if(!buyTimes.contains(times)) {
                    buyTimes.add(times);
                }
            }
            if(threeFirstPlankTime!=null){
                String times = threeFirstPlankTime.toString();
                if(!buyTimes.contains(times)) {
                    buyTimes.add(times);
                }
            }
            if(buyTimes.size()>0) {
                Map<String, BlockBuyProfitDTO> firstProfitMap = getBuysProfit(threeBuyLevelStocks.get("first"), buyTimes, stockMap, nestStockMap, preStockMap);
                Map<String, BlockBuyProfitDTO> twoProfitMap = getBuysProfit(threeBuyLevelStocks.get("two"), buyTimes, stockMap, nestStockMap, preStockMap);
                Map<String, BlockBuyProfitDTO> threeProfitMap = getBuysProfit(threeBuyLevelStocks.get("three"), buyTimes, stockMap, nestStockMap, preStockMap);
                if (plankTens.size() >= 3) {
                    List<PlankTimePairDTO> beautifulPlanks = judgeBeautifulPlanks(blockPairs, 100000l);
                    blocKFollowBuyDTO.setBeautifulPlanks10(beautifulPlanks.size());
                    BlockBuyProfitDTO firstProfit = firstProfitMap.get("100000");
                    BlockBuyProfitDTO twoProfit = twoProfitMap.get("100000");;
                    BlockBuyProfitDTO threeProfit = threeProfitMap.get("100000");
                    if(firstProfit!=null) {
                        blocKFollowBuyDTO.setProfit10First(firstProfit.getAvgProfit());
                        blocKFollowBuyDTO.setCount10First(firstProfit.getCount());
                    }
                    if(twoProfit!=null) {
                        blocKFollowBuyDTO.setProfit10Two(twoProfit.getAvgProfit());
                        blocKFollowBuyDTO.setCount10Two(twoProfit.getCount());
                    }
                    if(threeProfit!=null) {
                        blocKFollowBuyDTO.setProfit10Three(threeProfit.getAvgProfit());
                        blocKFollowBuyDTO.setCount10Three(threeProfit.getCount());
                    }
                }

                if (plankTens.size() >= 3 || threePlankTime != null || threeFirstPlankTime != null) {
                    StockIndex stockIndex = stockIndexService.getByUniqueKey(blocKFollowBuyDTO.getBlockCode() + "_" + blocKFollowBuyDTO.getTradeDate());
                    if (stockIndex != null) {
                        blocKFollowBuyDTO.setBiasDay6(stockIndex.getBias6());
                        blocKFollowBuyDTO.setBiasDay12(stockIndex.getBias12());
                        blocKFollowBuyDTO.setBiasDay24(stockIndex.getBias24());
                        blocKFollowBuyDTO.setMacd(stockIndex.getMacd());
                    }
                    buys.add(blocKFollowBuyDTO);

                    RedisMonior redisMonior = new RedisMonior();
                    redisMonior.setRedisKey(blocKFollowBuyDTO.getBlockCode()+"_"+blocKFollowBuyDTO.getTradeDate()+"_"+"bkgs");
                    redisMonior.setRedisValue(JSONObject.toJSONString(blocKFollowBuyDTO));
                    redisMonior.setCreateTime(new Date());
                    redisMoniorService.save(redisMonior);
                    System.out.println(blocKFollowBuyDTO.getBlockCode()+"_"+blocKFollowBuyDTO.getTradeDate()+"_"+"bkgs"+"========没有跑到");
                }
            }

        }
        return buys;
    }

    public List<PlankTimePairDTO> judgePlanks100000(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getStart()<=100000&&(pair.getEnd()==null||pair.getEnd()>100000)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }
    public List<PlankTimePairDTO> judgeFirstPlanks100000(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getPlanks()!=1){
                continue;
            }
            if(pair.getStart()<=100000&&(pair.getEnd()==null||pair.getEnd()>100000)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }

    public List<PlankTimePairDTO> judgeBeautifulPlanks(List<PlankTimePairDTO> pairs,Long timeLong){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getStart()!=92500){
                continue;
            }
            if((pair.getEnd()==null||pair.getEnd()>timeLong)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }

    public Long judgeThreePlanks(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> pairsCopy = Lists.newArrayList();
        pairsCopy.addAll(pairs);
        for (PlankTimePairDTO pair:pairs){
            int i = 0;
            for (PlankTimePairDTO dto:pairsCopy) {
                if(!dto.getStockCode().equals(pair.getStockCode())) {
                    if (dto.getStart() <= pair.getStart() && (dto.getEnd() == null || dto.getEnd() > pair.getStart())) {
                        i++;
                        if(i>=2) {
                            return pair.getStart();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Long judgeThreeFirstPlanks(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> pairsCopy = Lists.newArrayList();
        pairsCopy.addAll(pairs);
        for (PlankTimePairDTO pair:pairs){
            if(pair.getPlanks()!=1){
                continue;
            }
            int i = 0;
            for (PlankTimePairDTO dto:pairsCopy) {
                if((!dto.getStockCode().equals(pair.getStockCode()))&&dto.getPlanks()==1) {
                    if (dto.getStart() <= pair.getStart() && (dto.getEnd() == null || dto.getEnd() > pair.getStart())) {
                        i++;
                        if(i>=2) {
                            return pair.getStart();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Map<String, List<MarketMoneyDTO>> getThreeBuyLevelStocks(List<String> stocks,Map<String,CirculateInfo> circulateInfoMap,Map<String, StockKbar> stockKbarMap){
        Map<String, List<MarketMoneyDTO>> map = new HashMap<>();
        List<MarketMoneyDTO> list = Lists.newArrayList();
        for (String stock:stocks){
            CirculateInfo circulateInfo = circulateInfoMap.get(stock);
            StockKbar stockKbar = stockKbarMap.get(stock);
            if(stockKbar!=null&&circulateInfo!=null) {
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getOpenPrice()).setScale(2,BigDecimal.ROUND_HALF_UP);
                MarketMoneyDTO marketMoneyDTO = new MarketMoneyDTO();
                marketMoneyDTO.setStockCode(stock);
                marketMoneyDTO.setStockName(circulateInfo.getStockName());
                marketMoneyDTO.setCirculate(circulateInfo.getCirculateZ());
                marketMoneyDTO.setMarketMoney(marketMoney);
                list.add(marketMoneyDTO);
            }
        }
        if(CollectionUtils.isEmpty(list)||list.size()<=3){
            return map;
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);
        Map<String, List<Integer>> levelIndex = getLevelIndex(marketMoneyDTOS.size());
        List<Integer> firsts = levelIndex.get("first");
        List<Integer> twos = levelIndex.get("two");
        List<Integer> threes = levelIndex.get("three");
        List<MarketMoneyDTO> firstStocks = marketMoneyDTOS.subList(0, firsts.get(1));
        List<MarketMoneyDTO> twoStocks = marketMoneyDTOS.subList(twos.get(0)-1, twos.get(1));
        List<MarketMoneyDTO> threeStocks = marketMoneyDTOS.subList(threes.get(0)-1, threes.get(1));
        map.put("first",firstStocks);
        map.put("two",twoStocks);
        map.put("three",threeStocks);
        return map;
    }
    public Map<String,BlockBuyProfitDTO> getBuysProfit(List<MarketMoneyDTO> marketDtos,List<String> tradeTimes,Map<String, StockKbar> stockKbarMap,Map<String, StockKbar> nextStockKbarMap,Map<String, StockKbar> preStockKbarMap){
        Map<String, BlockBuyProfitDTO> map = new HashMap<>();
        Map<String, BlockBuyProfitDTO> totalMap = new HashMap<>();
        for (MarketMoneyDTO dto:marketDtos){
            StockKbar stockKbar = stockKbarMap.get(dto.getStockCode());
            StockKbar nextStockKbar = nextStockKbarMap.get(dto.getStockCode());
            StockKbar preStockKbar = preStockKbarMap.get(dto.getStockCode());
            if(stockKbar==null||preStockKbar==null||nextStockKbar==null){
                continue;
            }
            Map<String, BigDecimal> buyPriceMap = getStockBuyPrice(dto.getStockCode(), stockKbar.getKbarDate(), tradeTimes, preStockKbar);
            BigDecimal sellPrice = null;
            if(nextStockKbar.getTradeQuantity()!=null&&nextStockKbar.getTradeQuantity()!=0){
                sellPrice = nextStockKbar.getTradeAmount().divide(new BigDecimal(nextStockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
            }
            for (String buyTime:tradeTimes) {
                BigDecimal stockBuyPrice = buyPriceMap.get(buyTime);
                if (stockBuyPrice != null && sellPrice != null) {
                    BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(stockBuyPrice, stockKbar);
                    BigDecimal chuQuanSellPrice = chuQuanAvgPrice(sellPrice, stockKbar);
                    BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanSellPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
                    BlockBuyProfitDTO totalProfitDTO = totalMap.get(buyTime);
                    if(totalProfitDTO==null){
                        totalProfitDTO = new BlockBuyProfitDTO();
                        totalMap.put(buyTime,totalProfitDTO);
                    }
                    if(totalProfitDTO.getAvgProfit()==null) {
                        totalProfitDTO.setAvgProfit(profit);
                    }else{
                        totalProfitDTO.setAvgProfit(totalProfitDTO.getAvgProfit().add(profit));
                    }
                    totalProfitDTO.setCount(totalProfitDTO.getCount()+1);
                }
            }
        }
        for (String buyTime:totalMap.keySet()){
            BlockBuyProfitDTO blockBuyProfitDTO = totalMap.get(buyTime);
            if(blockBuyProfitDTO!=null&&blockBuyProfitDTO.getCount()>0){
                BigDecimal avgProfit = blockBuyProfitDTO.getAvgProfit().divide(new BigDecimal(blockBuyProfitDTO.getCount()), 2, BigDecimal.ROUND_HALF_UP);
                BlockBuyProfitDTO buyDto = new BlockBuyProfitDTO();
                buyDto.setAvgProfit(avgProfit);
                buyDto.setCount(blockBuyProfitDTO.getCount());
                map.put(buyTime,buyDto);
            }
        }
        return map;
    }

    public Map<String,BigDecimal> getStockBuyPrice(String stockCode,String tradeDate,List<String> buyTimes,StockKbar preStockKbar){
        Map<String, BigDecimal> map = new HashMap<>();
        /*boolean flag = true;
        for(String buyTime:buyTimes) {
            Map<String, BigDecimal> tradeDateBuyPriceMap = buyPriceCacheMap.get(tradeDate);
            if(tradeDateBuyPriceMap==null||tradeDateBuyPriceMap.get(stockCode + "_" + tradeDate + "_" + buyTime)==null){
                flag = false;
                break;
            }
            BigDecimal buyPrice = tradeDateBuyPriceMap.get(stockCode + "_" + tradeDate + "_" + buyTime);
            map.put(buyTime,buyPrice);
        }
        if(flag){
            return map;
        }*/
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        for(String buyTime:buyTimes) {
            long buyTimeInt = timeToLong(buyTime);
            String preMin = "09:25";
            Integer index = -1;
            for (ThirdSecondTransactionDataDTO data : datas) {
                BigDecimal tradePrice = data.getTradePrice();
                Integer tradeType = data.getTradeType();
                String tradeTime = data.getTradeTime();
                boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
                if (tradeTime.equals(preMin)) {
                    index++;
                } else {
                    preMin = tradeTime;
                    index = 0;
                }
                long timeLong = timeToLong(tradeTime, index);
                if (timeLong >= buyTimeInt) {
                    if (historyUpperPrice && tradeType == 1) {
                        break;
                    }
                    map.put(buyTime,tradePrice);
                    /*Map<String, BigDecimal> tradeDateBuyPriceMap = buyPriceCacheMap.get(tradeDate);
                    if(tradeDateBuyPriceMap==null){
                        tradeDateBuyPriceMap = new HashMap<>();
                        buyPriceCacheMap.put(tradeDate,tradeDateBuyPriceMap);
                    }
                    tradeDateBuyPriceMap.put((stockCode + "_" + tradeDate + "_" + buyTime),tradePrice);*/
                    break;
                }
            }
        }
        return map;
    }


    public static Map<String,List<Integer>> getLevelIndex(int size){
        Map<String, List<Integer>> map = new HashMap<>();
        if(size<=4){
            map.put("first",Lists.newArrayList(1,1));
            map.put("two",Lists.newArrayList(2,2));
            map.put("three",Lists.newArrayList(size,size));
            return map;
        }
        Integer levelSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.3")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        Integer halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size%2==1) {
            halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_UP).toString());
        }
        Integer handleSize = Integer.valueOf(new BigDecimal(levelSize).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size>=60){
            levelSize = 20;
            handleSize = 10;
        }
        map.put("first",Lists.newArrayList(1,levelSize));
        if(handleSize>0) {
            if(levelSize%2==0) {
                map.put("two", Lists.newArrayList(halfSize - handleSize + 1, halfSize + handleSize));
            }else{
                map.put("two", Lists.newArrayList(halfSize - handleSize, halfSize + handleSize));
            }
        }else{
            map.put("two", Lists.newArrayList(halfSize, halfSize));
        }
        map.put("three",Lists.newArrayList(size-levelSize+1,size));
        return map;
    }

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList();
        for (int i=0 ; i<=99 ;i++) {
            list.add(i);
        }
        for (int i=3 ; i<=100 ;i++) {
            Map<String, List<Integer>> levelIndex = getLevelIndex(i);
            System.out.println(levelIndex);
            List<Integer> firsts = list.subList(0, levelIndex.get("first").get(1));
            List<Integer> twos = list.subList(levelIndex.get("two").get(0)-1, levelIndex.get("two").get(1));
            List<Integer> threes = list.subList(levelIndex.get("three").get(0)-1, levelIndex.get("three").get(1));
            System.out.println(firsts);
            System.out.println(twos);
            System.out.println(threes);
            System.out.println(i+"======================");

        }

        List<Integer> subs = list.subList(2, 3);
        System.out.println(subs);
    }



    public Map<String,CirculateInfo> getCirculateInfoMap(List<CirculateInfo> circulateInfos){
        Map<String, CirculateInfo> circulateInfoMap= new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            circulateInfoMap.put(circulateInfo.getStockCode(),circulateInfo);
        }
        return circulateInfoMap;
    }

    public List<TradeDatePool> getTradeDates(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        /*query.setTradeDateTo(DateUtil.parseDate("20220501",DateUtil.yyyyMMdd));*/
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }





    public List<String> getBlockStocks(String blockCode,String tradeDate){
        List<String> list = Lists.newArrayList();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode(blockCode);
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocksList = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(historyBlockStocksList)){
            return null;
        }
        HistoryBlockStocks historyBlockStocks = historyBlockStocksList.get(0);
        String stocks = historyBlockStocks.getStocks();
        if(StringUtils.isBlank(stocks)){
            return null;
        }
        String[] split = stocks.split(",");
        List<String> stockList = Lists.newArrayList(split);
        for (String stockCode:stockList){
            boolean shMain = MarketUtil.isShMain(stockCode);
            boolean chuangYe = MarketUtil.isChuangYe(stockCode);
            boolean szMain = MarketUtil.isSzMain(stockCode);
            if(shMain||chuangYe||szMain){
                list.add(stockCode);
            }
        }
        return list;
    }


    public List<StockKbar> limitQueueToList(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        return list;
    }


    public PlankBuyTimeDTO getPlankPairs(StockKbar stockKbar,BigDecimal preEndPrice,int planks){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        PlankBuyTimeDTO pairDTO = new PlankBuyTimeDTO();
        pairDTO.setPlanks(planks);
        pairDTO.setStockCode(stockKbar.getStockCode());
        int i = 0;
        int index = 0;
        String preMin = "09:25";
        boolean gatherUpper = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(data.getTradeTime().equals(preMin)){
                index++;
            }else{
                preMin = data.getTradeTime();
                index = 0;
            }
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            if(data.getTradeTime().equals("09:25")&&upperPrice){
                gatherUpper = true;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=0&&tradeType!=1||data.getTradeTime().equals("09:25")){
                continue;
            }
            i++;
            if(i==1&&gatherUpper&&upperPrice&&tradeType==1){
                pairDTO.setStart(92500l);
                continue;
            }
            if(upperPrice&&tradeType==1) {
                if(pairDTO.getStart()==null) {
                    long start = timeToLong(data.getTradeTime(), index);
                    pairDTO.setStart(start);
                }
            }


        }
        return pairDTO;
    }

    public static long timeToLong(String time,int index){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        int second = index * 3;
        if(second<10) {
            timeStr = timeStr + "0" +second;
        }else{
            timeStr = timeStr+second;
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }

    public static long timeToLong(String time){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }


    public void calProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                /*if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }*/
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjClosePrice()), buyStockKbar.getAdjClosePrice());
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }




    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            /*query.setKbarDateFrom("");
            query.setKbarDateTo("");*/
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
           /*if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            stockKbars = stockKbars.subList(20, stockKbars.size());*/
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<StockKbar> best = commonComponent.deleteNewStockTimes(stockKbars, 2000);
            return best;
        }catch (Exception e){
            return null;
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

    public int calPlanks(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)){
            return 1;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 1;
        int i=0;
        StockKbar nextStockKbar = null;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i>=3){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextStockKbar.getClosePrice(), stockKbar.getClosePrice(), nextStockKbar.getKbarDate());
                if(endUpper){
                    planks++;
                }else {
                    return planks;
                }
            }
            nextStockKbar = stockKbar;
        }
        return planks;
    }

    public List<StockKbar> getStockKbarsByKBarDate(String kbarDate){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setKbarDate(kbarDate);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)){
                return null;
            }
            return stockKbars;
        }catch (Exception e){
            return null;
        }
    }



    public void getHistoryBlockInfoTest(){
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(new HistoryBlockInfoQuery());
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            if(!historyBlockInfo.getBlockCode().startsWith("885")){
                continue;
            }
            HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
            query.setBlockCode(historyBlockInfo.getBlockCode());
            query.addOrderBy("trade_date", Sort.SortType.ASC);
            List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
            String  blockCodeStr = historyBlockInfo.getBlockCode()+"_"+historyBlockInfo.getMarketDate();
            if(!CollectionUtils.isEmpty(historyBlockStocks)){
               /* String tradeDate = historyBlockStocks.get(0).getTradeDate();
                if(!tradeDate.equals(historyBlockInfo.getMarketDate())){
                    System.out.println(blockCodeStr);
                }*/
            }else{
                System.out.println(blockCodeStr);
            }

            /*String startTradeDate = historyBlockStocks.get(0).getTradeDate();
            String endTradeDate = historyBlockStocks.get(historyBlockStocks.size()-1).getTradeDate();
            blockCodeStr = blockCodeStr+"==="+startTradeDate+"==="+endTradeDate;
            System.out.println(blockCodeStr);*/


        }
    }
}
