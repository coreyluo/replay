package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockProfitDTO;
import com.bazinga.dto.DongBeiStockDTO;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.StockProfitDTO;
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
public class EastTherProvinceComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private BlockInfoService blockInfoService;
    @Autowired
    private BlockStockDetailService blockStockDetailService;
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
    public void dongBeiInfo(){
        List<DongBeiStockDTO> dtos = highProfitBlock();
        List<Object[]> datas = Lists.newArrayList();
        for(DongBeiStockDTO dto:dtos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getBlockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarket());
            list.add(dto.getPlanks());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","板块代码","流通z","流通市值","上板次数","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("18个点",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("东三省");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<DongBeiStockDTO>  highProfitBlock(){
        List<DongBeiStockDTO> list = Lists.newArrayList();
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        for (BlockInfo blockInfo:blockInfos) {
            if(!blockInfo.getBlockName().equals("黑龙江")&&!blockInfo.getBlockName().equals("吉林")&&!blockInfo.getBlockName().equals("辽宁")){
                continue ;
            }
            BlockStockDetailQuery query = new BlockStockDetailQuery();
            query.setBlockCode(blockInfo.getBlockCode());
            List<BlockStockDetail> blockDetails = blockStockDetailService.listByCondition(query);
            judgePlankInfo(blockDetails,list);
        }
        return list;
    }

    public void judgePlankInfo(List<BlockStockDetail> blockDetails,List<DongBeiStockDTO> list){
        for(BlockStockDetail detail:blockDetails){
            CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
            circulateInfoQuery.setStockCode(detail.getStockCode());
            List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
            if(CollectionUtils.isEmpty(circulateInfos)){
                continue;
            }
            CirculateInfo circulateInfo = circulateInfos.get(0);
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }

            BigDecimal totalProfit = BigDecimal.ZERO;
            int count = 0;
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preKbar!=null) {
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    if (!highPlank) {
                        highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getAdjHighPrice(), preKbar.getAdjClosePrice());
                    }
                    if (highPlank) {
                        boolean isPlank = isPlank(stockKbar, preKbar.getClosePrice());
                        BigDecimal profit = calProfit(stockKbars, stockKbar);
                        if (isPlank&&profit!=null) {
                            count++;
                            totalProfit = totalProfit.add(profit);
                        }
                    }
                }
                preKbar = stockKbar;
            }
            StockKbar lastStockKbar = stockKbars.get(stockKbars.size() - 1);
            if(count>0){
                BigDecimal avgProfit = totalProfit.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal market = lastStockKbar.getClosePrice().multiply(new BigDecimal(circulateInfo.getCirculateZ())).divide(new BigDecimal(100000000), 2, BigDecimal.ROUND_HALF_UP);
                DongBeiStockDTO dongBeiStockDTO = new DongBeiStockDTO();
                dongBeiStockDTO.setStockCode(circulateInfo.getStockCode());
                dongBeiStockDTO.setStockName(circulateInfo.getStockName());
                dongBeiStockDTO.setBlockName(detail.getBlockName());
                dongBeiStockDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000),2,BigDecimal.ROUND_HALF_UP));
                dongBeiStockDTO.setMarket(market);
                dongBeiStockDTO.setPlanks(count);
                dongBeiStockDTO.setProfit(avgProfit);
                list.add(dongBeiStockDTO);
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
            stockKbars = stockKbars.subList(20, stockKbars.size());
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
