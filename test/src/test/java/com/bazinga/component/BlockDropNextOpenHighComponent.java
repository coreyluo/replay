package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockRateDTO;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.StockInfoRateDTO;
import com.bazinga.dto.TbondUseMainDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.BlockDropBuyInfoDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
public class BlockDropNextOpenHighComponent {
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
    private ThsBlockKbarService thsBlockKbarService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;

    private static Map<String,List<ThsBlockStockDetail>> BLOCK_DETAIL = new HashMap<>();
    private static Map<String,ThsBlockInfo> BLOCK_INFO = new HashMap<>();
    public void chaoDie(){
        List<TbondUseMainDTO> dtos = choaDieInfo();
       /* List<Object[]> datas = Lists.newArrayList();
        for(TbondUseMainDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getMainCode());
            list.add(dto.getMainName());
            list.add(dto.getTradeDate());
            list.add(dto.getTradeTime());
            list.add(dto.getTbondTradeTime());
            list.add(dto.getBuyRate());
            list.add(dto.getTradePrice());
            list.add(dto.getBeforeSellQuantity());
            list.add(dto.getBeforeTradeDeal());
            list.add(dto.getSellTime());
            list.add(dto.getTbondSellTime());
            list.add(dto.getSellRate());
            list.add(dto.getSellPrice());
            list.add(dto.getAvg10TradeDeal());
            list.add(dto.getBuyTimeRate());
            list.add(dto.getTbondBuyTimeRate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","主板股票代码","主板股票名称","买入日期","主板买入时间","转载买入时间","买入时涨速","买入时价格","买入前总卖","买入前成交",
                "主板卖出时间","转债卖出时间","卖出时涨速","卖出时价格","买入前10跳平均成交","主板买入时候涨幅","转载买入时候涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("主板转债",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("主板转债");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }

    public List<TbondUseMainDTO> choaDieInfo() {
        blockInfo();


        List<TradeDatePool> tradeDatePools = getTradeDatePool();
        Map<String, BlockRateDTO> preBlockRateDTO = new HashMap<>();
        List<BlockRateDTO> blockRates = new ArrayList<>();
        String preTradeDate = null;
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if (preTradeDate != null) {
                for (String blockCode : BLOCK_INFO.keySet()) {
                    Map<String, StockInfoRateDTO> stockInfos = getDayAllKbars(tradeDate, preTradeDate);
                    ThsBlockInfo thsBlockInfo = BLOCK_INFO.get(blockCode);
                    List<ThsBlockStockDetail> blockDetails = BLOCK_DETAIL.get(blockCode);
                    BlockRateDTO blockRateDTO = blockRateDto(thsBlockInfo, blockDetails, stockInfos, tradeDate);
                    blockRates.add(blockRateDTO);
                }
            }
            for (BlockRateDTO blockRate:blockRates){
                BlockRateDTO preBlockRate = preBlockRateDTO.get(blockRate.getBlockCode());
                if(preBlockRate!=null
                        &&preBlockRate.getPreCloseRate().compareTo(new BigDecimal("-1"))==-1
                        &&blockRate.getOpenRate().compareTo(new BigDecimal(0))==1){
                    blockRate.setPreCloseRate(preBlockRate.getCloseRate());
                    blockRate.setPreTotalTradeAmount(preBlockRate.getTotalTradeAmount());

                }
            }
            preTradeDate = tradeDate;
        }


        return null;
    }
    public BlockRateDTO blockRateDto(ThsBlockInfo thsBlockInfo,List<ThsBlockStockDetail> blockDetails,Map<String, StockInfoRateDTO> stockInfos,String tradeDate){
        int count = 0;
        BigDecimal totalOpenRate = BigDecimal.ZERO;
        BigDecimal totalCloseRate = BigDecimal.ZERO;
        BigDecimal gatherTradeAmount = BigDecimal.ZERO;
        BigDecimal totalTradeAmount = BigDecimal.ZERO;
        for (ThsBlockStockDetail detail:blockDetails){
            StockInfoRateDTO stockInfo = stockInfos.get(detail.getStockCode());
            if(stockInfo!=null){
                if(stockInfo.getOpenRate()!=null&&stockInfo.getCloseRate()!=null){
                    count++;
                    totalOpenRate = totalOpenRate.add(stockInfo.getOpenRate());
                    totalCloseRate  = totalCloseRate.add(stockInfo.getCloseRate());
                    totalTradeAmount = totalTradeAmount.add(stockInfo.getTradeAmount());
                }
                if(stockInfo.getGatherTradeAmount()!=null) {
                    gatherTradeAmount = gatherTradeAmount.add(stockInfo.getGatherTradeAmount());
                }
            }
        }
        BlockRateDTO blockRateDTO = new BlockRateDTO();
        blockRateDTO.setBlockCode(thsBlockInfo.getBlockCode());
        blockRateDTO.setBlockName(thsBlockInfo.getBlockName());
        blockRateDTO.setTradeDate(tradeDate);
        blockRateDTO.setGatherTradeAmount(gatherTradeAmount);
        blockRateDTO.setTotalTradeAmount(totalTradeAmount);
        if(count>0){
            BigDecimal avgOpenRate = totalOpenRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal avgCloseRate = totalCloseRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
            blockRateDTO.setOpenRate(avgOpenRate);
            blockRateDTO.setCloseRate(avgCloseRate);
        }
        return blockRateDTO;
    }

    public Map<String, StockInfoRateDTO> getDayAllKbars(String tradeDate,String preTradeDate){
        Map<String,StockInfoRateDTO> map  = new HashMap<>();
        Map<String, StockKbar> kbarMap = new HashMap<>();
        Map<String, StockKbar> preKbarMap = new HashMap<>();
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate(tradeDate);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        for (StockKbar stockKbar:stockKbars){
            kbarMap.put(stockKbar.getStockCode(),stockKbar);
        }
        StockKbarQuery preQuery = new StockKbarQuery();
        preQuery.setKbarDate(preTradeDate);
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preQuery);
        for (StockKbar stockKbar:preStockKbars){
            preKbarMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (String stockCode:kbarMap.keySet()){
            StockKbar preStockKbar = preKbarMap.get(stockCode);
            StockKbar stockKbar = kbarMap.get(stockCode);
            if (preStockKbar!=null && stockKbar!=null){
                StockInfoRateDTO stockRateDto = new StockInfoRateDTO();
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                stockRateDto.setStockCode(stockCode);
                stockRateDto.setStockName(stockKbar.getStockName());
                stockRateDto.setOpenRate(openRate);
                stockRateDto.setCloseRate(closeRate);
                stockRateDto.setTradeAmount(stockKbar.getTradeAmount());
                stockRateDto.setTradeDate(tradeDate);
               /* List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
                if(!CollectionUtils.isEmpty(datas)&&datas.size()>0){
                    ThirdSecondTransactionDataDTO dataDTO = datas.get(0);
                    BigDecimal gatherTradeAmount = dataDTO.getTradePrice().multiply(new BigDecimal(dataDTO.getTradeQuantity() * 100));
                    stockRateDto.setGatherTradeAmount(gatherTradeAmount);
                }*/
                map.put(stockCode,stockRateDto);
            }
        }
        return map;
    }

    public List<TradeDatePool> getTradeDatePool(){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-11-01",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("2021-11-24",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        return  tradeDatePools;
    }

    public void blockInfo(){
        ThsBlockInfoQuery thsBlockInfoQuery = new ThsBlockInfoQuery();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(thsBlockInfoQuery);
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos) {
            ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
            detailQuery.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
            if(details==null||details.size()<10){
                continue;
            }
            BLOCK_INFO.put(thsBlockInfo.getBlockCode(),thsBlockInfo);
            BLOCK_DETAIL.put(thsBlockInfo.getBlockCode(),details);
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
