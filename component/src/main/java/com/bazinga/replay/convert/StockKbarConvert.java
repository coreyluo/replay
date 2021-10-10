package com.bazinga.replay.convert;


import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.model.StockKbar;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;

import java.math.BigDecimal;
import java.util.List;

public class StockKbarConvert {


    public static List<StockKbar> convert(DataTable dataTable, String stockCode, String stockName){

        int rows = dataTable.rows();
        List<StockKbar> stockKbarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            StockKbar stockKbar = new StockKbar();
            stockKbar.setStockCode(stockCode);
            stockKbar.setStockName(stockName);
            stockKbar.setKbarDate(row[0]);
            stockKbar.setOpenPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setClosePrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setHighPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setLowPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setTradeQuantity(Long.valueOf(row[5])/100);
            stockKbar.setTradeAmount(new BigDecimal(row[6]));
            stockKbar.setUniqueKey(stockCode+ SymbolConstants.UNDERLINE+row[0]);
            stockKbarList.add(stockKbar);
        }
        return stockKbarList;
    }

    public static List<StockKbar> convertSpecial(DataTable dataTable, String stockCode, String stockName){
        int rows = dataTable.rows();
        List<StockKbar> stockKbarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            if(i==0) {
                String[] row = dataTable.getRow(i);
                StockKbar stockKbar = new StockKbar();
                stockKbar.setStockCode(stockCode);
                stockKbar.setStockName(stockName);
                stockKbar.setKbarDate(row[0]);
                stockKbar.setOpenPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
                stockKbar.setClosePrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
                stockKbar.setHighPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
                stockKbar.setLowPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
                stockKbar.setTradeQuantity(Long.valueOf(row[5]) / 100);
                stockKbar.setTradeAmount(new BigDecimal(row[6]));
                stockKbar.setUniqueKey(stockCode + SymbolConstants.UNDERLINE + row[0]);
                stockKbarList.add(stockKbar);
            }
        }
        return stockKbarList;
    }
}
