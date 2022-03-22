package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
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
import java.util.Map;

@Slf4j
@Component
public class BackSealReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonReplayComponent commonReplayComponent;


    public void replay(){

        List<BackSealDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210301");
            List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbarList) || stockKbarList.size()<32){
                continue;
            }

            for (int i = 31; i < stockKbarList.size()-1; i++) {
                StockKbar stockKbar = stockKbarList.get(i);
                StockKbar preStockKbar = stockKbarList.get(i-1);
                StockKbar sellStockKbar = stockKbarList.get(i+1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(stockKbarList.subList(i-8,i+1));

               /* if(plankHighDTO.getPlankHigh()<2){
                    continue;
                }*/
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());

                int rotNums =0;
                String firstPlankTime="";
                String nPlankTime="";
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
                    }
                    if(StringUtils.isEmpty(nPlankTime) && rotNums ==2){
                        nPlankTime = afterDto.getTradeTime();
                        break;
                    }
                    totalTradeAmount = totalTradeAmount.add(currentDto.getTradePrice().multiply(CommonConstant.DECIMAL_HUNDRED)
                            .multiply(new BigDecimal(currentDto.getTradeQuantity().toString())));
                }
                if(StringUtils.isEmpty(nPlankTime)){
                    continue;
                }
                log.info("满足买入条件stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                BackSealDTO exportDTO = new BackSealDTO();
                exportDTO.setStockCode(stockKbar.getStockCode());
                exportDTO.setStockName(stockKbar.getStockName());
                exportDTO.setFirstPlankTime(firstPlankTime);
                exportDTO.setBuyPrice(stockKbar.getHighPrice());
                exportDTO.setBuyDate(stockKbar.getKbarDate());
                exportDTO.setBackSealTime(nPlankTime);
                exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                exportDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRateDesc(stockKbarList.subList(i-31,i),5));
                exportDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRateDesc(stockKbarList.subList(i-31,i),10));
                exportDTO.setDay30Rate(StockKbarUtil.getNDaysUpperRateDesc(stockKbarList.subList(i-31,i),30));
                exportDTO.setPreDayAmount(preStockKbar.getTradeAmount());
                exportDTO.setTotalTradeAmount(totalTradeAmount);
                exportDTO.setShOpenRate(shOpenRateMap.get(stockKbar.getKbarDate()));

                exportDTO.setUnPlankHigh(plankHighDTO.getUnPlank());
                boolean upperFlag = stockKbar.getOpenPrice().compareTo(stockKbar.getHighPrice())==0;
                boolean closeUpperFlag = stockKbar.getClosePrice().compareTo(stockKbar.getHighPrice())==0;
                exportDTO.setOneLineOpen(upperFlag ? 1:0);
                exportDTO.setSealType(closeUpperFlag?1:0);
                BigDecimal sellPrice = historyTransactionDataComponent.calAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(sellPrice!=null){
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(exportDTO.getBuyPrice()),exportDTO.getBuyPrice()));
                }
                resultList.add(exportDTO);
            }
        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\二板回封回测.xls");

    }




}
