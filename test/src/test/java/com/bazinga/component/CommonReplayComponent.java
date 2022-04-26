package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.*;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.model.suport.F10Cates;
import com.tradex.util.TdxHqUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommonReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private RedisMoniorService redisMoniorService;


    public Map<String, List<PlankDayDTO>> getPlankDayInfoMap(){

        Map<String,List<PlankDayDTO>> resultMap = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)|| stockKbars.size()<2){
                continue;
            }
            for (int i = 1; i < stockKbars.size(); i++) {
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                if(StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){

                    Integer sealType = StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)?1:0;
                    List<PlankDayDTO> plankDayDTOList = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k-> new ArrayList<>());
                    plankDayDTOList.add(new PlankDayDTO(stockKbar.getStockCode(),sealType,stockKbar.getTradeAmount()));
                }
            }
        }
        return resultMap;


    }


    public Map<String, Integer> endPlanksMap(List<CirculateInfo> circulateInfos){
        Map<String, Integer> map = new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            //query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preStockKbar!=null){
                    boolean historyPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(historyPrice){
                        Integer counts = map.get(stockKbar.getKbarDate());
                        if(counts==null){
                            counts = 0;
                        }
                        counts = counts+1;
                        map.put(stockKbar.getKbarDate(),counts);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return map;
    }


    public Map<String,Integer> initAmountRankMap(String kbarDateFrom ,String kbarDateTo){
        Map<String, Integer > rankMap = new HashMap<>();

        String key = "stock_rank_map" + kbarDateFrom +SymbolConstants.UNDERLINE + kbarDateTo;
        RedisMonior redisMonior = redisMoniorService.getByRedisKey(key);
        if(redisMonior !=null){
            JSONObject jsonObject = JSONObject.parseObject(redisMonior.getRedisValue());
            jsonObject.forEach((jsonKey,value)->{
                rankMap.put(jsonKey, Integer.valueOf(value.toString()));
            });
        }else {
            TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
            tradeQuery.setTradeDateFrom(DateUtil.parseDate(kbarDateFrom,DateUtil.yyyyMMdd));
            tradeQuery.setTradeDateTo(DateUtil.parseDate(kbarDateTo,DateUtil.yyyyMMdd));
            List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);

            List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
            Map<String,Long> circulateMap = new HashMap<>();
            for (CirculateInfo circulateInfo : circulateInfos) {
                circulateMap.put(circulateInfo.getStockCode(),circulateInfo.getCirculate());
            }

            for (TradeDatePool tradeDatePool : tradeDatePools) {
                String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                StockKbarQuery query = new StockKbarQuery();
                query.setKbarDate(kbarDate);
                List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<100){
                    continue;
                }
                Map<String ,BigDecimal> amountMap = new HashMap<>();
                for (StockKbar stockKbar : stockKbarList) {
                    Long circulate = circulateMap.get(stockKbar.getStockCode());
                    if(circulate == null){
                        continue;
                    }
                    amountMap.put(stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE + stockKbar.getStockCode(),stockKbar.getClosePrice().multiply(new BigDecimal(circulate.toString())));
                }

                Map<String, BigDecimal> sortMap = SortUtil.sortByValue(amountMap);

                int rank = 1;
                for (Map.Entry<String, BigDecimal> entry : sortMap.entrySet()) {
                    //  log.info("amount{}",entry.getValue());
                    rankMap.put(entry.getKey(),rank);
                    rank++;
                }
            }
            redisMonior = new RedisMonior();
            redisMonior.setRedisKey(key);
            redisMonior.setRedisValue(JSONObject.toJSONString(rankMap));
            redisMoniorService.save(redisMonior);
        }
        return rankMap;

    }



    public Map<String, OpenCompeteDTO> get300CompeteInfo() {

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> item.getStockCode().startsWith("3")).collect(Collectors.toList());


        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        Map<String, List<OpenCompeteDTO>> tempMap = new HashMap<>();

        for (CirculateInfo circulateInfo : circulateInfos) {
           /* if(!"300945".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210501");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            for (int i = 1; i < kbarList.size() - 1; i++) {
                StockKbar buyStockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i - 1);
                StockKbar sellStockKbar = kbarList.get(i + 1);
                if (!StockKbarUtil.isUpperPrice(buyStockKbar, preStockKbar)) {
                    continue;
                }

                OpenCompeteDTO openCompeteDTO = new OpenCompeteDTO();
                openCompeteDTO.setStockCode(buyStockKbar.getStockCode());
                openCompeteDTO.setRate(PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(buyStockKbar.getClosePrice()), buyStockKbar.getClosePrice()));

                List<OpenCompeteDTO> list = tempMap.get(buyStockKbar.getKbarDate());
                if (CollectionUtils.isEmpty(list)) {
                    list = new ArrayList<>();
                    tempMap.put(buyStockKbar.getKbarDate(), list);
                }
                list.add(openCompeteDTO);
            }
        }
        Map<String, OpenCompeteDTO> resultMap = new HashMap<>();
        for (Map.Entry<String, List<OpenCompeteDTO>> entry : tempMap.entrySet()) {
            String kbarDate = entry.getKey();
            List<OpenCompeteDTO> list = entry.getValue();
            List<OpenCompeteDTO> sortedList = list.stream().sorted(Comparator.comparing(OpenCompeteDTO::getRate)).collect(Collectors.toList());
            sortedList = Lists.reverse(sortedList);
            for (int i = 0; i < sortedList.size(); i++) {
                OpenCompeteDTO openCompeteDTO = sortedList.get(i);
                openCompeteDTO.setCompeteNum(i + 1);
                resultMap.put(openCompeteDTO.getStockCode() + SymbolConstants.UNDERLINE + kbarDate, openCompeteDTO);
            }

        }


        return resultMap;
    }

    public Map<String, BigDecimal> initShOpenRateMap() {

        Map<String, BigDecimal> resultMap = new HashMap<>();

        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.setKbarDateFrom("20191115");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> kbarList = stockKbarService.listByCondition(query);
        for (int i = 1; i < kbarList.size(); i++) {
            StockKbar preStockKbar = kbarList.get(i - 1);
            StockKbar stockKbar = kbarList.get(i);
            resultMap.put(stockKbar.getKbarDate(), PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice()));
        }
        return resultMap;
    }


    public Map<String,Integer> getPlankHighCountMap(){
        Map<String,Integer> resultMap = new HashMap<>();

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210415");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<20){
                continue;
            }
            for (int i = 8; i < kbarList.size(); i++) {
                int plank = PlankHighUtil.calSerialsPlank(kbarList.subList(i - 8, i + 1));
                StockKbar stockKbar = kbarList.get(i);
                if(plank==2){
                    String mapKey = stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE + 2;
                    Integer count = resultMap.get(mapKey);
                    if(count==null){
                        resultMap.put(mapKey,1);
                    }else {
                        count = count+1;
                        resultMap.put(mapKey,count);
                    }
                }
                if(plank>=3){
                    String mapKey = stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE + 3;
                    Integer count = resultMap.get(mapKey);
                    if(count==null){
                        resultMap.put(mapKey,1);
                    }else {
                        count = count+1;
                        resultMap.put(mapKey,count);
                    }
                }
            }
        }
        return resultMap;
    }


    public Map<String, IndexRateDTO> initIndexRateMap(String indexCode) {

        Map<String, IndexRateDTO> resultMap = new HashMap<>();

        List<KBarDTO> list = Lists.newArrayList();
        for (int i = 0; i < 250; i++) {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, indexCode, i, 1);
            KBarDTO kBarDTO = KBarDTOConvert.convertSZKBar(dataTable);
            list.add(kBarDTO);
        }

        list = Lists.reverse(list);
        for (int i = 1; i < list.size() - 1; i++) {
            KBarDTO preKbar = list.get(i - 1);
            KBarDTO currentKbar = list.get(i);
            KBarDTO keyKbar = list.get(i + 1);

            BigDecimal highRate = PriceUtil.getPricePercentRate(currentKbar.getHighestPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
            BigDecimal closeRate = PriceUtil.getPricePercentRate(currentKbar.getEndPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
            resultMap.put(keyKbar.getDateStr(), new IndexRateDTO(closeRate, highRate));
        }
        return resultMap;
    }

    public void replay() {

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> !item.getStockCode().startsWith("3")).collect(Collectors.toList());

        Map<String, List<PlankTimeDTO>> resultMap = new HashMap<>();
        for (CirculateInfo circulateInfo : circulateInfos) {
     /*       if(!"603963".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210418");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            if (CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size() < 7) {
                continue;
            }
            for (int i = 7; i < stockKbarList.size()-1; i++) {

                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                StockKbar prestockKbar = stockKbarList.get(i - 1);
                if (!StockKbarUtil.isHighUpperPrice(stockKbar, prestockKbar)) {
                    continue;
                }
                if (!PriceUtil.isSuddenPrice(stockKbar.getLowPrice(), prestockKbar.getClosePrice())) {
                    continue;
                }

                int plank = PlankHighUtil.calSerialsPlank(stockKbarList.subList(i - 7, i));
                if (plank > 1) {

                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());

                    BigDecimal lowPrice = list.get(0).getTradePrice();
                    boolean isSuddenUpper = false;
                    String plankTime ="";
                    for (int j = 1; j < list.size(); j++) {
                        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                        if(transactionDataDTO.getTradePrice().compareTo(lowPrice)<0){
                            lowPrice = transactionDataDTO.getTradePrice();
                        }
                        if(lowPrice.subtract(new BigDecimal("0.01")).compareTo(stockKbar.getLowPrice())<=0){
                            ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j-1);
                            if(transactionDataDTO.getTradeType()==1 && stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) ==0 ){
                                if(preTransactionDataDTO.getTradeType()!=1 || stockKbar.getHighPrice().compareTo(preTransactionDataDTO.getTradePrice()) >0){
                                    isSuddenUpper = true;
                                    plankTime = transactionDataDTO.getTradeTime();
                                    break;
                                }
                            }
                        }
                    }
                    if(isSuddenUpper){
                        log.info("滿足连板地天条件 stockCode{} kbarDate{}", stockKbar.getStockCode(), stockKbar.getKbarDate());
                        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());



                        BigDecimal premium = PriceUtil.getPricePercentRate(avgPrice.subtract(stockKbar.getHighPrice()),stockKbar.getHighPrice());
                        List<PlankTimeDTO> resultlist = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k -> new ArrayList<>());
                        Integer plankTimeInteger = Integer.parseInt(plankTime.replaceAll(":",""));
                        if(resultlist.size()>=1){
                            Integer preTimeInteger = resultlist.get(0).getPlankInteger();
                            if(preTimeInteger > plankTimeInteger){
                                resultlist.add(0,new PlankTimeDTO(stockKbar.getStockCode(),plankTimeInteger,premium));
                            }
                        }else {
                            resultlist.add(new PlankTimeDTO(stockKbar.getStockCode(),plankTimeInteger,premium));
                        }
                    }

                }
            }


        }

        resultMap.forEach((kbarDate, list) -> {

            if (!CollectionUtils.isEmpty(list)) {


                log.info("kabrDate{} 地天板{}", kbarDate, JSONObject.toJSONString(list.get(0)));

            }


        });


    }

    @Data
    class PlankTimeDTO{
        private String stockCode;
        private Integer plankInteger;
        private BigDecimal premium;

        public PlankTimeDTO(String stockCode, Integer plankInteger, BigDecimal premium) {
            this.stockCode = stockCode;
            this.plankInteger = plankInteger;
            this.premium = premium;
        }
    }

}
