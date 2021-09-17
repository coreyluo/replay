package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
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
public class HotBlockDropBuyComponent {
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


    public void hotDrop(){
        List<HotBlockDropBuyDTO> dailys = Lists.newArrayList();
        Map<String, List<HotBlockDropBuyDTO>> blockDayLevelMap = getBlockDayLevel();
        for (String tradeDate:blockDayLevelMap.keySet()){
            List<HotBlockDropBuyDTO> dtos = blockDayLevelMap.get(tradeDate);
            for (HotBlockDropBuyDTO dto:dtos) {
                List<HotBlockDropBuyDTO> dragons = getDragons(dto);
                if(dragons.size()==0){
                    continue;
                }
                Map<String,HotBlockDropBuyDTO> map  = new HashMap<>();
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
                for (String key:map.keySet()){
                    dailys.add(map.get(key));
                }
            }
        }
        List<Object[]> datas = Lists.newArrayList();
        for(HotBlockDropBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
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
            list.add(dto.getBeforeRate3());
            list.add(dto.getBeforeRate5());
            list.add(dto.getBeforeRate10());
            list.add(dto.getRaiseDayPlankTime());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","交易日期","板块代码","板块名称","大涨涨幅","大涨时排名","大涨间隔日期","大跌幅度","买入日开盘板块涨幅","买入日开盘板块排名",
                "股票买入日开盘涨幅","股票买入日开盘排名","股票大涨日涨幅","股票大涨日排名",
                "股票大跌日涨幅","股票大跌日排名","买入前3日涨跌幅","买入前5日涨跌幅","买入前10日涨跌幅","大涨日上板时间","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("热门大跌",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("热门大跌");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<HotBlockDropBuyDTO> getDragons(HotBlockDropBuyDTO dto){
        List<HotBlockDropBuyDTO> datas = Lists.newArrayList();
        ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
        query.setBlockCode(dto.getBlockCode());
        List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(query);
        List<LevelDTO> buyDayRates = Lists.newArrayList();
        List<LevelDTO> dropDayRates = Lists.newArrayList();
        List<LevelDTO> raiseDayRates = Lists.newArrayList();
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
            if(buyDTO.getOpenRate()!=null) {
                datas.add(buyDTO);
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
        Collections.sort(buyDayRates);
        Collections.sort(dropDayRates);
        Collections.sort(raiseDayRates);
        for (HotBlockDropBuyDTO data:datas){
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
            }
            if(j==2){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyBeforeKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeCloseRate(rate);
                buyDTO.setDropDayRate(rate);
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


}
