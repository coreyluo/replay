package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.StockKbarSumInfoDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.misc.Cache;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class BlockKbarComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockCommonReplayService stockCommonReplayService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private ThsBlockKbarService thsBlockKbarService;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    public void thsBlockKbar(Date date){
        Map<String,StockKbarSumInfoDTO> stockKBarInfoMap = stockKbarInfo(date);
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo blockInfo:thsBlockInfos){
            ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
            detailQuery.setBlockCode(blockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
            if(CollectionUtils.isEmpty(details)||details.size()<10){
                continue;
            }
            ThsBlockKbar thsBlockKbar = calBlockKBar(blockInfo, details, stockKBarInfoMap);
            thsBlockKbar.setTradeDate(DateUtil.format(date,DateUtil.yyyyMMdd));
            thsBlockKbar.setUniqueKey(thsBlockKbar.getBlockCode()+"_"+thsBlockKbar.getTradeDate());
            thsBlockKbar.setCreateTime(new Date());
            thsBlockKbarService.save(thsBlockKbar);
        }
    }

    public void thsBlockKbarGatherQuantity(){

        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo blockInfo:thsBlockInfos){
            System.out.println(blockInfo.getBlockCode());
            ThsBlockStockDetailQuery detailQuery = new ThsBlockStockDetailQuery();
            detailQuery.setBlockCode(blockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(detailQuery);
            if(CollectionUtils.isEmpty(details)||details.size()<10){
                continue;
            }
            ThsBlockKbarQuery query = new ThsBlockKbarQuery();
            query.setBlockCode(blockInfo.getBlockCode());
            query.addOrderBy("trade_date", Sort.SortType.ASC);
            List<ThsBlockKbar> thsBlockKbars = thsBlockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(thsBlockKbars)){
                continue;
            }
            ThsBlockKbar preKbar = null;
            for (ThsBlockKbar thsBlockKbar:thsBlockKbars){
                try {
                    if (preKbar != null) {
                        if (preKbar.getCloseRate().compareTo(new BigDecimal(-1)) == -1 && thsBlockKbar.getOpenRate().compareTo(new BigDecimal(0)) == 1) {
                            Map<String, BigDecimal> gatherAmountMap = gatherQuantityInfo(thsBlockKbar.getTradeDate(), details);
                            BigDecimal gatherAmount = calGatherAmount(details, gatherAmountMap);
                            thsBlockKbar.setGatherAmount(gatherAmount);
                            thsBlockKbarService.updateById(thsBlockKbar);
                        }
                    }
                } catch (Exception e){
                    log.error(e.getMessage(),e);
                }
                preKbar  = thsBlockKbar;
            }

        }
    }


    public BigDecimal calGatherAmount(List<ThsBlockStockDetail> blockDetails,Map<String,BigDecimal> gatherAmountMap){
        BigDecimal totalGatherAmount = BigDecimal.ZERO;
        for (ThsBlockStockDetail detail:blockDetails){
            BigDecimal gatherAmount = gatherAmountMap.get(detail.getStockCode());
            if(gatherAmount!=null){
                totalGatherAmount   = totalGatherAmount.add(gatherAmount);
            }
        }
        return totalGatherAmount;
    }

    public ThsBlockKbar calBlockKBar(ThsBlockInfo thsBlockInfo,List<ThsBlockStockDetail> blockDetails,Map<String,StockKbarSumInfoDTO> stockKBarInfoMap){
        int openCounts = 0;
        int endCounts = 0;
        int day5Counts = 0;
        BigDecimal openTotalRate = BigDecimal.ZERO;
        BigDecimal endTotalRate = BigDecimal.ZERO;
        BigDecimal totalTradeAmount = BigDecimal.ZERO;
        BigDecimal day5TotalRate = BigDecimal.ZERO;
        for (ThsBlockStockDetail detail:blockDetails){
            StockKbarSumInfoDTO sumInfoDTO = stockKBarInfoMap.get(detail.getStockCode());
            if(sumInfoDTO==null){
                continue;
            }
            if(sumInfoDTO.getTradeAmount()!=null){
                totalTradeAmount = totalTradeAmount.add(sumInfoDTO.getTradeAmount());
            }
            if(sumInfoDTO.getOpenRate()!=null){
                openCounts = openCounts+1;
                openTotalRate = openTotalRate.add(sumInfoDTO.getOpenRate());
            }
            if(sumInfoDTO.getEndRate()!=null){
                endCounts = endCounts+1;
                endTotalRate = endTotalRate.add(sumInfoDTO.getEndRate());
            }
            if(sumInfoDTO.getEndRateDay5()!=null){
                day5Counts = day5Counts+1;
                day5TotalRate = day5TotalRate.add(sumInfoDTO.getEndRateDay5());
            }
        }
        ThsBlockKbar thsBlockKbar = new ThsBlockKbar();
        thsBlockKbar.setBlockCode(thsBlockInfo.getBlockCode());
        thsBlockKbar.setBlockName(thsBlockInfo.getBlockName());
        thsBlockKbar.setTradeAmount(totalTradeAmount);
        if(openCounts>0){
            BigDecimal divide = openTotalRate.divide(new BigDecimal(openCounts),2,BigDecimal.ROUND_HALF_UP);
            thsBlockKbar.setOpenRate(divide);
        }
        if(endCounts>0){
            BigDecimal divide = endTotalRate.divide(new BigDecimal(endCounts),2,BigDecimal.ROUND_HALF_UP);
            thsBlockKbar.setCloseRate(divide);
        }
        if(day5Counts>0){
            BigDecimal divide = day5TotalRate.divide(new BigDecimal(day5Counts),2,BigDecimal.ROUND_HALF_UP);
            thsBlockKbar.setCloseRateDay5(divide);
        }
        return thsBlockKbar;
    }
    public Map<String,StockKbarSumInfoDTO> stockKbarInfo(Date date){
        Map<String,StockKbarSumInfoDTO> stockKBarInfoMap=new HashMap<>();
        String dateyyyyMMdd = DateUtil.format(date, DateUtil.yyyyMMdd);
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos) {
            List<StockKbar> stockKBarRemoveNew = stockKbarComponent.getStockKBarRemoveNew(circulateInfo.getStockCode(), 400, 20);
            if(CollectionUtils.isEmpty(stockKBarRemoveNew)){
                continue;
            }
            StockKbarSumInfoDTO sumInfoDTO = new StockKbarSumInfoDTO();
            sumInfoDTO.setStockCode(circulateInfo.getStockCode());
            sumInfoDTO.setDateStr(dateyyyyMMdd);
            List<StockKbar> reverse = Lists.reverse(stockKBarRemoveNew);
            boolean flag = false;
            int i = 0;
            StockKbar buyDayKbar = null;
            for (StockKbar stockKbar:reverse){
                if(stockKbar.getKbarDate().equals(dateyyyyMMdd)){
                    flag=true;
                    buyDayKbar = stockKbar;
                }
                if(flag) {
                    i++;
                }
                if(i==2){
                    BigDecimal endRate = PriceUtil.getPricePercentRate(buyDayKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(buyDayKbar.getAdjOpenPrice().subtract(stockKbar.getAdjOpenPrice()), stockKbar.getAdjOpenPrice());
                    sumInfoDTO.setEndRate(endRate);
                    sumInfoDTO.setOpenRate(openRate);
                    sumInfoDTO.setTradeAmount(buyDayKbar.getTradeAmount());
                }
                if(i==6){
                    BigDecimal endRate = PriceUtil.getPricePercentRate(buyDayKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                    sumInfoDTO.setEndRateDay5(endRate);
                    continue;
                }
            }
            stockKBarInfoMap.put(circulateInfo.getStockCode(),sumInfoDTO);
        }
        return stockKBarInfoMap;
    }

    public Map<String,BigDecimal> gatherQuantityInfo(String tradeDate,List<ThsBlockStockDetail> details){
        Map<String,BigDecimal> gatherAmountMap=new HashMap<>();
        for (ThsBlockStockDetail detail:details) {
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(detail.getStockCode(), tradeDate);
            if(CollectionUtils.isEmpty(datas)){
                continue;
            }
            if(datas!=null&&datas.size()>=1){
                ThirdSecondTransactionDataDTO transactionDataDTO = datas.get(0);
                if(transactionDataDTO.getTradeTime().equals("09:25")){
                    BigDecimal gatherAmount = transactionDataDTO.getTradePrice().multiply(new BigDecimal(transactionDataDTO.getTradeQuantity() * 100));
                    gatherAmountMap.put(detail.getStockCode(),gatherAmount);
                }
            }
        }
        return gatherAmountMap;
    }

}
