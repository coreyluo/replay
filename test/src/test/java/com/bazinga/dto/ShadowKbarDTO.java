package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ShadowKbarDTO {
    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private  BigDecimal marketMoney;
    private StockKbar stockKbar;
    private StockKbar preStockKbar;
    private StockKbar prePreStockKbar;


    public static List<ShadowKbarDTO> marketMoneySort(List<ShadowKbarDTO> list){
        Collections.sort(list,new MarketMoneyComparator());
        return list;
    }

    static class MarketMoneyComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getMarketMoney()==null){
                return 1;
            }
            if(p2.getMarketMoney()==null){
                return -1;
            }
            int i = p2.getMarketMoney().compareTo(p1.getMarketMoney());
            return i;
        }
    }
}
