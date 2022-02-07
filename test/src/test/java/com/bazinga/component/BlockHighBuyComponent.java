package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.BlockInfoQuery;
import com.bazinga.replay.query.BlockStockDetailQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockHighBuyComponent {
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
    private BlockInfoService blockInfoService;
    @Autowired
    private BlockStockDetailService blockStockDetailService;

    public List<ThsBlockInfo> THS_BLOCK_INFOS = Lists.newArrayList();
    public Map<String,List<ThsBlockStockDetail>> THS_BLOCK_STOCK_DETAIL_MAP = new HashMap<>();

    public void jieFeiDaoInfo(List<BlockInfo> bLockInfos){
        Map<String, Integer> blockCountMap = getBlockCount();
        List<BlockRateBuyDTO> feiDaos = getFeiDao(bLockInfos);
        List<Object[]> datas = Lists.newArrayList();
        for(BlockRateBuyDTO dto:feiDaos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getBlockCode());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getTradeDate());
            list.add(dto.getCount());
            list.add(dto.getAvgRate());
            list.add(dto.getRateDay5());
            list.add(dto.getRateDay10());
            list.add(dto.getRateDay15());
            list.add(dto.getRateDay30());
            list.add(dto.getRateDay60());
            list.add(dto.getRateDay90());
            list.add(dto.getRateHighToBuy());
            list.add(blockCountMap.get(dto.getBlockCode()));
            list.add(dto.getProfit());
            list.add(dto.getProfit2());
            list.add(dto.getProfit3());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","买入日期","买入次数","买入平均涨幅","5日涨幅","10日涨幅","15日涨幅","30日涨幅","60日涨幅","90日涨幅","最高点至买入日开盘涨幅","股票数量","盈利1","盈利2","盈利3"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板块买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板块买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }



    public List<BlockRateBuyDTO> getFeiDao(List<BlockInfo> blockInfos){
        List<BlockRateBuyDTO> result = Lists.newArrayList();
        Map<String, List<BlockRateBuyDTO>> blockKbarMap = new HashMap<>();
        for (BlockInfo blockInfo:blockInfos){
           /* if(!circulateInfo.getStockCode().equals("002858")){
                continue;
            }*/
            System.out.println(blockInfo.getBlockCode());
            List<KBarDTO> stockKbars = getBlockKbars(blockInfo.getBlockCode());
            blockKbarInfo(stockKbars,blockInfo,blockKbarMap);
        }
        int i = 0;
        for (String key:blockKbarMap.keySet()){
            Date date = DateUtil.parseDate(key, DateUtil.yyyyMMdd);
            if(date.before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))){
                continue;
            }
            i++;
            System.out.println(key+"===="+i);
            List<BlockRateBuyDTO> blockKbarInfos = blockKbarMap.get(key);
            List<BlockRateBuyDTO> buys = blockMinuteDate(blockKbarInfos, key);
            result.addAll(buys);
        }
        return result;
    }

    public Map<String,Integer> getBlockCount(){
        Map<String, Integer> map = new HashMap<>();
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        for (BlockInfo blockInfo:blockInfos){
            BlockStockDetailQuery query = new BlockStockDetailQuery();
            query.setBlockCode(blockInfo.getBlockCode());
            List<BlockStockDetail> details = blockStockDetailService.listByCondition(query);
            if(!CollectionUtils.isEmpty(details)) {
                map.put(blockInfo.getBlockCode(), details.size());
            }
        }
        return map;
    }
    public List<BlockRateBuyDTO> blockMinuteDate(List<BlockRateBuyDTO> blockKbarInfos,String tradeDateStr){
        List<BlockRateBuyDTO> list = Lists.newArrayList();
        Map<String,List<BlockRateBuyDTO>> buyMap = new HashMap<>();
        Map<String, List<BlockRateBuyDTO>> map = new HashMap<>();
        for (BlockRateBuyDTO blockKbarInfo:blockKbarInfos) {
            String tradeDate = blockKbarInfo.getTradeDate();
            BigDecimal preClosePrice = blockKbarInfo.getPreClosePrice();
            String blockCode = blockKbarInfo.getBlockCode();
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(blockCode, tradeDate);
            if (CollectionUtils.isEmpty(datas)) {
                return list;
            }
            Map<String, BlockRateBuyDTO> rateMap = new HashMap();
            for (ThirdSecondTransactionDataDTO data : datas) {
                String key = tradeDate + "_" + data.getTradeTime();
                BlockRateBuyDTO rateDto = new BlockRateBuyDTO();
                rateDto.setBlockCode(blockKbarInfo.getBlockCode());
                rateDto.setBlockName(blockKbarInfo.getBlockName());
                rateDto.setTradeDate(tradeDate);
                rateDto.setTradeTime(data.getTradeTime());
                rateDto.setTradePrice(data.getTradePrice());
                rateDto.setClosePrice(blockKbarInfo.getClosePrice());
                rateDto.setPreClosePrice(blockKbarInfo.getPreClosePrice());
                rateDto.setNextTradeDate(blockKbarInfo.getNextTradeDate());
                rateDto.setNextTwoTradeDate(blockKbarInfo.getNextTwoTradeDate());
                rateDto.setNextThreeTradeDate(blockKbarInfo.getNextThreeTradeDate());
                rateDto.setRateDay5(blockKbarInfo.getRateDay5());
                rateDto.setRateDay10(blockKbarInfo.getRateDay10());
                rateDto.setRateDay15(blockKbarInfo.getRateDay15());
                rateDto.setRateDay30(blockKbarInfo.getRateDay30());
                rateDto.setRateDay60(blockKbarInfo.getRateDay60());
                rateDto.setRateDay90(blockKbarInfo.getRateDay90());
                rateDto.setYearAgoKbar(blockKbarInfo.getYearAgoKbar());
                rateDto.setRateHighToBuy(blockKbarInfo.getRateHighToBuy());
                BigDecimal rate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(preClosePrice), preClosePrice);
                rateDto.setRate(rate);
                rateMap.put(key, rateDto);
            }
            for (String key : rateMap.keySet()) {
                List<BlockRateBuyDTO> blockRateBuyDTOS = map.get(key);
                if (blockRateBuyDTOS == null) {
                    blockRateBuyDTOS = Lists.newArrayList();
                    map.put(key, blockRateBuyDTOS);
                }
                blockRateBuyDTOS.add(rateMap.get(key));
            }
        }
        for(String key:map.keySet()){
            List<BlockRateBuyDTO> blockRateBuyDTOS = map.get(key);
            List<BlockRateBuyDTO> levelDtos = BlockRateBuyDTO.rateSort(blockRateBuyDTOS);
            int i=0;
            for(BlockRateBuyDTO blockRateBuyDTO:levelDtos){
                i++;
                if(i<=10) {
                    if(blockRateBuyDTO.getRate().compareTo(new BigDecimal(0))!=-1) {
                        List<BlockRateBuyDTO> buyDtos = buyMap.get(blockRateBuyDTO.getBlockCode());
                        if (buyDtos == null) {
                            buyDtos = Lists.newArrayList();
                            buyMap.put(blockRateBuyDTO.getBlockCode(), buyDtos);
                        }
                        buyDtos.add(blockRateBuyDTO);
                    }
                }
            }
        }
        for(String key:buyMap.keySet()){
            List<BlockRateBuyDTO> blockRateBuyDTOS = buyMap.get(key);
            BlockRateBuyDTO buy = blockRateBuyDTOS.get(0);
            BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(key, DateUtil.parseDate(buy.getNextTradeDate(), DateUtil.yyyyMMdd));
            BigDecimal avgTwoPrice = historyTransactionDataComponent.calAvgPrice(key, DateUtil.parseDate(buy.getNextTwoTradeDate(), DateUtil.yyyyMMdd));
            BigDecimal avgThreePrice = historyTransactionDataComponent.calAvgPrice(key, DateUtil.parseDate(buy.getNextThreeTradeDate(), DateUtil.yyyyMMdd));

            BigDecimal total = BigDecimal.ZERO;
            for(BlockRateBuyDTO blockRateBuyDTO:blockRateBuyDTOS){
                total = total.add(blockRateBuyDTO.getTradePrice());
            }
            BigDecimal avgBuyPrice = total.divide(new BigDecimal(blockRateBuyDTOS.size()), 2, BigDecimal.ROUND_HALF_UP);
            if(avgPrice!=null) {
                BigDecimal rate = PriceUtil.getPricePercentRate(avgPrice.subtract(avgBuyPrice), avgBuyPrice);
                buy.setProfit(rate);
            }
            if(avgTwoPrice!=null) {
                BigDecimal rate = PriceUtil.getPricePercentRate(avgTwoPrice.subtract(avgBuyPrice), avgBuyPrice);
                buy.setProfit2(rate);
            }
            if(avgThreePrice!=null) {
                BigDecimal rate = PriceUtil.getPricePercentRate(avgThreePrice.subtract(avgBuyPrice), avgBuyPrice);
                buy.setProfit3(rate);
            }
            BigDecimal avgRate = PriceUtil.getPricePercentRate(avgBuyPrice.subtract(buy.getPreClosePrice()), buy.getPreClosePrice());
            buy.setAvgRate(avgRate);
            buy.setCount(blockRateBuyDTOS.size());
            list.add(buy);
        }
        return list;
    }

    public void blockKbarInfo(List<KBarDTO> stockKbars,BlockInfo blockInfo,Map<String, List<BlockRateBuyDTO>> map){
        if(CollectionUtils.isEmpty(stockKbars)){
            return;
        }
        KBarDTO preKbar = null;
        BlockRateBuyDTO preBlockDto  = null;
        for (KBarDTO kBarDTO:stockKbars){
            BlockRateBuyDTO blockDto = new BlockRateBuyDTO();
            if(preKbar!=null){
                blockDto.setBlockCode(blockInfo.getBlockCode());
                blockDto.setBlockName(blockInfo.getBlockName());
                blockDto.setPreClosePrice(preKbar.getEndPrice());
                blockDto.setClosePrice(kBarDTO.getEndPrice());
                blockDto.setTradeDate(kBarDTO.getDateStr());
                blockDto.setPreTradeDate(preKbar.getDateStr());
                setBlockRateDayInfo(stockKbars,blockDto);
                kbarRateInfo(stockKbars,kBarDTO,blockDto);
                List<BlockRateBuyDTO> blockDtos = map.get(kBarDTO.getDateStr());
                if(blockDtos==null){
                    blockDtos = Lists.newArrayList();
                    map.put(kBarDTO.getDateStr(),blockDtos);
                }
                blockDtos.add(blockDto);
            }
            if(preBlockDto!=null){
                preBlockDto.setNextTradeDate(kBarDTO.getDateStr());
            }
            preBlockDto = blockDto;
            preKbar = kBarDTO;
        }
    }

    public void kbarRateInfo(List<KBarDTO> stockKbars,KBarDTO kBarDTO, BlockRateBuyDTO blockDTO){
        String dateStr = kBarDTO.getDateStr();
        Date date = DateUtil.parseDate(dateStr, DateUtil.yyyyMMdd);
        Date yearBefore = DateUtil.addMonths(date, -12);
        LimitQueue<KBarDTO> limitQueue6 = new LimitQueue<>(7);
        LimitQueue<KBarDTO> limitQueue11 = new LimitQueue<>(12);
        LimitQueue<KBarDTO> limitQueue16 = new LimitQueue<>(17);
        LimitQueue<KBarDTO> limitQueue31 = new LimitQueue<>(32);
        LimitQueue<KBarDTO> limitQueue61 = new LimitQueue<>(62);
        LimitQueue<KBarDTO> limitQueue91 = new LimitQueue<>(92);
        KBarDTO highKbar = null;
        for (KBarDTO dto:stockKbars){
            limitQueue6.offer(dto);
            limitQueue11.offer(dto);
            limitQueue16.offer(dto);
            limitQueue31.offer(dto);
            limitQueue61.offer(dto);
            limitQueue91.offer(dto);
           if(dto.getDate().before(yearBefore)){
               continue;
           }
            if(dto.getDateStr().equals(kBarDTO.getDateStr())){
                if(highKbar!=null) {
                    BigDecimal highToBuyRate = PriceUtil.getPricePercentRate(highKbar.getHighestPrice().subtract(dto.getStartPrice()), highKbar.getHighestPrice());
                    blockDTO.setRateHighToBuy(highToBuyRate);
                }
                if(limitQueue6.size()==7){
                    BigDecimal rate = rateInfo(limitQueue6);
                    blockDTO.setRateDay5(rate);
                }
                if(limitQueue11.size()==12){
                    BigDecimal rate = rateInfo(limitQueue11);
                    blockDTO.setRateDay10(rate);
                }
                if(limitQueue16.size()==17){
                    BigDecimal rate = rateInfo(limitQueue16);
                    blockDTO.setRateDay15(rate);
                }
                if(limitQueue31.size()==32){
                    BigDecimal rate = rateInfo(limitQueue31);
                    blockDTO.setRateDay30(rate);
                }
                if(limitQueue61.size()==62){
                    BigDecimal rate = rateInfo(limitQueue61);
                    blockDTO.setRateDay60(rate);
                }
                if(limitQueue91.size()==92){
                    BigDecimal rate = rateInfo(limitQueue91);
                    blockDTO.setRateDay90(rate);
                }
                return;
            }
            if(highKbar==null||dto.getHighestPrice().compareTo(highKbar.getHighestPrice())==1){
                highKbar = dto;
            }
        }
    }

    public BigDecimal rateInfo(LimitQueue<KBarDTO> limitQueue){
        if(limitQueue.size()<=3){
            return null;
        }
        int size = limitQueue.size();
        Iterator<KBarDTO> iterator = limitQueue.iterator();
        KBarDTO first = null;
        KBarDTO last = null;
        int i =0 ;
        while (iterator.hasNext()){
            i++;
            KBarDTO next = iterator.next();
            if(first==null){
                first = next;
            }
            if(i==size-1){
                last = next;
            }
        }
        if(last!=null&&first!=null){
            BigDecimal rate = PriceUtil.getPricePercentRate(last.getEndPrice().subtract(first.getEndPrice()), first.getEndPrice());
            return rate;
        }
        return null;
    }

    public void setBlockRateDayInfo(List<KBarDTO> stockKbars,BlockRateBuyDTO dto){
        int i = 0;
        boolean flag = false;
        for (KBarDTO kBarDTO:stockKbars){
            if(flag){
                i++;
            }
            if(i==2){
                dto.setNextTwoTradeDate(kBarDTO.getDateStr());
            }
            if(i==3){
                dto.setNextThreeTradeDate(kBarDTO.getDateStr());
                return;
            }
            if(kBarDTO.getDateStr().equals(dto.getTradeDate())){
                flag = true;
            }
        }

    }

    public void isBadManPlank(LimitQueue<StockKbar> limitQueue,BadPeopleBuyDTO badPeopleBuyDTO){
        if(limitQueue.size()<30){
            return;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        List<StockKbar> list = Lists.newArrayList();
        while (iterator.hasNext()){
            StockKbar stockKbar = iterator.next();
            list.add(stockKbar);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        int i  = 0;
        StockKbar highKbar = null;
        int highDays = 0;
        for (StockKbar stockKbar:reverse){
            if(i>=1&&i<=10){
                if(highKbar==null||stockKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())==1){
                    highKbar = stockKbar;
                    highDays = i;
                }
            }
            i++;
        }
        int j=0;
        StockKbar lowKbar  = null;
        int lowDays = 0;
        StockKbar secondLowKbar = null;
        int secondLowDays = 0;
        StockKbar lowestEndRateKbar = null;
        BigDecimal lowestEndRate = null;
        StockKbar nextKbar = null;
        int avgCount = 0;
        BigDecimal amountTotal = BigDecimal.ZERO;
        for (StockKbar stockKbar:reverse){
            if(j>0 && j<highDays){
                if(secondLowKbar==null || stockKbar.getAdjLowPrice().compareTo(secondLowKbar.getAdjLowPrice())==-1){
                    secondLowDays = j;
                    secondLowKbar = stockKbar;
                }
            }
            if(j>1 && j<=highDays){
                BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                if(lowestEndRate==null || rate.compareTo(lowestEndRate)==-1){
                    lowestEndRateKbar = nextKbar;
                    lowestEndRate = rate;
                }
            }
            if(j>0&&j<=highDays+5){
                avgCount++;
                amountTotal = amountTotal.add(stockKbar.getTradeAmount());
            }
            if(j>highDays && j<=highDays+20){
                if(stockKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())==1){
                    return;
                }
                if(lowKbar==null||stockKbar.getAdjLowPrice().compareTo(lowKbar.getAdjLowPrice())==-1){
                    lowKbar = stockKbar;
                    lowDays = j;
                }
            }
            nextKbar = stockKbar;
            j++;
        }
        if(highKbar==null||lowKbar==null||secondLowKbar==null){
            return;
        }
        BigDecimal highRate = PriceUtil.getPricePercentRate(highKbar.getAdjHighPrice().subtract(lowKbar.getAdjLowPrice()), lowKbar.getAdjLowPrice());
        BigDecimal lowRate = PriceUtil.getPricePercentRate(secondLowKbar.getAdjLowPrice().subtract(highKbar.getAdjHighPrice()), secondLowKbar.getAdjLowPrice());
        if(highRate.compareTo(new BigDecimal(20))==-1){
            return;
        }
        if(lowRate.compareTo(new BigDecimal(-8))==1){
            return;
        }
        BigDecimal avgAmount = amountTotal.divide(new BigDecimal(avgCount), 2, BigDecimal.ROUND_HALF_UP);
        badPeopleBuyDTO.setHighRate(highRate);
        badPeopleBuyDTO.setHighDays(highDays);
        badPeopleBuyDTO.setHighPrice(highKbar.getHighPrice());
        badPeopleBuyDTO.setSecondLowRate(lowRate);
        badPeopleBuyDTO.setLowPrice(lowKbar.getLowPrice());
        badPeopleBuyDTO.setLowDays(lowDays);
        badPeopleBuyDTO.setSecondLowPrice(secondLowKbar.getLowPrice());
        badPeopleBuyDTO.setSecondLowDays(secondLowDays);
        badPeopleBuyDTO.setLowDayEndRate(lowestEndRate);
        badPeopleBuyDTO.setLowDayAmount(lowestEndRateKbar.getTradeAmount());
        badPeopleBuyDTO.setAvgAmount(avgAmount);
        badPeopleBuyDTO.setBadFlag(true);
    }

    public List<FeiDaoBuyDTO> buyTime(StockKbar stockKbar,BigDecimal preEndPrice,List<ThirdSecondTransactionDataDTO> datas){
        List<FeiDaoBuyDTO> result = Lists.newArrayList();
        List<FeiDaoBuyDTO> list = Lists.newArrayList();
        boolean isPlank = false;
        boolean buyFlag = false;
        int times = 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            Integer tradeType = data.getTradeType();
            boolean isPlankS = false;
            if (tradeType == 1 && upperPrice) {
                isPlankS = true;
            }
            if(isPlank&&!isPlankS){
                buyFlag = true;
                FeiDaoBuyDTO feiDaoBuyDTO = new FeiDaoBuyDTO();
                LimitQueue<ThirdSecondTransactionDataDTO> limitQueue100 = new LimitQueue<>(100);
                feiDaoBuyDTO.setLimitQueue100(limitQueue100);
                feiDaoBuyDTO.setPlankSecond(times);
                feiDaoBuyDTO.setBuyTime(data.getTradeTime());
                list.add(feiDaoBuyDTO);
            }
            if(buyFlag){
                FeiDaoBuyDTO feiDaoBuyDTO = list.get(list.size() - 1);
                if(feiDaoBuyDTO.getLimitQueue100().size()<100) {
                    feiDaoBuyDTO.getLimitQueue100().offer(data);
                }
            }
            if(isPlankS){
                times = times+3;
                isPlank = true;
                buyFlag = false;
            }else{
                times   = 0;
                isPlank = false;
            }
        }
        for (FeiDaoBuyDTO buyDTO:list){
            buyAvgPrice(preEndPrice,buyDTO);
            if(buyDTO.getBuyAvgPrice()==null){
                continue;
            }
            result.add(buyDTO);
        }
        return result;
    }

    public void buyAvgPrice(BigDecimal preEndPrice,  FeiDaoBuyDTO buyDTO){
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue100 = buyDTO.getLimitQueue100();
        if(limitQueue100==null||limitQueue100.size()<1){
            return;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue100.iterator();
        int i = 0;
        boolean buyFlag = true;
        BigDecimal lowPrice = null;
        String firstBuyTime = null;
        while(iterator.hasNext()){
            i++;
            ThirdSecondTransactionDataDTO data = iterator.next();
            if(i<=10){
                if(data.getTradePrice().compareTo(preEndPrice)!=1){
                    if(firstBuyTime==null) {
                        firstBuyTime = data.getTradeTime();
                    }
                    buyFlag = true;
                }
            }
            if(lowPrice==null||data.getTradePrice().compareTo(lowPrice)==-1){
                lowPrice = data.getTradePrice();
            }

        }
        List<FeiDaoRateDTO> buys = Lists.newArrayList();
        BigDecimal priceTotal = BigDecimal.ZERO;
        if(buyFlag){
            BigDecimal rate = PriceUtil.getPricePercentRate(lowPrice.subtract(preEndPrice), preEndPrice);
            if(rate.compareTo(new BigDecimal(0))!=1){
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(preEndPrice);
                buys.add(buy);
                priceTotal = priceTotal.add(preEndPrice);
            }
            if(rate.compareTo(new BigDecimal(-2))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-2), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-4))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-4), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-6))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-6), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-8))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-8), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
        }
        if(buys.size()>0){
            BigDecimal divide = priceTotal.divide(new BigDecimal(buys.size()), 2, BigDecimal.ROUND_HALF_UP);
            buyDTO.setBuyAvgPrice(divide);
        }

    }




    public String plankTime(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean isPlank = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            Integer tradeType = data.getTradeType();
            if(data.getTradeTime().equals("09:25")){
                continue;
            }
            if(tradeType==1&&upperPrice){
                if(!isPlank){
                    return data.getTradeTime();
                }
            }else{
                isPlank = false;
            }
        }
        return null;
    }

    public Integer calEndPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<3){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int planks  = 0;
        int i = 0;
        for (StockKbar stockKbar:reverse){
            if(i>=2) {
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if (endUpper) {
                    planks++;
                } else {
                    return planks;
                }
            }
            i++;
            nextKbar = stockKbar;
        }
        return planks;
    }


    public BigDecimal calProfit(List<StockKbar> stockKbars, StockKbar buyKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyKbar.getAdjHighPrice()), buyKbar.getAdjHighPrice());
                return profit;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }

    public List<KBarDTO> getBlockKbars(String blockCode){
        List<KBarDTO> list = Lists.newArrayList();
        for (int i=600;i>=0;i--) {
            DataTable securityBars = TdxHqUtil.getBlockSecurityBars(KCate.DAY, blockCode, i, 1);
            KBarDTO kbar = KBarDTOConvert.convertSZKBar(securityBars);
            if(kbar!=null) {
                list.add(kbar);
            }
        }
        return list;
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

    public static void main(String[] args) {


    }


}
