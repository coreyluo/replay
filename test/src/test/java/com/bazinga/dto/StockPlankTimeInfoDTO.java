package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class StockPlankTimeInfoDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String plankTime;
    private String buyTime;
    private String buyDateStr;
    private String nextDateStr;
    private boolean nextIsPlank;
    private int planks;

    public static List<StockPlankTimeInfoDTO> planksLevel(List<StockPlankTimeInfoDTO> list){
        Collections.sort(list,new StockPlankTimeInfoDTO.PlanksComparator());
        return list;
    }

    static class PlanksComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockPlankTimeInfoDTO p1 = (StockPlankTimeInfoDTO)object1;
            StockPlankTimeInfoDTO p2 = (StockPlankTimeInfoDTO)object2;
            if(p2.planks>p1.planks){
                return 1;
            }else{
                return -1;
            }
        }
    }
}
