package com.bazinga.util;

import com.bazinga.dto.PlankHighDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.util.StockKbarUtil;

import java.util.List;

public class PlankHighUtil {

    public static int calSerialsPlank(List<StockKbar> stockKbarList) {
        int planks = 0;
        int unPlanks = 0;
        for (int i = stockKbarList.size() - 1; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
                continue;
            } else {
                //unPlanks++;
                return planks;
            }
          /*  if(unPlanks>=2){
                return planks;
            }*/
        }
        return planks;
    }

    public static PlankHighDTO calTodayPlank(List<StockKbar> stockKbarList) {
        int planks = 1;
        int unPlanks = 0;
        boolean prePlank = true;
        for (int i = stockKbarList.size() - 2; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
                prePlank = true;
            } else {
                unPlanks++;
                if(unPlanks>=2){
                    if(prePlank){
                        return new PlankHighDTO(planks,unPlanks-1);
                    }else {
                        return new PlankHighDTO(planks,0);
                    }
                }
                prePlank = false;

            }

        }
        return new PlankHighDTO(planks,unPlanks);
    }

    public static PlankHighDTO calCommonPlank(List<StockKbar> stockKbarList) {
        int planks = 0;
        int unPlanks = 0;
        boolean prePlank = true;
        for (int i = stockKbarList.size() - 1; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar)) {
                planks++;
                prePlank = true;
            } else {
                unPlanks++;
                prePlank = false;
            }
            if(unPlanks>=2){
                if(prePlank){
                    return new PlankHighDTO(planks,unPlanks-1);
                }else {
                    return new PlankHighDTO(planks,0);
                }
            }
        }
        return new PlankHighDTO(planks,unPlanks);
    }
}
