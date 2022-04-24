package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class XiaoTuStockDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal openRate;
    private BigDecimal openAmount;
    private StockKbar stockKbar;
    private StockKbar preStockKbar;


    public static List<XiaoTuStockDTO> openAmountSort(List<XiaoTuStockDTO> list){
        Collections.sort(list,new OpenAmountComparator());
        return list;
    }

    static class OpenAmountComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            XiaoTuStockDTO p1 = (XiaoTuStockDTO)object1;
            XiaoTuStockDTO p2 = (XiaoTuStockDTO)object2;
            if(p1.getOpenAmount()==null){
                return 1;
            }
            if(p2.getOpenAmount()==null){
                return -1;
            }
            int i = p2.getOpenAmount().compareTo(p1.getOpenAmount());
            return i;
        }
    }




}
