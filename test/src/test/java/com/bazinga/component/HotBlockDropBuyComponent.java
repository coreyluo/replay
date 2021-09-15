package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.OtherExcelDTO;
import com.bazinga.dto.PlankExchangeDailyDTO;
import com.bazinga.dto.StockRateDTO;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class HotBlockDropBuyComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void zgcBuy(List<OtherExcelDTO> excelDTOS){
        List<PlankExchangeDailyDTO> dailys = Lists.newArrayList();
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        for(CirculateInfo circulateInfo:circulateInfos){
            /* if (!stockCode.equals("000848")) {
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());

        }
        List<Object[]> datas = Lists.newArrayList();
        for(PlankExchangeDailyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());


            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","流通z","买金额","卖金额","正盈利","盈亏比","连板情况"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("中关村数据回撤",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("中关村数据回撤");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void getBlockDayLevel(){
        Map<String,Map<String, BlockLevelDTO>> allCloseLevelMap = new HashMap<>();
        Map<String,Map<String, BlockLevelDTO>> allOpenLevelMap = new HashMap<>();

        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-01-01",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("2021-09-15",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        Map<String, StockKbar> preEndPriceMap = new HashMap<>();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(tradeDateStr);
            StockKbarQuery query = new StockKbarQuery();
            query.setKbarDate(tradeDateStr);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockRateDTO> openRates = Lists.newArrayList();
            List<StockRateDTO> closeRates = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                StockKbar preStockKbar = preEndPriceMap.get(stockKbar.getStockCode());
                if(preStockKbar!=null){
                    BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    StockRateDTO closeRateDTO = new StockRateDTO();
                    closeRateDTO.setStockCode(stockKbar.getStockCode());
                    closeRateDTO.setStockName(stockKbar.getStockName());
                    closeRateDTO.setRate(closeRate);

                    StockRateDTO openRateDTO  = new StockRateDTO();
                    openRateDTO.setStockCode(stockKbar.getStockCode());
                    openRateDTO.setStockName(stockKbar.getStockName());
                    openRateDTO.setRate(openRate);
                    openRates.add(openRateDTO);
                    closeRates.add(closeRateDTO);
                }
                preEndPriceMap.put(stockKbar.getStockCode(),stockKbar);
            }
            Map<String, BlockLevelDTO> closeLevelMap = blockLevelReplayComponent.calBlockLevelDTO(closeRates);
            allCloseLevelMap.put(tradeDateStr,closeLevelMap);
            Map<String, BlockLevelDTO> openLevelMap = blockLevelReplayComponent.calBlockLevelDTO(openRates);
            allOpenLevelMap.put(tradeDateStr,openLevelMap);
        }
        List<BlockLevelDTO> day3Levels = Lists.newArrayList();
        List<BlockLevelDTO> day2Levels = Lists.newArrayList();
        List<BlockLevelDTO> day1Levels = Lists.newArrayList();
        List<BlockLevelDTO> day1Greens = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            List<BlockLevelDTO> list = Lists.newArrayList();
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            Map<String, BlockLevelDTO> closeLevelMap = allCloseLevelMap.get(tradeDateStr);
            if(closeLevelMap==null||closeLevelMap.size()==0){
                continue;
            }
            for (String key:closeLevelMap.keySet()){
                list.add(closeLevelMap.get(key));
            }
            day1Greens.clear();
            Collections.sort(list);
            for (BlockLevelDTO dto:list){
                if(dto.getAvgRate().compareTo(new BigDecimal(-1))==-1){
                    day1Greens.add(dto);
                }
            }
            day3Levels  = day2Levels;
            day2Levels = day1Levels;
            day1Levels = list;
        }

    }


}
