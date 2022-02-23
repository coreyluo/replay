package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.IndexRateDTO;
import com.bazinga.dto.OpenCompeteDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommonReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;




    public Map<String, OpenCompeteDTO> get300CompeteInfo() {

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> item.getStockCode().startsWith("3")).collect(Collectors.toList());


        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);

        Map<String, List<OpenCompeteDTO>> tempMap = new HashMap<>();

        for (CirculateInfo circulateInfo : circulateInfos) {
           /* if(!"300945".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210501");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            for (int i = 1; i < kbarList.size() - 1; i++) {
                StockKbar buyStockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i - 1);
                StockKbar sellStockKbar = kbarList.get(i + 1);
                if (!StockKbarUtil.isUpperPrice(buyStockKbar, preStockKbar)) {
                    continue;
                }

                OpenCompeteDTO openCompeteDTO = new OpenCompeteDTO();
                openCompeteDTO.setStockCode(buyStockKbar.getStockCode());
                openCompeteDTO.setRate(PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(buyStockKbar.getClosePrice()), buyStockKbar.getClosePrice()));

                List<OpenCompeteDTO> list = tempMap.get(buyStockKbar.getKbarDate());
                if (CollectionUtils.isEmpty(list)) {
                    list = new ArrayList<>();
                    tempMap.put(buyStockKbar.getKbarDate(), list);
                }
                list.add(openCompeteDTO);
            }
        }
        Map<String, OpenCompeteDTO> resultMap = new HashMap<>();
        for (Map.Entry<String, List<OpenCompeteDTO>> entry : tempMap.entrySet()) {
            String kbarDate = entry.getKey();
            List<OpenCompeteDTO> list = entry.getValue();
            List<OpenCompeteDTO> sortedList = list.stream().sorted(Comparator.comparing(OpenCompeteDTO::getRate)).collect(Collectors.toList());
            sortedList = Lists.reverse(sortedList);
            for (int i = 0; i < sortedList.size(); i++) {
                OpenCompeteDTO openCompeteDTO = sortedList.get(i);
                openCompeteDTO.setCompeteNum(i + 1);
                resultMap.put(openCompeteDTO.getStockCode() + SymbolConstants.UNDERLINE + kbarDate, openCompeteDTO);
            }

        }


        return resultMap;
    }

    public Map<String, BigDecimal> initShOpenRateMap() {

        Map<String, BigDecimal> resultMap = new HashMap<>();

        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.setKbarDateFrom("20210101");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> kbarList = stockKbarService.listByCondition(query);
        for (int i = 1; i < kbarList.size(); i++) {
            StockKbar preStockKbar = kbarList.get(i - 1);
            StockKbar stockKbar = kbarList.get(i);
            resultMap.put(stockKbar.getKbarDate(), PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice()));
        }
        return resultMap;
    }


    public Map<String, IndexRateDTO> initIndexRateMap(String indexCode) {

        Map<String, IndexRateDTO> resultMap = new HashMap<>();

        List<KBarDTO> list = Lists.newArrayList();
        for (int i = 0; i < 250; i++) {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, indexCode, i, 1);
            KBarDTO kBarDTO = KBarDTOConvert.convertSZKBar(dataTable);
            list.add(kBarDTO);
        }

        list = Lists.reverse(list);
        for (int i = 1; i < list.size() - 1; i++) {
            KBarDTO preKbar = list.get(i - 1);
            KBarDTO currentKbar = list.get(i);
            KBarDTO keyKbar = list.get(i + 1);

            BigDecimal highRate = PriceUtil.getPricePercentRate(currentKbar.getHighestPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
            BigDecimal closeRate = PriceUtil.getPricePercentRate(currentKbar.getEndPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
            resultMap.put(keyKbar.getDateStr(), new IndexRateDTO(closeRate, highRate));
        }
        return resultMap;
    }

    public void replay() {

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item -> !item.getStockCode().startsWith("3")).collect(Collectors.toList());

        Map<String, List<String>> resultMap = new HashMap<>();
        for (CirculateInfo circulateInfo : circulateInfos) {
           /* if(!"000665".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210418");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            if (CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size() < 7) {
                continue;
            }
            for (int i = 7; i < stockKbarList.size(); i++) {

                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar prestockKbar = stockKbarList.get(i - 1);
                if (!StockKbarUtil.isHighUpperPrice(stockKbar, prestockKbar)) {
                    continue;
                }
                if (!PriceUtil.isSuddenPrice(stockKbar.getLowPrice(), prestockKbar.getClosePrice())) {
                    continue;
                }

                int plank = PlankHighUtil.calSerialsPlank(stockKbarList.subList(i - 7, i));
                if (plank > 1) {
                    log.info("滿足连板地天条件 stockCode{} kbarDate{}", stockKbar.getStockCode(), stockKbar.getKbarDate());

                    List<String> list = resultMap.get(stockKbar.getKbarDate());
                    if (list == null) {
                        list = new ArrayList<>();
                        resultMap.put(stockKbar.getKbarDate(), list);
                    }
                    list.add(stockKbar.getStockCode());
                }
            }


        }

        resultMap.forEach((kbarDate, list) -> {

            if (!CollectionUtils.isEmpty(list)) {
                log.info("kabrDate{} 地天板{}", kbarDate, JSONObject.toJSONString(list));
            }


        });


    }

}