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
public class BoxStockBuyDTO {
    private String stockCode;
    private String stockName;
    private Long circulateZ;
    private Long totalCirculateZ;
    private String tradeDate;
    private BigDecimal beforeHighLowRate;
    private BigDecimal firstHighRate;
    private BigDecimal afterHighLowRate;
    private BigDecimal buyPrice;
    private String buyTime;
    private BigDecimal buyTimeRaiseRate;
    private BigDecimal openRate;
    private String firstHighTime;
    private BigDecimal firstHighRaiseRate;
    private Integer betweenTime;
    private BigDecimal buyTimeTradeAmount;
    private BigDecimal preTradeAmount;
    private Integer prePlanks;
    private Integer preEndPlanks;
    private BigDecimal rateDay3;
    private BigDecimal rateDay5;
    private BigDecimal rateDay10;
    private BigDecimal rateDay30;
    private BigDecimal avgAmountDay5;
    private Integer levelBuyTime;
    private BigDecimal profit;

    public static List<BoxStockBuyDTO> buyTimeSort(List<BoxStockBuyDTO> list){
        Collections.sort(list,new BoxStockBuyDTO.BuyTimeComparator());
        return list;
    }

    static class BuyTimeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BoxStockBuyDTO p1 = (BoxStockBuyDTO)object1;
            BoxStockBuyDTO p2 = (BoxStockBuyDTO)object2;
            if(p1.getBuyTime()==null){
                return 1;
            }
            if(p2.getBuyTime()==null){
                return -1;
            }
            Date date1 = DateUtil.parseDate(p1.getBuyTime(), DateUtil.HH_MM);
            Date date2 = DateUtil.parseDate(p2.getBuyTime(), DateUtil.HH_MM);
            if(date2.before(date1)){
                return 1;
            }else if(date2.after(date1)){
                return -1;
            }else{
                return 0;
            }
        }
    }
}
