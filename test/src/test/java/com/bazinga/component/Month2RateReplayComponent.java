package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.Fast300UpperDTO;
import com.bazinga.dto.Month2RateDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
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
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.text.resources.no.JavaTimeSupplementary_no;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Month2RateReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CirculateInfoService circulateInfoService;


    public void szNeeddle(){
        List<KBarDTO> list = Lists.newArrayList();
        for (int i = 0; i < 400; i++) {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, "999999", i, 1);
            List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(dataTable);
            list.add(kBarDTOS.get(0));
        }
      //  log.info("{}", JSONObject.toJSONString(list));
        list = Lists.reverse(list);
        for (int i = 11; i < list.size(); i++) {
            KBarDTO kBarDTO = list.get(i);
            KBarDTO preKBarDTO = list.get(i-1);
            List<KBarDTO> subList = list.subList(i - 11, i );
            KBarDTO first = subList.get(0);
            BigDecimal day5Rate = PriceUtil.getPricePercentRate(subList.get(subList.size()-1).getEndPrice().subtract(first.getEndPrice()),first.getEndPrice());

            BigDecimal minPrice = kBarDTO.getStartPrice().compareTo(kBarDTO.getEndPrice()) >0 ? kBarDTO.getEndPrice():kBarDTO.getStartPrice();
            BigDecimal rate = PriceUtil.getPricePercentRate(kBarDTO.getLowestPrice().subtract(minPrice), preKBarDTO.getEndPrice());
            if(rate.compareTo(new BigDecimal("-1"))<0){
                log.info("满足上证下影线大于1个点kbarDate{} rate{} day10Rate{}", kBarDTO.getDateStr(),rate,day5Rate);
            }

        }
    }

    public void  replay(){
        List<Fast300UpperDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210101");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            for (int i = 2; i < kbarList.size()-1; i++) {

                StockKbar stockKbar = kbarList.get(i);
                StockKbar preStockKbar = kbarList.get(i-1);
                StockKbar sellKbar = kbarList.get(i+1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                int index = 0;
                String plankTime= "";
                for (int j = 1; j < list.size(); j++) {
                    ThirdSecondTransactionDataDTO transactionDataDTO = list.get(j);
                    ThirdSecondTransactionDataDTO preDto = list.get(j-1);
                    if(transactionDataDTO.getTradeType()==1 && transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        if(preDto.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || preDto.getTradeType()!=1){
                           // log.info("判断为涨停stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                            index = j;
                            plankTime = transactionDataDTO.getTradeTime();
                            break;
                        }

                    }

                }


                if(index==0){
                    continue;
                }
                int rateBeginIndex = index-40 <0? 0:index -40 ;
                ThirdSecondTransactionDataDTO begin = list.get(rateBeginIndex);
                BigDecimal rate2min = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(begin.getTradePrice()),preStockKbar.getClosePrice());

                List<ThirdSecondTransactionDataDTO> subList = list.subList(0, index);

                BigDecimal avgPrice = new BigDecimal(historyTransactionDataComponent.calAveragePrice(subList).toString());
                BigDecimal relativeAvgPriceRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(avgPrice), preStockKbar.getClosePrice());
                if(relativeAvgPriceRate.compareTo(new BigDecimal("6"))> 0 ){
                    log.info("满足条件 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    Fast300UpperDTO exportDTO = new Fast300UpperDTO();
                    exportDTO.setStockCode(stockKbar.getStockCode());
                    exportDTO.setStockName(stockKbar.getStockName());
                    exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                    exportDTO.setSellkbarDate(sellKbar.getKbarDate());
                    exportDTO.setBuyPrice(stockKbar.getHighPrice());
                    exportDTO.setRelativeAvgRate(relativeAvgPriceRate);
                    exportDTO.setPlankTime(plankTime);
                    exportDTO.setRate2min(rate2min);
                    BigDecimal sellAvgPrice = historyTransactionDataComponent.calMorningAvgPrice(sellKbar.getStockCode(), sellKbar.getKbarDate());
                    if(sellAvgPrice == null ){
                        continue;
                    }
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellAvgPrice.subtract(stockKbar.getHighPrice()),stockKbar.getHighPrice()));
                    resultList.add(exportDTO);
                }

            }


        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\主板相对均价买入回测.xls");


    }



    public void invokeStrategy(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<Month2RateDTO> resultList = Lists.newArrayList();
        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20201001");
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<40){
                continue;
            }

            for (int i = 40; i < kbarList.size()-1; i++) {
                StockKbar stockKbar = kbarList.get(i);
                StockKbar sellStockKbar = kbarList.get(i+1);
                StockKbar preStockKbar = kbarList.get(i - 1);
                List<StockKbar> subKbarList = kbarList.subList(i - 40, i);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                StockKbar highKbar = getMaxKbar(subKbarList);
                BigDecimal highPrice = highKbar.getAdjHighPrice();
                BigDecimal nDaysUpperRate = StockKbarUtil.getNDaysUpperRate(subKbarList, 40);
                if(nDaysUpperRate.compareTo(new BigDecimal("40"))<0){
                    continue;
                }

                if(stockKbar.getAdjHighPrice().compareTo(highPrice)>0){
                    continue;
                }

                log.info("满足买入条件 stockCode{} date{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                Month2RateDTO exportDTO = new Month2RateDTO();
                exportDTO.setStockCode(stockKbar.getStockCode());
                exportDTO.setStockName(stockKbar.getStockName());
                exportDTO.setBuykbarDate(stockKbar.getKbarDate());
                exportDTO.setBuyPrice(stockKbar.getHighPrice());
                exportDTO.setSellkbarDate(sellStockKbar.getKbarDate());
               // exportDTO.setDaysToHigh();
                exportDTO.setHighPriceRate(PriceUtil.getPricePercentRate(highPrice.subtract(subKbarList.get(0).getAdjClosePrice()),subKbarList.get(0).getAdjClosePrice()));
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                exportDTO.setPremium(PriceUtil.getPricePercentRate(avgPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                resultList.add(exportDTO);


                ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\涨幅过高未突破买入回测.xls");

            }



        }

    }

    private StockKbar getMaxKbar(List<StockKbar> subKbarList) {
       return subKbarList.stream().max(Comparator.comparing(StockKbar::getAdjHighPrice)).get();

    }


}
