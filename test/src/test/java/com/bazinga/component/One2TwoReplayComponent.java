package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.One2TwoSellDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.PlankHighDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.PlankHighUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.SortUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class One2TwoReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){
        Map<String, List<One2TwoSellDTO>> one2TwoMap = getOne2TwoMap();

        List<One2TwoSellDTO> resultList = new ArrayList<>();

        one2TwoMap.forEach((kbarDate,list)->{
            if(list.size()>3){
                resultList.addAll(list.subList(0,3));
            }else {
                resultList.addAll(list);
            }
        });

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\1进2前三分析.xls");


    }




    public Map<String, List<One2TwoSellDTO>> getOne2TwoMap(){

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, List<One2TwoSellDTO>> resultMap = new HashMap<>();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            if(stockKbarList.size()<20){
                continue;
            }
            for (int i = 8; i < stockKbarList.size()-1; i++) {

                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i - 1);

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

                PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(stockKbarList.subList(i - 8, i));
                if(plankHighDTO.getPlankHigh()!=1){
                    continue;
                }

                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                ThirdSecondTransactionDataDTO open = list.get(0);
                if(open.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                    open.setTradeType(1);
                }
                boolean upperSFlag= false;
                int upperIndex =0;
                for (int j = 1; j < list.size(); j++) {
                    ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                    ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j-1);
                    if(transactionDataDTO.getTradeType()==1 && stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) ==0 ){
                        if(preTransactionDataDTO.getTradeType()!=1 || stockKbar.getHighPrice().compareTo(transactionDataDTO.getTradePrice()) <0){
                            upperSFlag = true;
                            upperIndex = j;
                            break;
                        }
                    }
                }
                if(!upperSFlag || upperIndex > 1200){
                    continue;
                }

                int upperCount =0;
                BigDecimal plankAmount = BigDecimal.ZERO;
                BigDecimal unPlankAmount = BigDecimal.ZERO;
                for (int j = upperIndex; j < upperIndex + 10; j++) {
                    ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                    if(transactionDataDTO.getTradeType()==1 && transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        upperCount++;
                        plankAmount = plankAmount.add(transactionDataDTO.getTradePrice().multiply(CommonConstant.DECIMAL_HUNDRED)
                                .multiply(new BigDecimal(transactionDataDTO.getTradeQuantity().toString())));
                    }else {
                        unPlankAmount = unPlankAmount.add(transactionDataDTO.getTradePrice().multiply(CommonConstant.DECIMAL_HUNDRED)
                                .multiply(new BigDecimal(transactionDataDTO.getTradeQuantity().toString())));
                    }
                }

                One2TwoSellDTO one2TwoSellDTO = new One2TwoSellDTO();
                one2TwoSellDTO.setStockCode(stockKbar.getStockCode());
                one2TwoSellDTO.setStockName(stockKbar.getStockName());
                one2TwoSellDTO.setPlankTime(list.get(upperIndex).getTradeTime());
                one2TwoSellDTO.setPlankAmount(plankAmount);
                one2TwoSellDTO.setUnPlankAmount(unPlankAmount);
                one2TwoSellDTO.setPlankCount(upperCount);
                one2TwoSellDTO.setQuoteCount(upperIndex);
                one2TwoSellDTO.setKbarDate(stockKbar.getKbarDate());

                List<One2TwoSellDTO> one2TwoSellDTOS = resultMap.computeIfAbsent(stockKbar.getKbarDate(), k -> new ArrayList<>());
                one2TwoSellDTOS.add(one2TwoSellDTO);
            }
        }


        Map<String, List<One2TwoSellDTO>> sMap = new HashMap<>();

        resultMap.forEach((key,list)->{
             list =list.stream().sorted(Comparator.comparing(One2TwoSellDTO::getQuoteCount)).collect(Collectors.toList());
             sMap.put(key,list);
        });

        return  sMap;



    }




}
