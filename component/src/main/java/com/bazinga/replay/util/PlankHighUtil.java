package com.bazinga.replay.util;

import com.bazinga.replay.dto.PlankHighDTO;
import com.bazinga.replay.model.StockKbar;

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

    public static int calTodaySerialsPlank(List<StockKbar> stockKbarList) {
        int planks = 1;
        int unPlanks = 0;
        for (int i = stockKbarList.size() - 2; i > 0; i--) {
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

    public static boolean isTodayFirstPlank(List<StockKbar> stockKbarList){
        StockKbar stockKbar = stockKbarList.get(stockKbarList.size()-2);
        StockKbar pre1StockKbar = stockKbarList.get(stockKbarList.size()-3);
        StockKbar pre2StockKbar = stockKbarList.get(stockKbarList.size()-4);
        if(StockKbarUtil.isUpperPrice(stockKbar,pre1StockKbar) || StockKbarUtil.isUpperPrice(pre1StockKbar,pre2StockKbar)){
            return false;
        }else {
            return true;
        }
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

    public static Integer calOneLinePlank(List<StockKbar> stockKbarList){
        int planks = 0;
        for (int i = stockKbarList.size() - 1; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (StockKbarUtil.isUpperPrice(stockKbar, preStockKbar) && stockKbar.getOpenPrice().compareTo(stockKbar.getClosePrice())==0
                    && stockKbar.getOpenPrice().compareTo(stockKbar.getHighPrice())==0 && stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())==0) {
                planks++;
            } else {
               return planks;
            }
        }


        return planks;
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