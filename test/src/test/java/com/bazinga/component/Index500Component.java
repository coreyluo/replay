package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.IndexRate500DTO;
import com.bazinga.dto.IndexRateDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Index500Component {

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public  Map<String, IndexRate500DTO> getIndex500RateMap(){

        Map<String, IndexRate500DTO> resultMap = new HashMap<>();
        String[] headList = getHeadList();
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20190101", DateUtil.yyyyMMdd));
        tradeDateQuery.setTradeDateTo(DateUtil.parseDate("20220206", DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);
        BigDecimal closePrice = BigDecimal.ZERO;
        for (TradeDatePool tradeDatePool : tradeDatePools) {

            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getIndexData("000905", kbarDate);
            if(CollectionUtils.isEmpty(list)){
                log.info("");
            }
            if(closePrice.compareTo(BigDecimal.ZERO)==0){
                closePrice = list.get(list.size()-1).getTradePrice();
                continue;
            }
            BigDecimal openPrice = list.get(0).getTradePrice();
            BigDecimal openRate = PriceUtil.getPricePercentRate(openPrice.subtract(closePrice),closePrice);
            BigDecimal lowPrice = new BigDecimal("1000000");
            BigDecimal highPrice = new BigDecimal("-10");

            Map<String, IndexRate500DTO> tempMap = new HashMap<>();
            for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                if(highPrice.compareTo(transactionDataDTO.getTradePrice())<0){
                    highPrice = transactionDataDTO.getTradePrice();
                }
                if(lowPrice.compareTo(transactionDataDTO.getTradePrice())>0){
                    lowPrice = transactionDataDTO.getTradePrice();
                }
                BigDecimal lowRate = PriceUtil.getPricePercentRate(lowPrice.subtract(closePrice),closePrice);
                BigDecimal highRate = PriceUtil.getPricePercentRate(highPrice.subtract(closePrice),closePrice);
                BigDecimal buyRate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(closePrice),closePrice);
                tempMap.put(transactionDataDTO.getTradeTime(),new IndexRate500DTO(openRate,lowRate,highRate,buyRate));
            }
            for (int i = 0; i < headList.length-1; i++) {
                IndexRate500DTO indexRate500DTO = tempMap.get(headList[i]);
                resultMap.put(kbarDate + headList[i+1],indexRate500DTO);

            }
            closePrice = list.get(list.size()-1).getTradePrice();
        }
        return resultMap;

    }

    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
            /*for (int i = 1; i < 21; i++) {
                headList.add(DateUtil.format(date,"HH:mm") + SymbolConstants.UNDERLINE +i);
            }*/
        }
        headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }
}
