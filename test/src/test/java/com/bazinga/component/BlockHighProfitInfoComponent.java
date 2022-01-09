package com.bazinga.component;

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
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
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
public class BlockHighProfitInfoComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
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
    public void badPlankInfo(){
        Map<String, Map<String,StockProfitDTO>> tradeDateMap = new HashMap<>();
        judgePlankInfo(tradeDateMap);
        Map<String, List<LevelDTO>> levelDtoMap = getLevelDto(tradeDateMap);
        List<BlockProfitDTO> blockProfitDTOS = highProfitBlock(levelDtoMap);
        List<Object[]> datas = Lists.newArrayList();
        for(BlockProfitDTO dto:blockProfitDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getBlockName());
            list.add(dto.getBlockCode());
            list.add(dto.getRateThanTwo());
            list.add(dto.getTotalProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","日期","板块名称","板块代码","相对第二名比例","板票盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("18个点",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("强势板块");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<BlockProfitDTO> highProfitBlock(Map<String, List<LevelDTO>> map){
        Map<String, ThsBlockInfo> blockInfoMap = new HashMap<>();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            blockInfoMap.put(thsBlockInfo.getBlockCode(),thsBlockInfo);
        }
        List<BlockProfitDTO> list = Lists.newArrayList();
        for (String key:map.keySet()){
            List<LevelDTO> levelDTOS = map.get(key);
            if(levelDTOS==null||levelDTOS.size()<=3){
                continue;
            }
            Collections.sort(levelDTOS);
            BigDecimal totalFirst = BigDecimal.ZERO;
            BigDecimal totalTwo = BigDecimal.ZERO;
            int countTwo = 0;
            int i = 0;
            for (LevelDTO levelDTO:levelDTOS){
                i++;
                if(i<=3){
                    totalFirst = totalFirst.add(levelDTO.getRate());
                }
                if(i>3&&i<=6){
                    countTwo++;
                    totalTwo = totalTwo.add(levelDTO.getRate());
                }

            }
            BigDecimal avgFirst = totalFirst.divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal avgTwo = totalTwo.divide(new BigDecimal(countTwo), 2, BigDecimal.ROUND_HALF_UP);
            if(avgFirst.compareTo(avgTwo.multiply(new BigDecimal(1.5)))!=-1){
                BigDecimal divide = avgFirst.divide(avgTwo, 2, BigDecimal.ROUND_HALF_UP);
                BlockProfitDTO blockProfitDTO = new BlockProfitDTO();
                blockProfitDTO.setTradeDate(key);
                blockProfitDTO.setTotalProfit(avgFirst);
                blockProfitDTO.setBlockCode(levelDTOS.get(0).getKey());
                blockProfitDTO.setBlockName(blockInfoMap.get(levelDTOS.get(0).getKey()).getBlockName());
                blockProfitDTO.setRateThanTwo(divide);
                list.add(blockProfitDTO);
            }
        }
        return list;
    }
    public Map<String, List<LevelDTO>> getLevelDto(Map<String, Map<String,StockProfitDTO>> tradeDateMap){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        Map<String, List<LevelDTO>> map = new HashMap<>();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            ThsBlockStockDetailQuery thsBlockStockDetailQuery = new ThsBlockStockDetailQuery();
            thsBlockStockDetailQuery.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(thsBlockStockDetailQuery);
            for (TradeDatePool tradeDatePool:tradeDatePools){
                String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
                Map<String, StockProfitDTO> profitMap = tradeDateMap.get(tradeDateStr);
                if(profitMap==null){
                    continue;
                }
                BigDecimal totalProfit  = BigDecimal.ZERO;
                int count   = 0;
                for (ThsBlockStockDetail detail:details){
                    StockProfitDTO stockProfitDTO = profitMap.get(detail.getStockCode());
                    if(stockProfitDTO!=null&&stockProfitDTO.getProfit()!=null){
                        totalProfit = totalProfit.add(stockProfitDTO.getProfit());
                        count++;
                    }
                }
                if(count>0){
                    LevelDTO levelDTO = new LevelDTO();
                    levelDTO.setKey(thsBlockInfo.getBlockCode());
                    levelDTO.setRate(totalProfit);
                    List<LevelDTO> levelDTOS = map.get(tradeDateStr);
                    if(levelDTOS==null){
                        levelDTOS = Lists.newArrayList();
                        map.put(tradeDateStr,levelDTOS);
                    }
                    levelDTOS.add(levelDTO);
                }
            }
        }
        return map;
    }

    public void judgePlankInfo(Map<String, Map<String,StockProfitDTO>> map){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preKbar!=null) {
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    if(!highPlank){
                        highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(),stockKbar.getAdjHighPrice(),preKbar.getAdjClosePrice());
                    }
                    if(highPlank){
                        boolean isPlank = isPlank(stockKbar, preKbar.getClosePrice());
                        //boolean isPlank = true;
                        BigDecimal profit = calProfit(stockKbars, stockKbar);
                        //BigDecimal profit = new BigDecimal(1);
                        if(isPlank){
                            Map<String, StockProfitDTO> stockMap = map.get(stockKbar.getKbarDate());
                            if(stockMap==null){
                                stockMap = new HashMap<>();
                                map.put(stockKbar.getKbarDate(),stockMap);
                            }
                            StockProfitDTO stockProfitDTO = new StockProfitDTO();
                            stockProfitDTO.setStockCode(stockKbar.getStockCode());
                            stockProfitDTO.setProfit(profit);
                            stockMap.put(stockKbar.getStockCode(),stockProfitDTO);
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
    }

    public boolean isPlank(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
                return true;
            }
        }
        return false;
    }

    public BigDecimal calProfit(List<StockKbar> stockKbars,StockKbar kbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(kbar.getAdjHighPrice()), kbar.getAdjHighPrice());
                return profit;
            }
            if(stockKbar.getKbarDate().equals(kbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            //stockKbars = stockKbars.subList(stockKbars.size()-5, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            return result;
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
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11);
        List<Integer> integers = list.subList(5, list.size());
        System.out.println(integers);

    }


}
