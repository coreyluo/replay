package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class BlocKFollowStaticBuyDTO {
    private String stockCode;
    private String stockName;
    private String blockCode;
    private BigDecimal amountRate;
    private BigDecimal rate;
    private BigDecimal profit;

    public static List<BlocKFollowStaticBuyDTO> amountRateSort(List<BlocKFollowStaticBuyDTO> list){
        Collections.sort(list,new AmountRateComparator());
        return list;
    }

    static class AmountRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BlocKFollowStaticBuyDTO p1 = (BlocKFollowStaticBuyDTO)object1;
            BlocKFollowStaticBuyDTO p2 = (BlocKFollowStaticBuyDTO)object2;
            if(p1.getAmountRate()==null){
                return 1;
            }
            if(p2.getAmountRate()==null){
                return -1;
            }
            int i = p2.getAmountRate().compareTo(p1.getAmountRate());
            return i;
        }
    }


    public static List<BlocKFollowStaticBuyDTO> rateSort(List<BlocKFollowStaticBuyDTO> list){
        Collections.sort(list,new RateComparator());
        return list;
    }

    static class RateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BlocKFollowStaticBuyDTO p1 = (BlocKFollowStaticBuyDTO)object1;
            BlocKFollowStaticBuyDTO p2 = (BlocKFollowStaticBuyDTO)object2;
            if(p1.getAmountRate()==null){
                return 1;
            }
            if(p2.getAmountRate()==null){
                return -1;
            }
            int i = p2.getRate().compareTo(p1.getRate());
            return i;
        }
    }

}
