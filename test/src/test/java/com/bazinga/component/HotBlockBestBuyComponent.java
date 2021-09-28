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
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
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
public class HotBlockBestBuyComponent {
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



    public void hotBlockBestBuy(List<HotBlockDropBuyExcelDTO> dailys){
        List<BestBuyDTO> buys = Lists.newArrayList();
        List<BestBuyStockDTO> stockBuys = Lists.newArrayList();
        Map<String, BestBuyStockDTO> bestBuyStockMap = new HashMap<>();
        Map<String, HotBlockDropBuyExcelDTO> dailyMap = new HashMap<>();
        for (HotBlockDropBuyExcelDTO dto:dailys){
            String key = dto.getStockCode()+dto.getTradeDate();
            dailyMap.put(key,dto);
        }
        List<LevelDTO> blockDrops = splitRegionBlockDropRate(dailys);
        List<LevelDTO> blockRaises = splitRegionBlockRaiseRate(dailys);
        List<LevelDTO> stockDropDayExchanges = splitRegionDropDayExchange(dailys);
        List<LevelDTO> blockRate5s = splitRegionRaiseDayBlockRate5(dailys);
        List<LevelDTO> stockRaiseDayRates = splitRegionRaiseDayRate(dailys);

        Map<String, String> regionNameMap = new HashMap<>();
        Map<String, Map<String,LevelDTO>> blockDropMaps = regions(blockDrops,regionNameMap,"板块大跌日跌幅");
        Map<String, Map<String,LevelDTO>> blockRaisesMaps = regions(blockRaises,regionNameMap,"板块大涨日涨幅");
        Map<String, Map<String,LevelDTO>> stockDropDayExchangesMaps = regions(stockDropDayExchanges,regionNameMap,"板块大跌日单个股票成交量");
        Map<String, Map<String,LevelDTO>> blockRate5sMaps = regions(blockRate5s,regionNameMap,"板块大涨日收盘板块5日涨幅");
        Map<String, Map<String,LevelDTO>> stockRaiseDayRatesMaps = regions(stockRaiseDayRates,regionNameMap,"板块大涨日单个股票收盘涨幅");
        bestBuy(buys,dailyMap,bestBuyStockMap,regionNameMap,blockDropMaps,blockRaisesMaps,stockDropDayExchangesMaps,blockRate5sMaps,stockRaiseDayRatesMaps);


        List<Object[]> datas = Lists.newArrayList();
        List<Object[]> datas1 = Lists.newArrayList();
        int i= 0;
        for(BestBuyDTO dto:buys){
            i++;
            List<Object> list = new ArrayList<>();
            list.add(dto.getReason1());
            list.add(dto.getReason1());
            list.add(dto.getReason2());
            list.add(dto.getReason3());
            list.add(dto.getReason4());
            list.add(dto.getReason5());
            list.add(dto.getCount());
            list.add(dto.getRedCount());
            list.add(dto.getGreenCount());
            list.add(dto.getRedRate());
            list.add(dto.getProfitTotal());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            if(i<=50000) {
                datas.add(objects);
            }else{
                datas1.add(objects);
            }
        }

        String[] rowNames = {"index","大跌幅度区段","大涨涨幅区段","股票大跌日成交量区段","大涨日板块5日涨幅区段","股票大涨日涨幅区段","数量","赚钱数量","亏钱数量","赚亏比例","总溢价","平均溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("最佳因素1",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("最佳因素1");
        }catch (Exception e){
            log.info(e.getMessage());
        }

        PoiExcelUtil poiExcelUtil2 = new PoiExcelUtil("最佳因素2",rowNames,datas1);
        try {
            poiExcelUtil2.exportExcelUseExcelTitle("最佳因素2");
        }catch (Exception e){
            log.info(e.getMessage());
        }


        bestBuyStockMap = openRateLevel(bestBuyStockMap);
        List<Object[]> dataStocks = Lists.newArrayList();
        for(String key:bestBuyStockMap.keySet()){
            BestBuyStockDTO dto = bestBuyStockMap.get(key);
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getTradeDate());
            list.add(dto.getReason1());
            list.add(dto.getReason2());
            list.add(dto.getReason3());
            list.add(dto.getReason4());
            list.add(dto.getReason5());
            list.add(dto.getBeforePlankDay3());
            list.add(dto.getBuyDayOpenRate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            dataStocks.add(objects);
        }

        String[] rowNameStocks = {"index","stockCode","tradeDate","板块大跌日跌幅","板块大涨日涨幅","板块大跌日单个股票成交量","板块大涨日收盘板块5日涨幅","板块大涨日单个股票收盘涨幅","3天前是否涨停过","买入日开盘涨幅","溢价"};
        PoiExcelUtil poiExcelUtilStock = new PoiExcelUtil("最佳因素个股",rowNameStocks,dataStocks);
        try {
            poiExcelUtilStock.exportExcelUseExcelTitle("最佳因素个股");
        }catch (Exception e){
            log.info(e.getMessage());
        }

    }

