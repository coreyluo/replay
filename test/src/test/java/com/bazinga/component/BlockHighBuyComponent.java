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

    public List<ThsBlockInfo> THS_BLOCK_INFOS = Lists.newArrayList();
    public Map<String,List<ThsBlockStockDetail>> THS_BLOCK_STOCK_DETAIL_MAP = new HashMap<>();

    public void jieFeiDaoInfo(){
        List<BadPeopleBuyDTO> feiDaos = getFeiDao();
        List<Object[]> datas = Lists.newArrayList();
        for(BadPeopleBuyDTO dto:feiDaos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankTime());
            list.add(dto.getBuyPrice());
            list.add(dto.getHighPrice());
            list.add(dto.getHighRate());
            list.add(dto.getHighDays());
            list.add(dto.getSecondLowPrice());
            list.add(dto.getSecondLowRate());
            list.add(dto.getSecondLowDays());
            list.add(dto.getLowPrice());
            list.add(dto.getLowDays());
            list.add(dto.getLowDayAmount());
            list.add(dto.getLowDayEndRate());
            list.add(dto.getAvgAmount());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","买入日期","可买入时间","买入价格","最高价格","最高点相比最低点涨幅","最高点相比买入点天数","次低点价格","次低点相对高点跌幅","次低点相对买入点天数","最低点价格","最低点相对买入点天使",
                "最大跌幅成交额","最大收盘跌幅","平均成交额","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("庄股1",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("庄股1");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }




    public List<BadPeopleBuyDTO> getFeiDao(){
        List<BadPeopleBuyDTO> result = Lists.newArrayList();
        Map<String, List<BlockRateBuyDTO>> blockKbarMap = new HashMap<>();
        Map<String, List<BlockRateBuyDTO>> map = new HashMap<>();
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        for (BlockInfo blockInfo:blockInfos){
           /* if(!circulateInfo.getStockCode().equals("002858")){
                continue;
            }*/
            System.out.println(blockInfo.getBlockCode());
            List<KBarDTO> stockKbars = getBlockKbars(blockInfo.getBlockCode());
            blockKbarInfo(stockKbars,blockInfo,blockKbarMap);
        }
        for (String key:blockKbarMap.keySet()){
            List<BlockRateBuyDTO> blockKbarInfos = blockKbarMap.get(key);
            blockMinuteDate(map,blockKbarInfos);
        }
        return result;
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
                List<BlockRateBuyDTO> blockDtos = map.get(kBarDTO.getDateStr());
                if(blockDtos==null){
                    blockDtos = Lists.newArrayList();
                    map.put(kBarDTO.getDateStr(),blockDtos);
                }
                blockDtos.add(blockDto);
            }
            if(blockDto!=null){
                preBlockDto.setNextTradeDate(kBarDTO.getDateStr());
            }
            preBlockDto = blockDto;
            preKbar = kBarDTO;
        }
    }
    public void blockMinuteDate(Map<String, List<BlockRateBuyDTO>> map,List<BlockRateBuyDTO> blockKbarInfos){
        for (BlockRateBuyDTO blockKbarInfo:blockKbarInfos) {
            String tradeDate = blockKbarInfo.getTradeDate();
            BigDecimal preClosePrice = blockKbarInfo.getPreClosePrice();
            String blockCode = blockKbarInfo.getBlockCode();
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(blockCode, tradeDate);
            if (CollectionUtils.isEmpty(datas)) {
                return;
            }
            Map<String, BlockRateBuyDTO> rateMap = new HashMap();
            for (ThirdSecondTransactionDataDTO data : datas) {
                String key = tradeDate + "_" + data.getTradeTime();
                BlockRateBuyDTO rateDto = new BlockRateBuyDTO();
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
        for (int i=300;i>=0;i--) {
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
        StockPlankTimeInfoDTO infoDTO1 = new StockPlankTimeInfoDTO();
        infoDTO1.setPlanks(1);
        StockPlankTimeInfoDTO infoDTO2 = new StockPlankTimeInfoDTO();
        infoDTO2.setPlanks(3);
        StockPlankTimeInfoDTO infoDTO3 = new StockPlankTimeInfoDTO();
        infoDTO3.setPlanks(3);
        StockPlankTimeInfoDTO infoDTO4 = new StockPlankTimeInfoDTO();
        infoDTO4.setPlanks(4);
        StockPlankTimeInfoDTO infoDTO5 = new StockPlankTimeInfoDTO();
        infoDTO5.setPlanks(6);
        List<StockPlankTimeInfoDTO> list = Lists.newArrayList();
        list.add(infoDTO2);
        list.add(infoDTO4);
        list.add(infoDTO1);
        list.add(infoDTO2);
        list.add(infoDTO5);
        List<StockPlankTimeInfoDTO> haha = list.subList(list.size() - 3,list.size());

        List<StockPlankTimeInfoDTO> stockPlankTimeInfoDTOS = StockPlankTimeInfoDTO.planksLevel(list);
        System.out.println(haha.get(0));
    }


}
