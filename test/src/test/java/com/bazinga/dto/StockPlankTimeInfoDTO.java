package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class StockPlankTimeInfoDTO {

    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private String plankTime;
    private String buyTime;
    private String buyDateStr;
    private BigDecimal gatherAmount;
    private StockKbar stockKbar;
    private StockKbar preStockKbar;
    private String blockName;
    private int planks;
    private BigDecimal profit;

    public static List<StockPlankTimeInfoDTO> planksLevel(List<StockPlankTimeInfoDTO> list){
        Collections.sort(list,new StockPlankTimeInfoDTO.PlanksComparator());
        return list;
    }

    static class PlanksComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockPlankTimeInfoDTO p1 = (StockPlankTimeInfoDTO)object1;
            StockPlankTimeInfoDTO p2 = (StockPlankTimeInfoDTO)object2;
            if(p2.getPlanks()>p1.getPlanks()){
                return 1;
            }else{
                return -1;
            }
        }
    }


    public static List<StockPlankTimeInfoDTO> plankTimeLevel(List<StockPlankTimeInfoDTO> list){
        Collections.sort(list,new StockPlankTimeInfoDTO.PlankTimeComparator());
        return list;
    }

    static class PlankTimeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            StockPlankTimeInfoDTO p1 = (StockPlankTimeInfoDTO)object1;
            StockPlankTimeInfoDTO p2 = (StockPlankTimeInfoDTO)object2;
            Date date2 = DateUtil.parseDate(p2.getPlankTime(), DateUtil.HH_MM);
            Date date1 = DateUtil.parseDate(p1.getPlankTime(), DateUtil.HH_MM);
            if(date1.before(date2)){
                return 1;
            }else{
                return -1;
            }
        }
    }
}