    public Map<String,BestBuyStockDTO> openRateLevel(Map<String, BestBuyStockDTO> map){
        Map<String,BestBuyStockDTO> level30Map = new HashMap<>();
        Map<String, List<BestBuyStockDTO>> dateMap = new HashMap<>();
        for (String key:map.keySet()){
            BestBuyStockDTO bestBuyStockDTO = map.get(key);
            List<BestBuyStockDTO> stocks = dateMap.get(bestBuyStockDTO.getTradeDate());
            if(CollectionUtils.isEmpty(stocks)){
                stocks = Lists.newArrayList();
                dateMap.put(bestBuyStockDTO.getTradeDate(),stocks);
            }
            stocks.add(bestBuyStockDTO);
        }
        for (String key:dateMap.keySet()){
            List<BestBuyStockDTO> dtos = dateMap.get(key);
            BestBuyStockDTO.buyDayOpenRateSort(dtos);
            int i = 0;
            for(BestBuyStockDTO dto:dtos){
                i++;
                if(i<=30){
                    level30Map.put(dto.getStockCode()+dto.getTradeDate(),dto);
                }
            }
        }
        return level30Map;

    }

    public void bestBuy(List<BestBuyDTO> buys,Map<String, HotBlockDropBuyExcelDTO> dailyMap,Map<String, BestBuyStockDTO> bestBuyStockMap,Map<String, String> regionNameMap,
                        Map<String, Map<String,LevelDTO>> blockDropMaps,Map<String, Map<String,LevelDTO>> blockRaisesMaps,Map<String, Map<String,LevelDTO>> stockDropDayExchangesMaps,
                            Map<String, Map<String,LevelDTO>> blockRate5sMaps,Map<String, Map<String,LevelDTO>> stockRaiseDayRatesMaps){
        int x=0;
        for (int i=1;i<=10;i++){
            for (int j=1;j<=10;j++){
                for (int k=1;k<=10;k++){
                    for (int m=1;m<=10;m++){
                        for (int n=1;n<=10;n++){
                            String iName = regionNameMap.get(i + "板块大跌日跌幅");
                            String jName = regionNameMap.get(j + "板块大涨日涨幅");
                            String kName = regionNameMap.get(k + "板块大跌日单个股票成交量");
                            String mName = regionNameMap.get(m + "板块大涨日收盘板块5日涨幅");
                            String nName = regionNameMap.get(n + "板块大涨日单个股票收盘涨幅");

                            BestBuyDTO bestBuyDTO = new BestBuyDTO();
                            Map<String,LevelDTO> iMaps = blockDropMaps.get(String.valueOf(i));
                            int count = 0;
                            int redCount = 0;
                            int greenCount = 0;
                            BigDecimal totalProfit = BigDecimal.ZERO;
                            for (String key:iMaps.keySet()) {
                                Map<String, LevelDTO> jMaps = blockRaisesMaps.get(String.valueOf(j));
                                Map<String, LevelDTO> kMaps = stockDropDayExchangesMaps.get(String.valueOf(k));
                                Map<String, LevelDTO> mMaps = blockRate5sMaps.get(String.valueOf(m));
                                Map<String, LevelDTO> nMaps = stockRaiseDayRatesMaps.get(String.valueOf(n));
                                LevelDTO jDto = jMaps.get(key);
                                LevelDTO kDto = kMaps.get(key);
                                LevelDTO mDto = mMaps.get(key);
                                LevelDTO nDto = nMaps.get(key);
                                if(jDto!=null&&kDto!=null&&mDto!=null&&nDto!=null){
                                    count++;
                                    if(dailyMap.get(key).getProfit().compareTo(BigDecimal.ZERO)==1){
                                        redCount++;
                                    }else{
                                        greenCount++;
                                    }
                                    totalProfit = totalProfit.add(dailyMap.get(key).getProfit());
                                }
                            }
                            bestBuyDTO.setReason1(String.valueOf(i));
                            bestBuyDTO.setReason2(String.valueOf(j));
                            bestBuyDTO.setReason3(String.valueOf(k));
                            bestBuyDTO.setReason4(String.valueOf(m));
                            bestBuyDTO.setReason5(String.valueOf(n));
                            bestBuyDTO.setCount(count);
                            bestBuyDTO.setGreenCount(greenCount);
                            bestBuyDTO.setRedCount(redCount);
                            if(count>0){
                                bestBuyDTO.setProfitTotal(totalProfit);
                                BigDecimal divide = totalProfit.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                                bestBuyDTO.setProfit(divide);
                                bestBuyDTO.setRedRate(new BigDecimal(redCount).divide(new BigDecimal(count),4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)));
                            }
                            if(count>10 && bestBuyDTO.getProfit().compareTo(new BigDecimal("2.5"))==1 && bestBuyDTO.getRedRate().compareTo(new BigDecimal("50"))==1) {
                                buys.add(bestBuyDTO);
                                //if(i==10 && j==8 && k==3 && m==1 && n==10) {
                                    for (String key : iMaps.keySet()) {
                                        Map<String, LevelDTO> jMaps = blockRaisesMaps.get(String.valueOf(j));
                                        Map<String, LevelDTO> kMaps = stockDropDayExchangesMaps.get(String.valueOf(k));
                                        Map<String, LevelDTO> mMaps = blockRate5sMaps.get(String.valueOf(m));
                                        Map<String, LevelDTO> nMaps = stockRaiseDayRatesMaps.get(String.valueOf(n));
                                        LevelDTO jDto = jMaps.get(key);
                                        LevelDTO kDto = kMaps.get(key);
                                        LevelDTO mDto = mMaps.get(key);
                                        LevelDTO nDto = nMaps.get(key);
                                        if (jDto != null && kDto != null && mDto != null && nDto != null) {
                                            BestBuyStockDTO buyStockDTO = new BestBuyStockDTO();
                                            buyStockDTO.setStockCode(dailyMap.get(key).getStockCode());
                                            buyStockDTO.setTradeDate(dailyMap.get(key).getTradeDate());
                                            buyStockDTO.setReason1(iName);
                                            buyStockDTO.setReason2(jName);
                                            buyStockDTO.setReason3(kName);
                                            buyStockDTO.setReason4(mName);
                                            buyStockDTO.setReason5(nName);
                                            buyStockDTO.setBeforePlankDay3(dailyMap.get(key).getBeforePlankDay3());
                                            buyStockDTO.setBuyDayOpenRate(dailyMap.get(key).getBuyDayOpenRate());
                                            buyStockDTO.setProfit(dailyMap.get(key).getProfit());
                                            BestBuyStockDTO mapValue = bestBuyStockMap.get(buyStockDTO.getStockCode() + buyStockDTO.getTradeDate());
                                            if (mapValue == null) {
                                                bestBuyStockMap.put(buyStockDTO.getStockCode() + buyStockDTO.getTradeDate(), buyStockDTO);
                                            }
                                        }
                                    }
                                //}
                            }
                        }
                    }
                }
            }
        }
    }

    public Map<String, Map<String,LevelDTO>>  regions( List<LevelDTO> blockDrops,Map<String,String> regionNameMap,String title){
        Map<String, Map<String,LevelDTO>> levelMap = new HashMap<>();
        Map<String,LevelDTO> map1 = new HashMap<>();
        Map<String,LevelDTO> map2 = new HashMap<>();
        Map<String,LevelDTO> map3 = new HashMap<>();
        Map<String,LevelDTO> map4 = new HashMap<>();
        Map<String,LevelDTO> map5 = new HashMap<>();
        Map<String,LevelDTO> map6 = new HashMap<>();
        Map<String,LevelDTO> map7 = new HashMap<>();
        Map<String,LevelDTO> map8 = new HashMap<>();
        Map<String,LevelDTO> map9 = new HashMap<>();
        Map<String,LevelDTO> map10 = new HashMap<>();

        BigDecimal startDate = null;

        BigDecimal region1Date = null;
        BigDecimal region2Date = null;
        BigDecimal region3Date = null;
        BigDecimal region4Date = null;
        BigDecimal region5Date = null;
        BigDecimal region6Date = null;
        BigDecimal region7Date = null;
        BigDecimal region8Date = null;
        BigDecimal region9Date = null;
        BigDecimal region10Date = null;

        int regions = blockDrops.size()/10;
        int i=0;
        for (LevelDTO levelDTO:blockDrops){
            i++;
            if(i==1){
                startDate = levelDTO.getRate();
            }
            if(i<=regions*1){
                region1Date = levelDTO.getRate();
                map1.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region1Date.compareTo(levelDTO.getRate())==0){
                    map1.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*2){
                region2Date = levelDTO.getRate();
                map2.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region2Date.compareTo(levelDTO.getRate())==0){
                    map2.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*3){
                region3Date = levelDTO.getRate();
                map3.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region3Date.compareTo(levelDTO.getRate())==0){
                    map3.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*4){
                region4Date = levelDTO.getRate();
                map4.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region4Date.compareTo(levelDTO.getRate())==0){
                    map4.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*5){
                region5Date = levelDTO.getRate();
                map5.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region5Date.compareTo(levelDTO.getRate())==0){
                    map5.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*6){
                region6Date = levelDTO.getRate();
                map6.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region6Date.compareTo(levelDTO.getRate())==0){
                    map6.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*7){
                region7Date = levelDTO.getRate();
                map7.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region7Date.compareTo(levelDTO.getRate())==0){
                    map7.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*8){
                region8Date = levelDTO.getRate();
                map8.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region8Date.compareTo(levelDTO.getRate())==0){
                    map8.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*9){
                region9Date = levelDTO.getRate();
                map9.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region9Date.compareTo(levelDTO.getRate())==0){
                    map9.put(levelDTO.getKey(),levelDTO);
                }
            }
            if(i<=regions*10){
                region10Date = levelDTO.getRate();
                map10.put(levelDTO.getKey(),levelDTO);
            }else{
                if(region10Date.compareTo(levelDTO.getRate())==0){
                    map10.put(levelDTO.getKey(),levelDTO);
                }
            }
        }
        levelMap.put("1",map1);
        levelMap.put("2",map2);
        levelMap.put("3",map3);
        levelMap.put("4",map4);
        levelMap.put("5",map5);
        levelMap.put("6",map6);
        levelMap.put("7",map7);
        levelMap.put("8",map8);
        levelMap.put("9",map9);
        levelMap.put("10",map10);

        regionNameMap.put("1"+title,title+startDate.toString()+"至"+region1Date.toString());
        regionNameMap.put("2"+title,title+startDate.toString()+"至"+region2Date.toString());
        regionNameMap.put("3"+title,title+startDate.toString()+"至"+region3Date.toString());
        regionNameMap.put("4"+title,title+startDate.toString()+"至"+region4Date.toString());
        regionNameMap.put("5"+title,title+startDate.toString()+"至"+region5Date.toString());
        regionNameMap.put("6"+title,title+startDate.toString()+"至"+region6Date.toString());
        regionNameMap.put("7"+title,title+startDate.toString()+"至"+region7Date.toString());
        regionNameMap.put("8"+title,title+startDate.toString()+"至"+region8Date.toString());
        regionNameMap.put("9"+title,title+startDate.toString()+"至"+region9Date.toString());
        regionNameMap.put("10"+title,title+startDate.toString()+"至"+region10Date.toString());

        return levelMap;
    }



    //大跌幅度
    public List<LevelDTO>  splitRegionBlockDropRate(List<HotBlockDropBuyExcelDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyExcelDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(hotBlockDropBuyDTO.getBlockDropRate());
            if(levelDTO.getRate()!=null) {
                levelDTOS.add(levelDTO);
            }
        }
        Collections.sort(levelDTOS);
        List<LevelDTO> reverse = Lists.reverse(levelDTOS);
        return reverse;
    }
    //大涨幅度
    public List<LevelDTO>  splitRegionBlockRaiseRate(List<HotBlockDropBuyExcelDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyExcelDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(hotBlockDropBuyDTO.getBlockRaiseRate());
            if(levelDTO.getRate()!=null) {
                levelDTOS.add(levelDTO);
            }
        }
        Collections.sort(levelDTOS);
        List<LevelDTO> reverse = Lists.reverse(levelDTOS);
        return reverse;
    }

    //大跌日换手
    public List<LevelDTO>  splitRegionDropDayExchange(List<HotBlockDropBuyExcelDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyExcelDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(new BigDecimal(hotBlockDropBuyDTO.getDropDayExchange()));
            if(levelDTO.getRate()!=null) {
                levelDTOS.add(levelDTO);
            }
        }
        Collections.sort(levelDTOS);
        return levelDTOS;
    }

    //股票大涨日涨幅
    public List<LevelDTO>  splitRegionRaiseDayRate(List<HotBlockDropBuyExcelDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyExcelDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(hotBlockDropBuyDTO.getRaiseDayRate());
            if(levelDTO.getRate()!=null) {
                levelDTOS.add(levelDTO);
            }
        }
        Collections.sort(levelDTOS);
        return levelDTOS;
    }

    //大涨日板块5日涨幅
    public List<LevelDTO>  splitRegionRaiseDayBlockRate5(List<HotBlockDropBuyExcelDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyExcelDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(hotBlockDropBuyDTO.getRaiseDayBlockRate5());
            if(levelDTO.getRate()!=null) {
                levelDTOS.add(levelDTO);
            }
        }
        Collections.sort(levelDTOS);
        return levelDTOS;
    }

}
