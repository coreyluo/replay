package com.bazinga.dto;


import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class RelativeWithSzBuyDTO {

    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal buyPrice;
    private Long circulate;
    private Long circulateZ;
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal beforeRateDay60;
    private BigDecimal lowAddHigh;
    private BigDecimal rateDay3Total;
    private Integer level;
    private BigDecimal rateDay1;
    private BigDecimal rateDay2;
    private BigDecimal rateDay3;
    private BigDecimal profit;
    private BigDecimal profitEnd;


    public static List<RelativeWithSzBuyDTO> rateDay3TotalSort(List<RelativeWithSzBuyDTO> list){
        Collections.sort(list,new RelativeWithSzBuyDTO.RateDay3TotalComparator());
        return list;
    }

    static class RateDay3TotalComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            RelativeWithSzBuyDTO p1 = (RelativeWithSzBuyDTO)object1;
            RelativeWithSzBuyDTO p2 = (RelativeWithSzBuyDTO)object2;
            int result = p1.getRateDay3Total().compareTo(p2.getRateDay3Total())*(-1);
            return result;
        }
    }

}
