package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class BlockRateBuyDTO {

    private String blockCode;
    private String blockName;
    private String tradeDate;
    private String preTradeDate;
    private String nextTradeDate;
    private String nextTwoTradeDate;
    private String nextThreeTradeDate;
    private BigDecimal preClosePrice;
    private BigDecimal closePrice;
    private String tradeTime;
    private BigDecimal tradePrice;
    private BigDecimal rate;
    private Integer count;
    private BigDecimal avgRate;
    private BigDecimal profit;
    private BigDecimal profit2;
    private BigDecimal profit3;

    public static List<BlockRateBuyDTO> rateSort(List<BlockRateBuyDTO> list){
        Collections.sort(list,new BlockRateBuyDTO.RateComparator());
        return list;
    }

    static class RateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BlockRateBuyDTO p1 = (BlockRateBuyDTO)object1;
            BlockRateBuyDTO p2 = (BlockRateBuyDTO)object2;
            if(p1.getRate()==null){
                return 1;
            }
            if(p2.getRate()==null){
                return -1;
            }
            int i = p2.getRate().compareTo(p1.getRate());
            return i;
        }
    }
}
