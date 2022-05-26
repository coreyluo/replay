package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class IndexKbarComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public void replay(String indexCode){

        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20171225",DateUtil.yyyyMMdd));
        tradeQuery.setTradeDateTo(DateUtil.parseDate("20220521",DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);
        for (TradeDatePool tradeDatePool : tradeDatePools) {

            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(indexCode, tradeDatePool.getTradeDate());
            if(CollectionUtils.isEmpty(list)){
                continue;
            }
            List<ThirdSecondTransactionDataDTO> fixList
                    = historyTransactionDataComponent.getFixTimeData(list, "09:35");

            BigDecimal tradeAmount = BigDecimal.ZERO;
            BigDecimal openPrice = fixList.get(0).getTradePrice();
            BigDecimal closePrice = fixList.get(fixList.size()-1).getTradePrice();
            BigDecimal highPrice = openPrice;
            BigDecimal lowPrice = openPrice;


            for (ThirdSecondTransactionDataDTO transactionDataDTO : fixList) {
                if(highPrice.compareTo(transactionDataDTO.getTradePrice())<0){
                    highPrice = transactionDataDTO.getTradePrice();
                }
                if(lowPrice.compareTo(transactionDataDTO.getTradePrice())>0){
                    lowPrice = transactionDataDTO.getTradePrice();
                }
                tradeAmount = tradeAmount.add(new BigDecimal(transactionDataDTO.getTradeQuantity().toString()).multiply(CommonConstant.DECIMAL_HUNDRED));
            }

            StockKbar stockKbar = new StockKbar();
            stockKbar.setStockCode(indexCode+ SymbolConstants.UNDERLINE +"m5");
            stockKbar.setStockName("上证分钟k");
            stockKbar.setKbarDate(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd));
            stockKbar.setOpenPrice(openPrice);
            stockKbar.setClosePrice(closePrice);
            stockKbar.setHighPrice(highPrice);
            stockKbar.setLowPrice(lowPrice);
            stockKbar.setAdjFactor(new BigDecimal("1"));
            stockKbar.setAdjOpenPrice(stockKbar.getOpenPrice());
            stockKbar.setAdjClosePrice(stockKbar.getClosePrice());
            stockKbar.setAdjHighPrice(stockKbar.getHighPrice());
            stockKbar.setAdjLowPrice(stockKbar.getLowPrice());
            stockKbar.setTradeQuantity(-1L);
            stockKbar.setTradeAmount(tradeAmount);
            stockKbar.setUniqueKey(stockKbar.getStockCode()+ SymbolConstants.UNDERLINE + stockKbar.getKbarDate());

            stockKbarService.save(stockKbar);
        }





    }



}
