package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class PlankTimeLevelDTO {

    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String buyTime;
    private int timeInt;
    private int planks;
    private int endFlag;
    private Integer allLevel;
    private Integer morningLevel;
    private Integer afternoonLevel;
    private Integer plankLevel;
    private BigDecimal rateDay3;
    private BigDecimal rateDay5;
    private BigDecimal rateDay10;
    private BigDecimal profit;

    public static List<PlankTimeLevelDTO> timeIntSort(List<PlankTimeLevelDTO> list){
        Collections.sort(list,new PlankTimeLevelDTO.TimeIntComparator());
        return list;
    }

    static class TimeIntComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            PlankTimeLevelDTO p1 = (PlankTimeLevelDTO)object1;
            PlankTimeLevelDTO p2 = (PlankTimeLevelDTO)object2;
            if(p2.getTimeInt()>p1.getTimeInt()){
                return -1;
            }
            if(p2.getTimeInt()==p1.getTimeInt()){
                return 0;
            }
            if(p2.getTimeInt()<p1.getTimeInt()){
                return 1;
            }
            return 1;
        }
    }

}
