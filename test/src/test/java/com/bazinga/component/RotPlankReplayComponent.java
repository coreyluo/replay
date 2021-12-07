package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.RotPlankExportDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RotPlankReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(Integer seal){

        List<RotPlankExportDTO> resultList = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, BigDecimal> shIndexMap = initShOpenRateMap();

        for (CirculateInfo circulateInfo : circulateInfos) {
            if(!"601608".equals(circulateInfo.getStockCode())){
              //  continue;
            }
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210101");
            query.addOrderBy("kbar_date", Sort.SortType.ASC) ;
            List<StockKbar> kbarList = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(kbarList) || kbarList.size()<7){
                continue;
            }
            for (int i = 6; i < kbarList.size()-1; i++) {
                StockKbar buyStockKbar = kbarList.get(i);
                StockKbar sellStockKbar = kbarList.get(i+1);
                StockKbar preStockKbar = kbarList.get(i-1);
                if(!StockKbarUtil.isHighUpperPrice(buyStockKbar,preStockKbar)){
                    continue;
                }
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(kbarList.subList(i - 6, i + 1));
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(buyStockKbar.getStockCode(), buyStockKbar.getKbarDate());

                int rotNums =0;
                String firstPlankTime="";
                String nPlankTime="";
                for (int j = 0; j < list.size()-2; j++) {
                    ThirdSecondTransactionDataDTO currentDto = list.get(j);
                    ThirdSecondTransactionDataDTO afterDto = list.get(j + 1);
                    if(j==0 &&  currentDto.getTradePrice().compareTo(buyStockKbar.getHighPrice())==0){
                        currentDto.setTradeType(1);
                    }
                    if(afterDto.getTradeType()==1 && afterDto.getTradePrice().compareTo(buyStockKbar.getHighPrice())==0){
                        if(currentDto.getTradePrice().compareTo(buyStockKbar.getHighPrice())<0 || currentDto.getTradeType()==0){
                            rotNums++;
                        }
                    }
                    if(StringUtils.isEmpty(firstPlankTime) && rotNums==1){
                        firstPlankTime = afterDto.getTradeTime();
                    }
                    if(StringUtils.isEmpty(nPlankTime) && rotNums ==seal){
                        nPlankTime = afterDto.getTradeTime();
                    }
                }

                if(rotNums >=seal){
                    log.info("满足烂板条件stockCode{} kbarDate{}", buyStockKbar.getStockCode(),buyStockKbar.getKbarDate());
                    RotPlankExportDTO exportDTO = new RotPlankExportDTO();
                    exportDTO.setStockCode(buyStockKbar.getStockCode());
                    exportDTO.setStockName(buyStockKbar.getStockName());
                    exportDTO.setFirstPlankTime(firstPlankTime);
                    exportDTO.setNPlankTime(nPlankTime);
                    Integer sealType = buyStockKbar.getHighPrice().compareTo(buyStockKbar.getClosePrice())==0?1:0;
                    exportDTO.setSealType(sealType);
                    exportDTO.setOpenRate(PriceUtil.getPricePercentRate(buyStockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                    exportDTO.setKbarDate(buyStockKbar.getKbarDate());
                    exportDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    exportDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    exportDTO.setBuyPrice(buyStockKbar.getHighPrice());
                    exportDTO.setShRate(shIndexMap.get(buyStockKbar.getKbarDate() + SymbolConstants.UNDERLINE + nPlankTime));
                    exportDTO.setShOpenRate(shIndexMap.get(buyStockKbar.getKbarDate() + SymbolConstants.UNDERLINE + "09:25"));
                    exportDTO.setUnPlankHigh(plankHighDTO.getUnPlank());
                    BigDecimal avgPrice = historyTransactionDataComponent.calPre1HourAvgPrice(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    exportDTO.setPremium(PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getHighPrice()),buyStockKbar.getHighPrice()));
                    resultList.add(exportDTO);
                }
            }
        }
        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\烂板"+seal+"封买入.xls");

    }

    private Map<String,BigDecimal> initShOpenRateMap() {

        Map<String,BigDecimal> resultMap = new HashMap<>();

        StockKbarQuery query= new StockKbarQuery();
        query.setStockCode("999999");
        query.setKbarDateFrom("20210101");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> kbarList = stockKbarService.listByCondition(query);
        for (int i = 1; i < kbarList.size(); i++) {
            StockKbar preStockKbar = kbarList.get(i - 1);
            StockKbar stockKbar = kbarList.get(i);

            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
            for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                String key = stockKbar.getKbarDate()+ SymbolConstants.UNDERLINE + transactionDataDTO.getTradeTime();
                if(!resultMap.keySet().contains(key)){
                    resultMap.put(key,PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()),preStockKbar.getClosePrice()));
                }
            }
        }
        return resultMap;
    }
}
