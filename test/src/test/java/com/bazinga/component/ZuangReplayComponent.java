package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.ZuangExportDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZuangReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockKbarService stockKbarService;


    public void replay20220320(){
        List<ZuangExportDTO> resultList = Lists.newArrayList();

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
       // circulateInfos = circulateInfos.stream().filter(item-> !item.getStockCode().startsWith("3")).collect(Collectors.toList());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210301");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
            stockKbarList = stockKbarList.stream().filter(item->item.getTradeQuantity()>0).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<11){
                continue;
            }

            for (int i = 20; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                StockKbar sellStockKbar = stockKbarList.get(i + 1);

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
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
                if(StringUtils.isEmpty(firstPlankTime)){
                    continue;
                }

                List<StockKbar> tempList = stockKbarList.subList(i - 11, i);
                StockKbar currentStockKbar = tempList.get(tempList.size() - 1);
                BigDecimal lowPrice = currentStockKbar.getAdjLowPrice();
                int low2UpperDays = 0;
                for (int j = 1; j < 7; j++) {
                    StockKbar tempStockKbar = tempList.get(tempList.size() - j);
                    if(lowPrice.compareTo(tempStockKbar.getLowPrice())>0){
                        lowPrice = tempStockKbar.getAdjLowPrice();
                        low2UpperDays = j;
                    }
                }
                StockKbar beginStockKbar  = tempList.get(tempList.size()-7);
                BigDecimal low10Rate =  PriceUtil.getPricePercentRate(currentStockKbar.getAdjClosePrice().subtract(lowPrice), beginStockKbar.getAdjOpenPrice());

                if(low10Rate.compareTo(new BigDecimal(20))<0){
                    continue;
                }
                boolean isPlank = isPlank(tempList);
                if(isPlank){
                    continue;
                }

                int planks = plankDays(tempList);
                List<StockKbar> low2UpList = tempList.subList(tempList.size() - low2UpperDays, tempList.size());
                BigDecimal low2UpAvgAmount = low2UpList.stream().map(StockKbar::getTradeAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(low2UpList.size()), 2, BigDecimal.ROUND_HALF_UP);
                List<StockKbar> preLowDay5List = tempList.subList(tempList.size() - low2UpperDays - 5, tempList.size() - low2UpperDays);
                BigDecimal preDay5AvgAmount = preLowDay5List.stream().map(StockKbar::getTradeAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(preLowDay5List.size()), 2, BigDecimal.ROUND_HALF_UP);

                log.info("满足条件 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                ZuangExportDTO exportDTO = new ZuangExportDTO();
                exportDTO.setStockName(stockKbar.getStockName());
                exportDTO.setStockCode(stockKbar.getStockCode());
                exportDTO.setBuyKbarDate(stockKbar.getKbarDate());
                exportDTO.setBuyPrice(stockKbar.getHighPrice());
                exportDTO.setBuyTime(firstPlankTime);
                exportDTO.setCirculate(circulateInfo.getCirculate());
                exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                exportDTO.setLow2UpDays(low2UpperDays);
                exportDTO.setLow2UpRate(low10Rate);
                exportDTO.setPreBuyPrice(preStockKbar.getClosePrice());
                exportDTO.setLowAvgAmount(low2UpAvgAmount);
                exportDTO.setPreDay5LowAvgAmount(preDay5AvgAmount);
                exportDTO.setPlankDays(planks);
                boolean closeUpperFlag = stockKbar.getClosePrice().compareTo(stockKbar.getHighPrice())==0;
                exportDTO.setSealType(closeUpperFlag?1:0);
                BigDecimal sellPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(sellPrice!=null){
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                }
                resultList.add(exportDTO);
            }


        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\庄股6日分析.xls");


    }

    private boolean isPlank(List<StockKbar> tempList) {
        for (int i = 1; i < 7; i++) {
            StockKbar tempStockKbar = tempList.get(tempList.size() - i);
            StockKbar preTempStockKbar = tempList.get(tempList.size() - i-1);

            boolean isPlank = StockKbarUtil.isUpperPrice(tempStockKbar, preTempStockKbar);
            if(isPlank){
                return true;
            }
        }
        return false;
    }

    private int plankDays(List<StockKbar> tempList) {
        int plank=0;
        for (int i = 1; i < 7; i++) {
            StockKbar tempStockKbar = tempList.get(tempList.size() - i);
            StockKbar preTempStockKbar = tempList.get(tempList.size() - i-1);

            boolean isPlank = StockKbarUtil.isHighUpperPrice(tempStockKbar, preTempStockKbar);
            if(isPlank){
               plank++;
            }
        }
        return plank;
    }


    public void replay(){

        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("000607", "20210823");
        List<ZuangExportDTO> resultList = Lists.newArrayList();

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item-> !item.getStockCode().startsWith("3")).collect(Collectors.toList());

        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_Date", Sort.SortType.ASC);
            query.setKbarDateFrom("20201220");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbarList)|| stockKbarList.size()<5){
                continue;
            }
            stockKbarList = stockKbarList.stream().filter(item-> item.getTradeQuantity()!=0).collect(Collectors.toList());

            for (int i = 1; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice());
                BigDecimal circulateAmountZ =  stockKbar.getHighPrice().multiply(new BigDecimal(circulateInfo.getCirculateZ()));
              /*  if(circulateAmountZ.compareTo(new BigDecimal("5000000000"))>0){
                    continue;
                }*/

                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

                if(highRate.compareTo(new BigDecimal("8")) < 0){
                    continue;
                }
                if(openRate.compareTo(new BigDecimal("3"))>0){
                    continue;
                }

                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list) || list.size()<3){
                    continue;
                }

                ThirdSecondTransactionDataDTO open = list.get(0);
                BigDecimal openTradeAmount =  open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity())).multiply(CommonConstant.DECIMAL_HUNDRED);
                if(openTradeAmount.compareTo(new BigDecimal("1000000"))>0){
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> min2List = historyTransactionDataComponent.getFixTimeData(list, "09:32");
                boolean isOver8 = false;
                boolean isPlank = false;
                BigDecimal upperPrice = PriceUtil.calUpperPrice(preStockKbar.getStockCode(),preStockKbar.getClosePrice());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : min2List) {
                    if(!isPlank&& transactionDataDTO.getTradePrice().compareTo(upperPrice) == 0 && transactionDataDTO.getTradeType() ==1 ){
                        isPlank = true;
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                    if(rate.compareTo(new BigDecimal(8))>0){
                        isOver8 = true;
                        break;
                    }
                }
                if(isOver8){
                    String plankTime = "";
                    for (int j = min2List.size(); j < list.size()-1; j++) {
                        ThirdSecondTransactionDataDTO preTransactionDataDTO = list.get(j);
                        ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j+1);
                        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType() ==1){
                            if(preTransactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || preTransactionDataDTO.getTradeType()!=1){
                                log.info("出现涨停S stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                                plankTime = transactionDataDTO.getTradeTime();
                                break;
                            }
                        }
                    }
                    if(StringUtils.isEmpty(plankTime)){
                        continue;
                    }
                    log.info("满足条件 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    ZuangExportDTO exportDTO = new ZuangExportDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuyKbarDate(stockKbar.getKbarDate());
                    exportDTO.setBuyTime(plankTime);
                    exportDTO.setCirculateAmountZ(circulateAmountZ);
                    exportDTO.setOpenTradeAmount(openTradeAmount);
                    exportDTO.setBuyPrice(stockKbar.getHighPrice());
                    exportDTO.setOpenRate(openRate);
                    BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(avgPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                    resultList.add(exportDTO);
                }
            }

        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\庄股分析.xls");
    }


}
