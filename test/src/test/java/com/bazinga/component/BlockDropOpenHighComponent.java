package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockDaDieBestDTO;
import com.bazinga.dto.BlockStockBestDTO;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.TbondUseMainDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
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
public class BlockDropOpenHighComponent {
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
    private TransferableBondInfoService transferableBondInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private BlockKbarSelfService blockKbarSelfService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    public void chaoDie(){
        List<TbondUseMainDTO> dtos = choaDieInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(TbondUseMainDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","主板股票代码","主板股票名称","买入时间","买入时涨速","买入时价格","买入前总卖","买入前成交","买出时间","买出时涨速","买出时价格","买入前10跳平均成交","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板块大涨回调低吸",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板块大涨回调低吸");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<TbondUseMainDTO> choaDieInfo(){
        List<TbondUseMainDTO> list = new ArrayList<>();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-09-01",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("2021-11-10",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<TransferableBondInfo> tbInfos = transferableBondInfoService.listByCondition(new TransferableBondInfoQuery());
        for (TransferableBondInfo tbInfo:tbInfos){
            String mainCode = tbInfo.getMainCode();
            TradeDatePool preTradeDate   = null;
            for (TradeDatePool tradeDatePool:tradeDatePools){
                if(preTradeDate!=null){
                    String key = mainCode+"_"+DateUtil.format(preTradeDate.getTradeDate(),DateUtil.yyyyMMdd);
                    StockKbar stockKbar = stockKbarService.getByUniqueKey(key);
                    if(stockKbar!=null){
                        List<TbondUseMainDTO> tbondUseMainDTOS = blockDropInfo(mainCode, DateUtil.format(preTradeDate.getTradeDate(), DateUtil.yyyyMMdd), stockKbar.getClosePrice());

                    }
                }
                preTradeDate = tradeDatePool;
            }
        }
        return list;
    }


    public List<TbondUseMainDTO>  blockDropInfo(String stockCode,String tradeDate,BigDecimal preEndPrice){
        List<TbondUseMainDTO> list = new ArrayList<>();
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue  = new LimitQueue(10);
        boolean buyFlag = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            if (data.getTradeTime().equals("09:25")){
                continue;
            }
            limitQueue.offer(data);
            BigDecimal raiseRate = calRaise(limitQueue, preEndPrice);
            if((!buyFlag)&&raiseRate!=null&&raiseRate.compareTo(new BigDecimal(1))==1){
                TbondUseMainDTO tbondBuy = new TbondUseMainDTO();
                tbondBuy.setTradeTime(data.getTradeTime());
                buyFlag = true;
                list.add(tbondBuy);
            }
            if(buyFlag && raiseRate.compareTo(new BigDecimal("0.5"))==-1){
                list.get(list.size()-1).setSellTime(data.getTradeTime());
                buyFlag = false;
            }
        }
        return list;
    }
    public BigDecimal calRaise(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal preEndPrice){
        if(limitQueue==null||limitQueue.size()<2){
            return null;
        }
        ThirdSecondTransactionDataDTO lowest = null;
        ThirdSecondTransactionDataDTO last = null;
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            ThirdSecondTransactionDataDTO data = iterator.next();
            if(lowest==null||lowest.getTradePrice().compareTo(data.getTradePrice())!=-1){
                lowest = data;
            }
            last = data;
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(last.getTradePrice().subtract(lowest.getTradePrice()), preEndPrice);
        return rate;
    }
    public void calProfit(List<BlockKbarSelf> blockKbars,BlockStockBestDTO blockStockBestDTO){
        Map<String, BlockKbarSelf> selfMap = new HashMap<>();
        for (BlockKbarSelf blockKbarSelf:blockKbars){
            selfMap.put(blockKbarSelf.getKbarDate(),blockKbarSelf);
        }
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(blockStockBestDTO.getStockCode());
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        boolean flag  = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag) {
                i++;
            }
            if(i==1){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(blockStockBestDTO.getBuyKbar().getAdjOpenPrice()), blockStockBestDTO.getBuyKbar().getAdjOpenPrice());
                blockStockBestDTO.setProfitGather(rate);
            }
            if(i>=1){
                BlockKbarSelf blockKbarSelf = selfMap.get(stockKbar.getKbarDate());
                if(selfMap!=null&&blockKbarSelf.getClosePrice().compareTo(blockKbarSelf.getOpenPrice())==-1){
                    BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(blockStockBestDTO.getBuyKbar().getAdjOpenPrice()), blockStockBestDTO.getBuyKbar().getAdjOpenPrice());
                    blockStockBestDTO.setProfitBlock(rate);
                    break;
                }
                if(stockKbar.getClosePrice().compareTo(stockKbar.getOpenPrice())==-1){
                    BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(blockStockBestDTO.getBuyKbar().getAdjOpenPrice()), blockStockBestDTO.getBuyKbar().getAdjOpenPrice());
                    blockStockBestDTO.setProfitBlock(rate);
                    break;
                }
            }
            if(stockKbar.getKbarDate().equals(blockStockBestDTO.getBuyDate())){
                flag = true;
            }
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean beforeFlag = false;
        int j = 1;
        StockKbar lastStockKbar = null;
        for (StockKbar stockKbar:reverse){
            if(beforeFlag) {
                j++;
            }
            if(j==1){
                lastStockKbar = stockKbar;
            }
            if(j==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                blockStockBestDTO.setBeforeRate3(rate);
            }
            if(j==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                blockStockBestDTO.setBeforeRate10(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(blockStockBestDTO.getBuyDate())){
                beforeFlag = true;
            }
        }

    }
    public List<BlockStockBestDTO> getBuyStock(String buyDate,String blockCode,BlockDaDieBestDTO bestDTO,Map<String,CirculateInfo> circulateInfoMap){
        List<BlockStockBestDTO> bests = Lists.newArrayList();
        ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
        detailQuery.setBlockCode(blockCode);
        List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
        List<LevelDTO> gatherLevels = new ArrayList<>();
        List<LevelDTO> gather300Levels = new ArrayList<>();
        BigDecimal totalGatherAmount = BigDecimal.ZERO;
        BigDecimal totalGather300Amount = BigDecimal.ZERO;
        BigDecimal blockTotalMarketAmount = BigDecimal.ZERO;
        int detailCount  = 0;
        for (ThsBlockStockDetail detail:details){
            if (detail.getStockCode().startsWith("3")||detail.getStockCode().startsWith("60")||detail.getStockCode().startsWith("0")){
                detailCount++;
            }else{
                continue;
            }
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(detail.getStockCode(), buyDate);
            if(CollectionUtils.isEmpty(datas)){
                continue;
            }
            ThirdSecondTransactionDataDTO gatherData = datas.get(0);
            CirculateInfo circulateInfo = circulateInfoMap.get(detail.getStockCode());
            if(circulateInfo!=null) {
                BigDecimal marketAmount = new BigDecimal(circulateInfo.getCirculateZ()).multiply(gatherData.getTradePrice());
                blockTotalMarketAmount  = blockTotalMarketAmount.add(marketAmount);
            }
            BigDecimal gatherAmount = gatherData.getTradePrice().multiply(new BigDecimal(gatherData.getTradeQuantity() * 100)).setScale(2,BigDecimal.ROUND_HALF_UP);
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(detail.getStockCode());
            levelDTO.setRate(gatherAmount);
            gatherLevels.add(levelDTO);
            totalGatherAmount = totalGatherAmount.add(gatherAmount);
            if(detail.getStockCode().startsWith("30")){
                gather300Levels.add(levelDTO);
                totalGather300Amount = totalGather300Amount.add(gatherAmount);
            }
        }
        Collections.sort(gatherLevels);
        Collections.sort(gather300Levels);
        int i =0;
        for (LevelDTO levelDTO:gatherLevels){
            i++;
            if(i<=2){
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setKbarDateTo(buyDate);
                stockKbarQuery.setStockCode(levelDTO.getKey());
                stockKbarQuery.setLimit(2);
                stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);

                CirculateInfo circulateInfo = circulateInfoMap.get(levelDTO.getKey());
                BlockStockBestDTO blockStockBestDTO = new BlockStockBestDTO();
                blockStockBestDTO.setStockCode(circulateInfo.getStockCode());
                blockStockBestDTO.setStockName(circulateInfo.getStockName());
                blockStockBestDTO.setCirculate(circulateInfo.getCirculateZ());
                if(!CollectionUtils.isEmpty(stockKbars) && stockKbars.size()==2) {
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbars.get(0).getAdjOpenPrice().subtract(stockKbars.get(1).getAdjClosePrice()), stockKbars.get(1).getAdjClosePrice());
                    blockStockBestDTO.setOpenRate(openRate);
                    blockStockBestDTO.setMarketMoney(new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbars.get(0).getAdjOpenPrice()));
                }
                blockStockBestDTO.setGatherAmount(levelDTO.getRate());
                blockStockBestDTO.setBlockGatherAmount(totalGatherAmount);
                blockStockBestDTO.setBlock300GatherAmount(totalGather300Amount);
                blockStockBestDTO.setLevel(i);
                blockStockBestDTO.setChungYe(false);
                blockStockBestDTO.setBuyKbar(stockKbars.get(0));
                blockStockBestDTO.setDetailCount(detailCount);

                blockStockBestDTO.setBlockCode(bestDTO.getBlockCode());
                blockStockBestDTO.setBlockName(bestDTO.getBlockName());
                blockStockBestDTO.setHighDate(bestDTO.getHighDate());
                blockStockBestDTO.setRaiseRate(bestDTO.getRaiseRate());
                blockStockBestDTO.setHighTradeAmount(bestDTO.getHighTradeAmount());
                blockStockBestDTO.setDropTradeAmount(bestDTO.getDropTradeAmount());
                blockStockBestDTO.setBuyDate(bestDTO.getBuyDate());
                blockStockBestDTO.setDropRate(bestDTO.getDropRate());
                blockStockBestDTO.setBlockTotalMarketAmount(blockTotalMarketAmount);
                bests.add(blockStockBestDTO);
            }
        }
        int j =0;
        for (LevelDTO levelDTO:gather300Levels){
            j++;
            if(j<=2){
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setKbarDateTo(buyDate);
                stockKbarQuery.setStockCode(levelDTO.getKey());
                stockKbarQuery.setLimit(2);
                stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);

                CirculateInfo circulateInfo = circulateInfoMap.get(levelDTO.getKey());
                BlockStockBestDTO blockStockBestDTO = new BlockStockBestDTO();
                blockStockBestDTO.setStockCode(circulateInfo.getStockCode());
                blockStockBestDTO.setStockName(circulateInfo.getStockName());
                blockStockBestDTO.setCirculate(circulateInfo.getCirculateZ());
                if(!CollectionUtils.isEmpty(stockKbars) && stockKbars.size()==2) {
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbars.get(0).getAdjOpenPrice().subtract(stockKbars.get(1).getAdjClosePrice()), stockKbars.get(1).getAdjClosePrice());
                    blockStockBestDTO.setOpenRate(openRate);
                    blockStockBestDTO.setMarketMoney(new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbars.get(0).getAdjOpenPrice()));
                }
                blockStockBestDTO.setGatherAmount(levelDTO.getRate());
                blockStockBestDTO.setBlockGatherAmount(totalGatherAmount);
                blockStockBestDTO.setBlock300GatherAmount(totalGather300Amount);
                blockStockBestDTO.setLevel(j);
                blockStockBestDTO.setChungYe(true);
                blockStockBestDTO.setBuyKbar(stockKbars.get(0));
                blockStockBestDTO.setDetailCount(detailCount);

                blockStockBestDTO.setBlockCode(bestDTO.getBlockCode());
                blockStockBestDTO.setBlockName(bestDTO.getBlockName());
                blockStockBestDTO.setHighDate(bestDTO.getHighDate());
                blockStockBestDTO.setRaiseRate(bestDTO.getRaiseRate());
                blockStockBestDTO.setHighTradeAmount(bestDTO.getHighTradeAmount());
                blockStockBestDTO.setDropTradeAmount(bestDTO.getDropTradeAmount());
                blockStockBestDTO.setBuyDate(bestDTO.getBuyDate());
                blockStockBestDTO.setDropRate(bestDTO.getDropRate());
                blockStockBestDTO.setBlockTotalMarketAmount(blockTotalMarketAmount);
                bests.add(blockStockBestDTO);
            }
        }
        return bests;
    }



    public void afterHighFind(List<BlockKbarSelf> blockKbars,BlockKbarSelf buyKbar,BlockKbarSelf preBuyKbar,BlockDaDieBestDTO bestDTO){
        boolean buyFlag = false;
        BigDecimal highMoney = null;
        boolean flag = false;
        int i = 0;
        for (BlockKbarSelf blockKbar:blockKbars){
            if(flag){
                i++;
                if(i>20){
                    return;
                }
                if(!buyFlag) {
                    if (blockKbar.getHighPrice().compareTo(buyKbar.getHighPrice()) == 1) {
                        return;
                    }
                }
            }
            if(i==1){
                if(blockKbar.getTradeAmount().compareTo(highMoney)==1){
                    highMoney = blockKbar.getTradeAmount();
                }
                bestDTO.setHighTradeAmount(highMoney);
            }
            if(buyFlag){
                bestDTO.setBuyDate(blockKbar.getKbarDate());
                return;
            }
            if(i>1 && !buyFlag){
                if(bestDTO.getDropTradeAmount()!=null&&blockKbar.getClosePrice().compareTo(blockKbar.getOpenPrice())==1 && blockKbar.getTradeAmount().compareTo(bestDTO.getDropTradeAmount())==1){
                    buyFlag = true;
                }
                if(blockKbar.getClosePrice().compareTo(blockKbar.getOpenPrice())==-1) {
                    if (blockKbar.getTradeAmount().divide(bestDTO.getHighTradeAmount(), 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.8")) < 0){
                        bestDTO.setDropTradeAmount(blockKbar.getTradeAmount());
                        BigDecimal rate = PriceUtil.getPricePercentRate(blockKbar.getClosePrice().subtract(buyKbar.getHighPrice()), buyKbar.getHighPrice());
                        bestDTO.setDropRate(rate);
                    }
                }
            }

            if(buyKbar.getKbarDate().equals(blockKbar.getKbarDate())){
                flag = true;
                highMoney = preBuyKbar.getTradeAmount();
                if(buyKbar.getTradeAmount().compareTo(preBuyKbar.getTradeAmount())==1){
                    highMoney = buyKbar.getTradeAmount();
                }
            }
        }
    }


    public BigDecimal judgeRaiseRate(LimitQueue<BlockKbarSelf> limitQueue){
        if(limitQueue==null||limitQueue.size()<=5){
            return null;
        }
        Iterator<BlockKbarSelf> iterator = limitQueue.iterator();
        BlockKbarSelf lowKbar = null;
        BlockKbarSelf lastKbar = null;
        BlockKbarSelf highKbar = null;
        int i =0;
        while (iterator.hasNext()){
            i++;
            BlockKbarSelf next = iterator.next();
            if(i<limitQueue.size()){
                if(lowKbar==null||next.getLowPrice().compareTo(lowKbar.getLowPrice())==-1){
                    lowKbar = next;
                    highKbar = null;
                }
                if(highKbar==null||next.getHighPrice().compareTo(highKbar.getHighPrice())==1){
                    highKbar = next;
                }
            }
            lastKbar = next;
        }
        if(lowKbar!=null) {
            BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getHighPrice().subtract(lowKbar.getLowPrice()), lowKbar.getLowPrice());
            if(rate.compareTo(new BigDecimal(8))==1&&lastKbar.getHighPrice().compareTo(highKbar.getHighPrice())==1){
                return rate;
            }
        }
        return null;
    }


    public List<BlockKbarSelf> getBlockKbars(String blockCode){
        try {
            BlockKbarSelfQuery query = new BlockKbarSelfQuery();
            query.setBlockCode(blockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<BlockKbarSelf> blockKbarSelves = blockKbarSelfService.listByCondition(query);
            return blockKbarSelves;
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

    public static void main(String[] args) {
        LevelDTO levelDTO1 = new LevelDTO();
        levelDTO1.setRate(new BigDecimal(1));
        LevelDTO levelDTO2 = new LevelDTO();
        levelDTO2.setRate(new BigDecimal(2));
        LevelDTO levelDTO3 = new LevelDTO();
        levelDTO3.setRate(new BigDecimal(3));
        LevelDTO levelDTO4 = new LevelDTO();
        levelDTO4.setRate(new BigDecimal(4));

        List<LevelDTO> list = Lists.newArrayList();
        /*list.add(levelDTO3);
        list.add(levelDTO4);
        list.add(levelDTO2);
        list.add(levelDTO1);*/
        Collections.sort(list);
        System.out.println(11);
    }


}
