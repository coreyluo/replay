package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.PlankQuantityDivideDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class PlankQuantityDivideComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;


    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){

        List<PlankQuantityDivideDTO> resultList = Lists.newArrayList();

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210401");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<7){
                continue;
            }
            for (int i = 11; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preSockKbar = stockKbarList.get(i-1);
                StockKbar pre2SockKbar = stockKbarList.get(i-2);
                StockKbar sellStockKbar = stockKbarList.get(i + 1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preSockKbar)){
                    continue;
                }
                BigDecimal ampRate = PriceUtil.getPricePercentRate(preSockKbar.getHighPrice().subtract(preSockKbar.getLowPrice()),pre2SockKbar.getClosePrice());

                BigDecimal highRate = PriceUtil.getPricePercentRate(preSockKbar.getHighPrice().subtract(pre2SockKbar.getClosePrice()),pre2SockKbar.getClosePrice());

                if(ampRate.compareTo(new BigDecimal("6"))<0 && highRate.compareTo(new BigDecimal("4"))<0){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                int rotNums =0;
                String firstPlankTime="";
                BigDecimal totalTradeAmount = BigDecimal.ZERO;
                for (int j = 0; j < list.size()-2; j++) {
                    ThirdSecondTransactionDataDTO currentDto = list.get(j);
                    ThirdSecondTransactionDataDTO afterDto = list.get(j + 1);
                    if(j==0 &&  currentDto.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        currentDto.setTradeType(1);
                    }
                    if(afterDto.getTradeType()==1 && afterDto.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        if(currentDto.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || currentDto.getTradeType()==0){
                            rotNums++;
                        }
                    }
                    if(StringUtils.isEmpty(firstPlankTime) && rotNums==1){
                        firstPlankTime = afterDto.getTradeTime();
                        break;
                    }
                    totalTradeAmount = totalTradeAmount.add(currentDto.getTradePrice().multiply(CommonConstant.DECIMAL_HUNDRED)
                            .multiply(new BigDecimal(currentDto.getTradeQuantity().toString())));
                }
                if(rotNums==0){
                    continue;
                }
                BigDecimal midPrice = preSockKbar.getHighPrice().subtract(pre2SockKbar.getClosePrice().multiply(ampRate).divide(new BigDecimal("200"),2,BigDecimal.ROUND_HALF_UP));

                List<ThirdSecondTransactionDataDTO> preList = historyTransactionDataComponent.getData(preSockKbar.getStockCode(), preSockKbar.getKbarDate());
                BigDecimal overMidAmount = BigDecimal.ZERO;
                BigDecimal lowMidAmount = BigDecimal.ZERO;
                String preHighTime = "";
                BigDecimal preHighPrice = preSockKbar.getHighPrice();
                for (ThirdSecondTransactionDataDTO transactionDataDTO : preList) {
                    if(transactionDataDTO.getTradePrice().compareTo(preHighPrice) ==0){
                        preHighTime = transactionDataDTO.getTradeTime();
                    }
                    if(transactionDataDTO.getTradePrice().compareTo(midPrice)>=0){
                        overMidAmount = overMidAmount.add(transactionDataDTO.getTradePrice().multiply(new BigDecimal(transactionDataDTO.getTradeQuantity())));
                    }else {
                        lowMidAmount = lowMidAmount.add(transactionDataDTO.getTradePrice().multiply(new BigDecimal(transactionDataDTO.getTradeQuantity())));
                    }
                }
                log.info("满足买入条件stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(stockKbarList.subList(i - 8, i+1));

                BigDecimal preCloseRate = PriceUtil.getPricePercentRate(preSockKbar.getClosePrice().subtract(pre2SockKbar.getClosePrice()),pre2SockKbar.getClosePrice());

                PlankQuantityDivideDTO exportDTO = new PlankQuantityDivideDTO();

                exportDTO.setUpmRate(ampRate);
                exportDTO.setStockCode(stockKbar.getStockCode());
                exportDTO.setStockName(stockKbar.getStockName());
                boolean closeUpperFlag = stockKbar.getClosePrice().compareTo(stockKbar.getHighPrice())==0;
                exportDTO.setSealType(closeUpperFlag?1:0);
                exportDTO.setBuyDate(stockKbar.getKbarDate());
                exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                exportDTO.setUnPlankHigh(plankHighDTO.getUnPlank());
                exportDTO.setOverMidAmount(overMidAmount);
                exportDTO.setLowMidAmount(lowMidAmount);
                exportDTO.setFirstPlankTime(firstPlankTime);
                exportDTO.setPreDayHighRate(highRate);
                exportDTO.setBuyPrice(stockKbar.getHighPrice());
                exportDTO.setDay3Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),3));
                exportDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),5));
                exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRate(stockKbarList.subList(i-11,i),10));
                exportDTO.setDay5LowRate(StockKbarUtil.getNDaysLowCloseRate(stockKbarList.subList(i-11,i),5));
                exportDTO.setDay10LowRate(StockKbarUtil.getNDaysLowCloseRate(stockKbarList.subList(i-11,i),10));
                exportDTO.setPreCloseRate(preCloseRate);
                exportDTO.setPreHighTime(preHighTime);

                BigDecimal sellPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(sellPrice!=null){
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                }
                resultList.add(exportDTO);
            }
        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\打板筹码结构回测.xls");


    }


}
