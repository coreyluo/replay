package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.BlockTotalInfoDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SocketUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockKbarSelfComponent {

    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private BlockKbarSelfService blockKbarSelfService;


    public void initBlockKbarSelf(){
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            /*if(!thsBlockInfo.getBlockCode().equals("CA9F")){
                continue;
            }*/
            System.out.println(thsBlockInfo.getBlockCode());
            String blockCode = thsBlockInfo.getBlockCode();
            String blockName = thsBlockInfo.getBlockName();
            try {
                ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
                query.setBlockCode(blockCode);
                List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(query);
                if(CollectionUtils.isEmpty(details)){
                    continue;
                }
                calKbarInfo(details,thsBlockInfo,400);

            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", blockCode, blockName,e);
            }

        }
    }

    public void calKbarInfo(List<ThsBlockStockDetail> details,ThsBlockInfo blockInfo,int limit){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateTo(DateTimeUtils.getDate235959(new Date()));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.DESC);
        tradeDatePoolQuery.setLimit(limit);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<TradeDatePool> tradeDates = Lists.reverse(tradeDatePools);
        String startDateyyyymmdd = DateUtil.format(tradeDates.get(0).getTradeDate(),DateUtil.yyyyMMdd);
        String endDateyyyymmdd = DateUtil.format(tradeDates.get(0).getTradeDate(),DateUtil.yyyyMMdd);
        Map<String, BlockTotalInfoDTO> map = new HashMap<>();
        for (ThsBlockStockDetail detail:details){
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setStockCode(detail.getStockCode());
            stockKbarQuery.setKbarDateFrom(startDateyyyymmdd);
            stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preStockKbar!=null) {
                    BlockTotalInfoDTO blockTotalInfoDTO = map.get(stockKbar.getKbarDate());
                    if(blockTotalInfoDTO==null){
                        blockTotalInfoDTO = new BlockTotalInfoDTO();
                        map.put(stockKbar.getKbarDate(),blockTotalInfoDTO);
                    }
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    BigDecimal openRateTotal = openRate;
                    if(blockTotalInfoDTO.getOpenTotalRate()!=null){
                        openRateTotal = blockTotalInfoDTO.getOpenTotalRate().add(openRate);
                    }
                    BigDecimal closeRateTotal = closeRate;
                    if(blockTotalInfoDTO.getCloseTotalRate()!=null){
                        closeRateTotal = blockTotalInfoDTO.getCloseTotalRate().add(closeRate);
                    }
                    blockTotalInfoDTO.setOpenTotalRate(openRateTotal);
                    blockTotalInfoDTO.setCloseTotalRate(closeRateTotal);
                    blockTotalInfoDTO.setCount(blockTotalInfoDTO.getCount()+1);
                    blockTotalInfoDTO.setTradeDate(stockKbar.getKbarDate());
                    BigDecimal totalExchangeAmount = stockKbar.getTradeAmount();
                    if(blockTotalInfoDTO.getTotalExchangeAmount()!=null){
                        totalExchangeAmount = blockTotalInfoDTO.getTotalExchangeAmount().add(totalExchangeAmount);
                    }
                    blockTotalInfoDTO.setTotalExchangeAmount(totalExchangeAmount);
                }
                preStockKbar = stockKbar;
            }
        }
        for (TradeDatePool tradeDatePool:tradeDates){
            String key = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            BlockTotalInfoDTO blockTotalInfoDTO = map.get(key);
            if(blockTotalInfoDTO==null){
                continue;
            }
            if(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd).equals("20211101")){
                System.out.println(11);
            }
            if(blockTotalInfoDTO.getCount()>0){
                BigDecimal avgOpenRate = blockTotalInfoDTO.getOpenTotalRate().divide(new BigDecimal(blockTotalInfoDTO.getCount()), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal avgCloseRate = blockTotalInfoDTO.getCloseTotalRate().divide(new BigDecimal(blockTotalInfoDTO.getCount()), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal tradeAmount = blockTotalInfoDTO.getTotalExchangeAmount();
                BlockKbarSelf blockKbarSelf = new BlockKbarSelf();
                blockKbarSelf.setBlockCode(blockInfo.getBlockCode());
                blockKbarSelf.setBlockName(blockInfo.getBlockName());
                blockKbarSelf.setKbarDate(key);
                blockKbarSelf.setUniqueKey(blockInfo.getBlockCode() + "_" + key);
                blockKbarSelf.setTradeAmount(tradeAmount);

                BlockKbarSelfQuery blockKbarSelfQuery = new BlockKbarSelfQuery();
                blockKbarSelfQuery.setBlockCode(blockInfo.getBlockCode());
                blockKbarSelfQuery.setKbarDateTo(key);
                blockKbarSelfQuery.setLimit(1);
                blockKbarSelfQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                List<BlockKbarSelf> blockKbars = blockKbarSelfService.listByCondition(blockKbarSelfQuery);
                BigDecimal preEndPirce = new BigDecimal("100.00");
                if(!CollectionUtils.isEmpty(blockKbars)) {
                    preEndPirce = blockKbars.get(0).getClosePrice();
                }
                BigDecimal openPrice = PriceUtil.absoluteRateToPrice(avgOpenRate, preEndPirce);
                BigDecimal closePrice = PriceUtil.absoluteRateToPrice(avgCloseRate, preEndPirce);
                BigDecimal highPrice = openPrice;
                BigDecimal lowPrice  = closePrice;
                if(closePrice.compareTo(openPrice)==1){
                    highPrice = closePrice;
                    lowPrice = openPrice;
                }
                blockKbarSelf.setOpenPrice(openPrice);
                blockKbarSelf.setClosePrice(closePrice);
                blockKbarSelf.setHighPrice(highPrice);
                blockKbarSelf.setLowPrice(lowPrice);
                blockKbarSelfService.save(blockKbarSelf);
            }
        }

    }

    public static void main(String[] args) {
        BigDecimal bigDecimal = PriceUtil.absoluteRateToPrice(new BigDecimal(2), new BigDecimal(10));
        BigDecimal rate2 = PriceUtil.absoluteRateToPrice(new BigDecimal(-2), new BigDecimal(10));
        System.out.println(111);
    }


}
