package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class StockKbarRateDTO {


    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String tradeDate;
    private StockKbar stockKbar;
    private BigDecimal openRate;
    private BigDecimal endRate;
    private BigDecimal day10Rate;
    private StockKbar nextDayStockKbar;

    public static List<StockKbarRateDTO> endRateHighSort(List<StockKbarRateDTO> list){
        Collections.sort(list,new StockKbarRateDTO.EndRateComparator());
        return list;
    }

    static class EndRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockKbarRateDTO p1 = (StockKbarRateDTO)object1;
            StockKbarRateDTO p2 = (StockKbarRateDTO)object2;
            if(p1.getEndRate()==null){
                return 1;
            }
            if(p2.getEndRate()==null){
                return -1;
            }
            int i = p2.getEndRate().compareTo(p1.getEndRate());
            return i;
        }
    }
    public static List<StockKbarRateDTO> endRateLowSort(List<StockKbarRateDTO> list){
        Collections.sort(list,new StockKbarRateDTO.EndRateLowComparator());
        return list;
    }

    static class EndRateLowComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockKbarRateDTO p1 = (StockKbarRateDTO)object1;
            StockKbarRateDTO p2 = (StockKbarRateDTO)object2;
            if(p1.getEndRate()==null){
                return -1;
            }
            if(p2.getEndRate()==null){
                return 1;
            }
            int i = p1.getEndRate().compareTo(p2.getEndRate());
            return i;
        }
    }



    public static List<StockKbarRateDTO> day10RateHighSort(List<StockKbarRateDTO> list){
        Collections.sort(list,new StockKbarRateDTO.Day10RateComparator());
        return list;
    }

    static class Day10RateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockKbarRateDTO p1 = (StockKbarRateDTO)object1;
            StockKbarRateDTO p2 = (StockKbarRateDTO)object2;
            if(p1.getDay10Rate()==null){
                return 1;
            }
            if(p2.getDay10Rate()==null){
                return -1;
            }
            int i = p2.getDay10Rate().compareTo(p1.getDay10Rate());
            return i;
        }
    }
    public static List<StockKbarRateDTO> day10RateLowSort(List<StockKbarRateDTO> list){
        Collections.sort(list,new StockKbarRateDTO.Day10RateLowComparator());
        return list;
    }

    static class Day10RateLowComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockKbarRateDTO p1 = (StockKbarRateDTO)object1;
            StockKbarRateDTO p2 = (StockKbarRateDTO)object2;
            if(p2.getDay10Rate()==null){
                return 1;
            }
            if(p1.getDay10Rate()==null){
                return -1;
            }
            int i = p1.getDay10Rate().compareTo(p2.getDay10Rate());
            return i;
        }
    }


}
