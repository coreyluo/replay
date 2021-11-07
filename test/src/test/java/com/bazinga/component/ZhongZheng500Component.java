package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
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
public class ZhongZheng500Component {
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
    public void zz500Buy(){
        List<Zz500BuyDTO> zz500BuyDTOS = zz500LevelInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(Zz500BuyDTO dto:zz500BuyDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getEndRate());
            list.add(dto.getEndPrice());
            list.add(dto.getExchangeAmount());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","收盘涨幅","收盘价格","成交额","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("中证500",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("中证500");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<Zz500BuyDTO> zz500LevelInfo(){
        List<Zz500BuyDTO> list = Lists.newArrayList();
        Map<String, List<StockKbarRateDTO>> map = zz500KbarInfo();
        for (String key:map.keySet()){
            if(DateUtil.parseDate(key,DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd))){
                continue;
            }
            List<StockKbarRateDTO> stockKbarRateDTOS = map.get(key);
            StockKbarRateDTO.endRateSort(stockKbarRateDTOS);
            List<StockKbarRateDTO> reverse = Lists.reverse(stockKbarRateDTOS);
            List<StockKbarRateDTO> high50 = stockKbarRateDTOS.subList(0, 10);
            List<StockKbarRateDTO> low50 = reverse.subList(0, 10);
            for (StockKbarRateDTO stockKbarRateDTO:high50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setEndRate(stockKbarRateDTO.getEndRate());
                buyDTO.setEndPrice(stockKbarRateDTO.getStockKbar().getClosePrice());
                buyDTO.setExchangeAmount(stockKbarRateDTO.getStockKbar().getTradeAmount());
                BigDecimal profit = profit(stockKbarRateDTO);
                buyDTO.setProfit(profit);
                list.add(buyDTO);
            }
            for (StockKbarRateDTO stockKbarRateDTO:low50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setEndRate(stockKbarRateDTO.getEndRate());
                buyDTO.setEndPrice(stockKbarRateDTO.getStockKbar().getClosePrice());
                buyDTO.setExchangeAmount(stockKbarRateDTO.getStockKbar().getTradeAmount());
                BigDecimal profit = profit(stockKbarRateDTO);
                buyDTO.setProfit(profit);
                list.add(buyDTO);
            }
        }
        return list;
    }

    public Map<String, List<StockKbarRateDTO>>  zz500KbarInfo(){
        Map<String, List<StockKbarRateDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 150);
            StockKbar preKbar = null;
            if(CollectionUtils.isEmpty(stockKbars)){
                System.out.println(circulateInfo.getStockCode());
                continue;
            }
            StockKbarRateDTO preRateDTO = null;
            for (StockKbar stockKbar:stockKbars){
                if(preKbar!=null){
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    StockKbarRateDTO rateDTO = new StockKbarRateDTO();
                    rateDTO.setStockCode(circulateInfo.getStockCode());
                    rateDTO.setStockName(circulateInfo.getStockName());
                    rateDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    rateDTO.setStockKbar(stockKbar);
                    rateDTO.setTradeDate(stockKbar.getKbarDate());
                    rateDTO.setOpenRate(openRate);
                    rateDTO.setEndRate(endRate);
                    List<StockKbarRateDTO> stockKbarRateDTOS = map.get(rateDTO.getTradeDate());
                    if(stockKbarRateDTOS==null){
                        stockKbarRateDTOS = Lists.newArrayList();
                        map.put(rateDTO.getTradeDate(),stockKbarRateDTOS);
                    }
                    stockKbarRateDTOS.add(rateDTO);
                    if(preRateDTO!=null){
                        preRateDTO.setNextDayStockKbar(stockKbar);
                    }
                    preRateDTO = rateDTO;
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public BigDecimal profit(StockKbarRateDTO buyDTO){
        if(buyDTO.getNextDayStockKbar()==null){
            return null;
        }
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(buyDTO.getStockCode(), DateUtil.parseDate(buyDTO.getNextDayStockKbar().getKbarDate(), DateUtil.yyyyMMdd));
        avgPrice = chuQuanAvgPrice(avgPrice, buyDTO.getNextDayStockKbar());
        if(avgPrice==null||buyDTO.getStockKbar()==null||buyDTO.getStockKbar().getAdjClosePrice()==null){
            System.out.println(buyDTO.getStockCode()+"没有收盘价格");
            return null;
        }
        BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyDTO.getStockKbar().getAdjClosePrice()), buyDTO.getStockKbar().getAdjClosePrice());
        return profit;
    }

    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            if(reverse.size()<=30){
                return null;
            }
            List<StockKbar> bars = reverse.subList(30, reverse.size());
            return bars;
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
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);
    }


}
