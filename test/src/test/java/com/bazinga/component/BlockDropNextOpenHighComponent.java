package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
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
        List<BlockRateDTO> dtos = choaDieInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(BlockRateDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getTradeDate());
            list.add(dto.getOpenRate());
            list.add(dto.getGatherTradeAmount());
            list.add(dto.getPreCloseRate());
            list.add(dto.getPreTotalTradeAmount());

            list.add(dto.getStockGatherTradeAmount());
            list.add(dto.getGatherLeve());
            list.add(dto.getStockOpenRate());
            list.add(dto.getStockPreTradeAmount());
            list.add(dto.getProfit());

            Object[] objects = list.toArray();
            datas.add(objects);
        }
        String[] rowNames = {"index","股票代码","股票名称","板块代码","板块名称","交易日期","开盘涨幅","集合成交量","前一日收盘涨幅","前一日成交量","股票集合成交量","股票集合成交量排名","股票开盘涨幅","股票前一日成交量","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板块跳空",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板块跳空");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<BlockRateDTO> choaDieInfo() {
        List<BlockRateDTO> list = new ArrayList<>();
        blockInfo();
        ThsBlockInfoQuery thsBlockInfoQuery = new ThsBlockInfoQuery();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(thsBlockInfoQuery);
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos) {
            ThsBlockKbarQuery query = new ThsBlockKbarQuery();
            query.setBlockCode(thsBlockInfo.getBlockCode());
            query.addOrderBy("trade_date", Sort.SortType.ASC);
            List<ThsBlockKbar> thsBlockKbars = thsBlockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(thsBlockKbars)){
                continue;
            }
            ThsBlockKbar preKbar = null;
            for (ThsBlockKbar blockKbar:thsBlockKbars){
                System.out.println(blockKbar.getBlockCode());
                if(preKbar!=null){
                    if (preKbar.getCloseRate().compareTo(new BigDecimal(-1)) == -1 && blockKbar.getOpenRate().compareTo(new BigDecimal(0)) == 1) {
                        if(preKbar.getTradeAmount()!=null&&preKbar.getTradeAmount().compareTo(new BigDecimal(10000))==1 && blockKbar.getGatherAmount()!=null){
                            BigDecimal gatherAmount = blockKbar.getGatherAmount();
                            BigDecimal divide = gatherAmount.divide(preKbar.getTradeAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            if(divide.compareTo(new BigDecimal("0.5"))==1){
                                List<BlockRateDTO> result = getLevelStock(thsBlockInfo, preKbar, blockKbar, thsBlockInfo.getBlockCode(), blockKbar.getTradeDate());
                                list.addAll(result);
                               /* if(list.size()>5){
                                    return list;
                                }*/
                            }
                        }
                    }
                }
                preKbar = blockKbar;
            }
        }

        return list;
    }

    public List<BlockRateDTO> getLevelStock(ThsBlockInfo thsBlockInfo,ThsBlockKbar preKbar,ThsBlockKbar blockKbar,String blockCode,String tradeDate){
        List<BlockRateDTO> results = new ArrayList();
        Map<String,ThsBlockStockDetail> detailMap = new HashMap<>();
        List<ThsBlockStockDetail> details = BLOCK_DETAIL.get(blockCode);
        List<LevelDTO> list = new ArrayList<>();
        for (ThsBlockStockDetail detail:details){
            detailMap.put(detail.getStockCode(),detail);
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(detail.getStockCode(), tradeDate);
            if(detail.getStockCode().equals("000625")){
                System.out.println(1111);
            }
            if(!CollectionUtils.isEmpty(datas)){
                ThirdSecondTransactionDataDTO data = datas.get(0);
                BigDecimal gatherMoney = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2,BigDecimal.ROUND_HALF_UP);
                LevelDTO levelDTO = new LevelDTO();
                levelDTO.setKey(detail.getStockCode());
                levelDTO.setRate(gatherMoney);
                list.add(levelDTO);
            }
        }
        if(!CollectionUtils.isEmpty(list)){
            Collections.sort(list);
        }
        if(list.size()>5){
            list = list.subList(0, 5);
        }
        int i = 0;
        for (LevelDTO levelDTO:list){
            i++;
            ThsBlockStockDetail detail = detailMap.get(levelDTO.getKey());
            BlockRateDTO blockRateDTO = new BlockRateDTO();
            blockRateDTO.setBlockCode(thsBlockInfo.getBlockCode());
            blockRateDTO.setBlockName(thsBlockInfo.getBlockName());
            blockRateDTO.setTradeDate(blockKbar.getTradeDate());
            blockRateDTO.setPreTotalTradeAmount(preKbar.getTradeAmount());
            blockRateDTO.setGatherTradeAmount(blockKbar.getGatherAmount());
            blockRateDTO.setPreCloseRate(preKbar.getCloseRate());
            blockRateDTO.setOpenRate(blockKbar.getOpenRate());

            blockRateDTO.setStockCode(detail.getStockCode());
            blockRateDTO.setStockName(detail.getStockName());
            blockRateDTO.setStockGatherTradeAmount(levelDTO.getRate());
            blockRateDTO.setGatherLeve(i);
            getStockInfo(blockRateDTO.getStockCode(),blockRateDTO.getTradeDate(),blockRateDTO);
            results.add(blockRateDTO);
        }
        return results;
    }

    public void getStockInfo(String stockCode,String tradeDate,BlockRateDTO blockRateDTO){
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockKbars)){
            return;
        }
        int i = 0;
        boolean flag = false;
        StockKbar preKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(blockRateDTO.getStockBuyPrice()), blockRateDTO.getStockBuyPrice());
                blockRateDTO.setProfit(profit);
                return;
            }
            if(stockKbar.getKbarDate().equals(tradeDate)){
                flag = true;
                if(preKbar!=null){
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    blockRateDTO.setStockOpenRate(openRate);
                    blockRateDTO.setStockPreTradeAmount(preKbar.getTradeAmount());
                    blockRateDTO.setStockBuyPrice(stockKbar.getAdjOpenPrice());
                }
            }
            preKbar = stockKbar;
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
        list.add(levelDTO3);
        list.add(levelDTO4);
        list.add(levelDTO2);
        list.add(levelDTO1);
        Collections.sort(list);
        list = list.subList(0,3);
        System.out.println(11);
    }


}
