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
    private StockKbar nextDayStockKbar;

    public static List<StockKbarRateDTO> endRateSort(List<StockKbarRateDTO> list){
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


}
