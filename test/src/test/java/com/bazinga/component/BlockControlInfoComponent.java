package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.HighPositionDTO;
import com.bazinga.dto.StockPlankTimeInfoDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockControlInfoComponent {
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
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    public List<ThsBlockInfo> THS_BLOCK_INFOS = Lists.newArrayList();
    public Map<String,List<ThsBlockStockDetail>> THS_BLOCK_STOCK_DETAIL_MAP = new HashMap<>();

    public void badPlankInfo(){
        getBlockInfo();
        Map<String, Map<String, StockPlankTimeInfoDTO>> stockPlankTimeInfoMaps = new HashMap<>();
        getPlankMaps(stockPlankTimeInfoMaps);
        Map<String, Map<String, List<StockPlankTimeInfoDTO>>> map = blockPlankCountInfo(stockPlankTimeInfoMaps);

        List<Object[]> datas = Lists.newArrayList();
       /* for(HighPositionDTO dto:highPositionDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketAmount());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankTime());
            list.add(dto.isEndPlankFlag());
            list.add(dto.getHighPlanks());
            list.add(dto.getEndPlanks());
            list.add(dto.getYesterdayHighPlankFlag());
            list.add(dto.getYesterdayEndPlankFlag());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }*/

        String[] rowNames = {"index","股票代码","股票名称","流通z","流通市值","交易日期","买入时间","尾盘是否封住","前10日上板未封住次数","前10日上板且尾盘封住次数","昨日是否触碰板","昨日尾盘是否封住","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("10天5板数据",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("10天5板数据");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public void planksInfo(Map<String, Map<String, List<StockPlankTimeInfoDTO>>> map){
        for (String tradeDateStr:map.keySet()){
            Date buyDate = commonComponent.afterTradeDate(DateUtil.parseDate(tradeDateStr, DateUtil.yyyyMMdd));
            Map<String, List<StockPlankTimeInfoDTO>> buyStockMaps = map.get(tradeDateStr);
            for (String blockCode:buyStockMaps.keySet()){
                List<StockPlankTimeInfoDTO> buyStocks = buyStockMaps.get(blockCode);
                for (StockPlankTimeInfoDTO buyStock:buyStocks){
                    buyStock.setBuyDateStr(DateUtil.format(buyDate,DateUtil.yyyyMMdd));

                }
            }

        }
    }

    public void getBlockInfo(){
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        THS_BLOCK_INFOS.addAll(thsBlockInfos);
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
            query.setBlockCode(thsBlockInfo.getBlockCode());
            List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(query);
            THS_BLOCK_STOCK_DETAIL_MAP.put(thsBlockInfo.getBlockCode(),details);
        }
    }

    public Map<String, Map<String,List<StockPlankTimeInfoDTO>>> blockPlankCountInfo(Map<String, Map<String, StockPlankTimeInfoDTO>> stockPlankTimeInfoMaps){
        Map<String, Map<String,List<StockPlankTimeInfoDTO>>> map = new HashMap<>();
        for (String tradeDateStr:stockPlankTimeInfoMaps.keySet()){
            Map<String, StockPlankTimeInfoDTO> plankTimeInfoDTOMap = stockPlankTimeInfoMaps.get(tradeDateStr);
            if(plankTimeInfoDTOMap==null||plankTimeInfoDTOMap.size()<10){
                continue;
            }
            for (ThsBlockInfo thsBlockInfo:THS_BLOCK_INFOS){
                List<StockPlankTimeInfoDTO> list = Lists.newArrayList();
                List<ThsBlockStockDetail> details = THS_BLOCK_STOCK_DETAIL_MAP.get(thsBlockInfo.getBlockCode());
                for (ThsBlockStockDetail detail:details){
                    StockPlankTimeInfoDTO stockPlankTimeInfoDTO = plankTimeInfoDTOMap.get(detail.getStockCode());
                    list.add(stockPlankTimeInfoDTO);
                }
                if(list.size()>=10){
                    List<StockPlankTimeInfoDTO> planksLevel = StockPlankTimeInfoDTO.planksLevel(list);
                    List<StockPlankTimeInfoDTO> buyStocks = buyStocks(planksLevel);
                    Map<String, List<StockPlankTimeInfoDTO>> buyStockMap = map.get(tradeDateStr);
                    if(buyStockMap==null){
                        buyStockMap = new HashMap<>();
                        map.put(tradeDateStr,buyStockMap);
                    }
                    buyStockMap.put(thsBlockInfo.getBlockCode(),buyStocks);
                }
            }
        }
        return map;
    }

    public List<StockPlankTimeInfoDTO> buyStocks(List<StockPlankTimeInfoDTO> list){
        List<StockPlankTimeInfoDTO> buyStocks = Lists.newArrayList();
        if(list==null||list.size()<0){
            return null;
        }
        int buyCounts = 0;
        if(list.size()>=10&&list.size()<=20){
            buyCounts=1;
        }else if(list.size()<=30){
            buyCounts = 3;
        }else if(list.size()>30){
            buyCounts = 5;
        }
        int planks = 0;
        for(StockPlankTimeInfoDTO dto:list){
            if(dto.getPlanks()>=planks){
                list.add(dto);
            }
            if(buyStocks.size()==buyCounts) {
                planks = dto.getPlanks();
            }
        }
        return buyStocks;
    }

    public void getPlankMaps(Map<String, Map<String, StockPlankTimeInfoDTO>> stockPlankTimeInfoMaps){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("002694")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(12);
            StockPlankTimeInfoDTO preBuyDTO = null;
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(preKbar!=null) {
                    if(preBuyDTO!=null){

                    }
                    Boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    if(endPlank){
                        StockPlankTimeInfoDTO stockPlankTimeInfoDTO = new StockPlankTimeInfoDTO();
                        Integer continuePlanks = calEndPlanks(limitQueue);
                        stockPlankTimeInfoDTO.setPlanks(continuePlanks);
                        Map<String, StockPlankTimeInfoDTO> plankTimeMap = stockPlankTimeInfoMaps.get(stockKbar.getKbarDate());
                        if(plankTimeMap==null){
                            plankTimeMap   = new HashMap<>();
                            stockPlankTimeInfoMaps.put(stockKbar.getKbarDate(),plankTimeMap);
                        }
                        plankTimeMap.put(stockKbar.getStockCode(),stockPlankTimeInfoDTO);
                        preBuyDTO = stockPlankTimeInfoDTO;
                    }else{
                        preBuyDTO = null;
                    }
                }
                preKbar = stockKbar;
            }
        }
    }

    public void isPlank(StockKbar stockKbar,BigDecimal preEndPrice,StockPlankTimeInfoDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return ;
        }
        boolean isUpper = true;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeTime().equals("09:25")&&!upperPrice){
                isUpper = false;
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=1||!upperPrice){
                isUpper = false;
            }
            if(tradeType==1&&upperPrice){
                if(!isUpper){
                    buyDTO.setBuyTime(data.getTradeTime());
                    return;
                }
            }
        }
    }

    public Integer calEndPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<12){
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
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(endUpper){
                    planks++;
                }else{
                    return planks;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;
    }

    public Integer calHighPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<12){
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
        int i = 1;
        int planks  = 0;
        for (StockKbar stockKbar:reverse){
            if(nextKbar!=null) {
                boolean highUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getHighPrice(), stockKbar.getClosePrice());
                if (!highUpper) {
                    highUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjHighPrice(), stockKbar.getAdjClosePrice());
                }
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(i>=2){
                    if(highUpper&&!endUpper){
                        planks++;
                    }
                }
            }
            nextKbar = stockKbar;
            i++;
        }
        return planks;
    }

    public void calProfit(List<StockKbar> stockKbars,HighPositionDTO buyDTO){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyDTO.getStockKbar().getAdjHighPrice()), buyDTO.getStockKbar().getAdjHighPrice());
                buyDTO.setProfit(profit);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyDTO.getStockKbar().getKbarDate())){
                flag = true;
            }
        }
    }



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<20){
                return null;
            }
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
        List<StockPlankTimeInfoDTO> stockPlankTimeInfoDTOS = StockPlankTimeInfoDTO.planksLevel(list);
        System.out.println(stockPlankTimeInfoDTOS);
    }


}
