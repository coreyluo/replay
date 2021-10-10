package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class BestBuyStockDTO {
    private String stockCode;

    private String stockName;

    private String tradeDate;

    private String reason1;

    private String reason2;

    private String reason3;

    private String reason4;

    private String reason5;

    private Boolean beforePlankDay3;

    private BigDecimal buyDayOpenRate;

    private BigDecimal profit;

    public static List<BestBuyStockDTO> buyDayOpenRateSort(List<BestBuyStockDTO> list){
        Collections.sort(list,new BuyDayOpenRateComparator());
        return list;
    }

    static class BuyDayOpenRateComparator implements Comparator<Object>{
        public int compare(Object object1,Object object2){
            BestBuyStockDTO p1 = (BestBuyStockDTO)object1;
            BestBuyStockDTO p2 = (BestBuyStockDTO)object2;
            if(p1.getBuyDayOpenRate()==null){
                return 1;
            }
            if(p2.getBuyDayOpenRate()==null){
                return -1;
            }
            int i = p2.getBuyDayOpenRate().compareTo(p1.getBuyDayOpenRate());
            return i;
        }
    }
}
