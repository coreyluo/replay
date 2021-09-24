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

        Map<String, Map<String,LevelDTO>> blockDropMaps = regions(blockDrops);
        Map<String, Map<String,LevelDTO>> blockRaisesMaps = regions(blockRaises);
        Map<String, Map<String,LevelDTO>> stockDropDayExchangesMaps = regions(stockDropDayExchanges);
        Map<String, Map<String,LevelDTO>> blockRate5sMaps = regions(blockRate5s);
        Map<String, Map<String,LevelDTO>> stockRaiseDayRatesMaps = regions(stockRaiseDayRates);
        bestBuy(buys,dailyMap,blockDropMaps,blockRaisesMaps,stockDropDayExchangesMaps,blockRate5sMaps,stockRaiseDayRatesMaps);


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
            list.add(dto.getProfitTotal());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            if(i<=50000) {
                datas.add(objects);
            }else{
                datas1.add(objects);
            }
        }

        String[] rowNames = {"index","大跌幅度区段","大涨涨幅区段","股票大跌日成交量区段","大涨日板块5日涨幅区段","股票大涨日涨幅区段","数量","总溢价","平均溢价"};
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
    }

    public void bestBuy(List<BestBuyDTO> buys,Map<String, HotBlockDropBuyExcelDTO> dailyMap,
                        Map<String, Map<String,LevelDTO>> blockDropMaps,Map<String, Map<String,LevelDTO>> blockRaisesMaps,Map<String, Map<String,LevelDTO>> stockDropDayExchangesMaps,
                            Map<String, Map<String,LevelDTO>> blockRate5sMaps,Map<String, Map<String,LevelDTO>> stockRaiseDayRatesMaps){
        int x=0;
        for (int i=1;i<=10;i++){
            for (int j=1;j<=10;j++){
                for (int k=1;k<=10;k++){
                    for (int m=1;m<=10;m++){
                        for (int n=1;n<=10;n++){
                            BestBuyDTO bestBuyDTO = new BestBuyDTO();
                            Map<String,LevelDTO> iMaps = blockDropMaps.get(String.valueOf(i));
                            int count = 0;
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
                                    totalProfit = totalProfit.add(dailyMap.get(key).getProfit());
                                }
                            }
                            bestBuyDTO.setReason1(String.valueOf(i));
                            bestBuyDTO.setReason2(String.valueOf(j));
                            bestBuyDTO.setReason3(String.valueOf(k));
                            bestBuyDTO.setReason4(String.valueOf(m));
                            bestBuyDTO.setReason5(String.valueOf(n));
                            bestBuyDTO.setCount(count);
                            if(count>0){
                                bestBuyDTO.setProfitTotal(totalProfit);
                                BigDecimal divide = totalProfit.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                                bestBuyDTO.setProfit(divide);
                            }
                            x++;
                            System.out.println(x);
                            buys.add(bestBuyDTO);
                        }
                    }
                }
            }
        }
    }

    public Map<String, Map<String,LevelDTO>>  regions( List<LevelDTO> blockDrops){
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

        int regions = blockDrops.size()/10;
        int i=0;
        for (LevelDTO levelDTO:blockDrops){
            i++;
            if(i<regions*1){
                map1.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*2){
                map2.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*3){
                map3.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*4){
                map4.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*5){
                map5.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*6){
                map6.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*7){
                map7.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*8){
                map8.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*9){
                map9.put(levelDTO.getKey(),levelDTO);
            }
            if(i<regions*10){
                map10.put(levelDTO.getKey(),levelDTO);
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
        return levelDTOS;
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
        List<LevelDTO> reverse = Lists.reverse(levelDTOS);
        return reverse;
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
        List<LevelDTO> reverse = Lists.reverse(levelDTOS);
        return reverse;
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
        List<LevelDTO> reverse = Lists.reverse(levelDTOS);
        return reverse;
    }

}
