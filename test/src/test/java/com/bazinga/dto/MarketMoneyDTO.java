package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class MarketMoneyDTO {
    private String stockCode;

    private String stockName;

    private long circulate;

    private BigDecimal marketMoney;

    public static List<MarketMoneyDTO> marketLevelSort(List<MarketMoneyDTO> list){
        Collections.sort(list,new MarketMoneyDTO.MarketMoneyComparator());
        return list;
    }

    static class MarketMoneyComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            MarketMoneyDTO p1 = (MarketMoneyDTO)object1;
            MarketMoneyDTO p2 = (MarketMoneyDTO)object2;
            int result = p1.getMarketMoney().compareTo(p2.getMarketMoney());
            return result;
        }
    }

}
