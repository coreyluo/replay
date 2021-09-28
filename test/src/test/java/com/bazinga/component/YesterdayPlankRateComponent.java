package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class YesterdayPlankRateComponent {
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

    public void yesterdayPlankRate(){

        Map<String, List<YesterdayPlankDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            List<StockKbar> stockKBars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 200);
            if(CollectionUtils.isEmpty(stockKBars)){
                continue;
            }
            List<YesterdayPlankDTO> yesterdayPlankDTOS = plankInfo(stockKBars);
            for (YesterdayPlankDTO plankDTO:yesterdayPlankDTOS){
                List<YesterdayPlankDTO> dtos = map.get(plankDTO.getTradeDate());
                if(dtos==null){
                    dtos = Lists.newArrayList();
                    map.put(plankDTO.getTradeDate(),dtos);
                }
                dtos.add(plankDTO);
            }
        }
        List<YesterdayPlankDTO> planks = Lists.newArrayList();
        for (String key:map.keySet()){
            List<YesterdayPlankDTO> plankDTOS = map.get(key);
            BigDecimal totalEndRate = BigDecimal.ZERO;
            BigDecimal totalStartRate = BigDecimal.ZERO;
            int count = 0;
            for (YesterdayPlankDTO plankDTO:plankDTOS){
                if(plankDTO.getStartRate()!=null&&plankDTO.getEndRate()!=null){
                    totalEndRate = totalEndRate.add(plankDTO.getEndRate());
                    totalStartRate = totalStartRate.add(plankDTO.getStartRate());
                    count = count+1;
                }
            }
            if(count>0){
                BigDecimal endRate = totalEndRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal startRate = totalStartRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                YesterdayPlankDTO plankDTO = new YesterdayPlankDTO();
                Date date = commonComponent.afterTradeDate(DateUtil.parseDate(key, DateUtil.yyyyMMdd));
                plankDTO.setTradeDate(DateUtil.format(date,DateUtil.yyyy_MM_dd));
                if(plankDTO.getTradeDate().equals("2021-02-25")){
                    System.out.println(11111);
                }
                plankDTO.setEndRate(endRate);
                plankDTO.setStartRate(startRate);
                planks.add(plankDTO);
            }
        }

        List<Object[]> datas = Lists.newArrayList();
        for(YesterdayPlankDTO dto:planks){
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getStartRate());
            list.add(dto.getEndRate());
            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","日期","开盘涨幅","收盘涨幅"};

        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("昨日涨停",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("昨日涨停");
        }catch (Exception e){
            log.info(e.getMessage());
        }

    }

    public List<YesterdayPlankDTO>  plankInfo(List<StockKbar> stockKbars){
        List<YesterdayPlankDTO> list = Lists.newArrayList();
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(preStockKbar!=null){
                boolean endUpper = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice());
                boolean adjEndUpper = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getAdjClosePrice(), preStockKbar.getAdjClosePrice());
                if(endUpper||adjEndUpper){
                    YesterdayPlankDTO plankDTO = new YesterdayPlankDTO();
                    plankDTO.setStockCode(stockKbar.getStockCode());
                    plankDTO.setTradeDate(stockKbar.getKbarDate());
                    plankDTO.setBuyKbar(stockKbar);
                    nextDayRateInfo(stockKbars,plankDTO);
                    list.add(plankDTO);
                }
            }
            preStockKbar = stockKbar;
        }

        return list;
    }
    public void nextDayRateInfo(List<StockKbar> stockKbars,YesterdayPlankDTO plankDTO){
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(i==1){
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(plankDTO.getBuyKbar().getAdjClosePrice()), plankDTO.getBuyKbar().getAdjClosePrice());
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(plankDTO.getBuyKbar().getAdjClosePrice()), plankDTO.getBuyKbar().getAdjClosePrice());
                plankDTO.setStartRate(openRate);
                plankDTO.setEndRate(endRate);
                return;
            }
            if(plankDTO.getTradeDate().equals(stockKbar.getKbarDate())){
                i++;
            }
        }
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

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);
    }


}
