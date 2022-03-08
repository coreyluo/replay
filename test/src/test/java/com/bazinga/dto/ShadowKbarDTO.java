package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class ShadowKbarDTO {
    private String stockCode;
    private String stockName;
    private BigDecimal circulateZ;
    private  BigDecimal marketMoney;
    private StockKbar stockKbar;
    private StockKbar preStockKbar;
    private StockKbar prePreStockKbar;
    private StockKbar nextStockKbar;
    private BigDecimal rateThanAvg5;

    private BigDecimal rateDay5;
    private BigDecimal rateDay10;
    private BigDecimal rateDay15;


    private BigDecimal shadowDayDealMoney;
    private BigDecimal shadowBefore10DealMoney;


    private Long shadowBefore30AvgQuantity;
    private BigDecimal buyRateThanHigh;
    private BigDecimal shadowLength;
    private String shadowTime;
    private Integer buySize;
    private Integer level;

    private BigDecimal buyPrice;
    private BigDecimal afterBuyPrice;
    private BigDecimal buyDayOpenDealMoney;
    private BigDecimal buyBeforeDealMoney;
    private BigDecimal buyDayOPenRate;
    private BigDecimal buyTimeRate;
    private BigDecimal buyPercent;

    private Integer plankTimes;
    private BigDecimal openExchangeMoneyRate;
    private Integer openExchangeMoneyLevel;
    private Integer openExchangeMoneyRateLevel;
    private Integer preDateEndPlanks;

    private boolean haveHighSell = false;
    private boolean haveBestSell  = false;
    private BigDecimal twoPointFiveProfit;
    private BigDecimal profit;
    private BigDecimal moneyProfit;




    private BigDecimal afterProfit;





    //用于创业板低吸
    private boolean buyFlag = false;
    private BigDecimal openRate;
    private BigDecimal openAmount;
    private BigDecimal openAmountRate;
    private Integer openAmountRateLevel;
    private boolean newFlag = false;
    private boolean preIsUpper = false;
    private int continuePlanks = 0;
    private int day5Planks;

    public static List<ShadowKbarDTO> marketMoneySort(List<ShadowKbarDTO> list){
        Collections.sort(list,new MarketMoneyComparator());
        return list;
    }

    static class MarketMoneyComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getMarketMoney()==null){
                return 1;
            }
            if(p2.getMarketMoney()==null){
                return -1;
            }
            int i = p2.getMarketMoney().compareTo(p1.getMarketMoney());
            return i;
        }
    }

    public static List<ShadowKbarDTO> RateThanAvgSort(List<ShadowKbarDTO> list){
        Collections.sort(list,new RateThanAvgComparator());
        return list;
    }

    static class RateThanAvgComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getRateThanAvg5()==null){
                return 1;
            }
            if(p2.getRateThanAvg5()==null){
                return -1;
            }
            int i = p2.getRateThanAvg5().compareTo(p1.getRateThanAvg5());
            return i;
        }
    }


    public static List<ShadowKbarDTO> buyDayOpenDealMoneySort(List<ShadowKbarDTO> list){
        Collections.sort(list,new BuyDayOpenDealMoneyComparator());
        return list;
    }

    static class BuyDayOpenDealMoneyComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getBuyDayOpenDealMoney()==null){
                return 1;
            }
            if(p2.getBuyDayOpenDealMoney()==null){
                return -1;
            }
            int i = p2.getBuyDayOpenDealMoney().compareTo(p1.getBuyDayOpenDealMoney());
            return i;
        }
    }


    public static List<ShadowKbarDTO> openExchangeMoneyRateSort(List<ShadowKbarDTO> list){
        Collections.sort(list,new OpenExchangeMoneyRateComparator());
        return list;
    }

    static class OpenExchangeMoneyRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getOpenExchangeMoneyRate()==null){
                return 1;
            }
            if(p2.getOpenExchangeMoneyRate()==null){
                return -1;
            }
            int i = p2.getOpenExchangeMoneyRate().compareTo(p1.getOpenExchangeMoneyRate());
            return i;
        }
    }

    public static List<ShadowKbarDTO> openAmountRateSort(List<ShadowKbarDTO> list){
        Collections.sort(list,new OpenAmountRateComparator());
        return list;
    }

    static class OpenAmountRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            ShadowKbarDTO p1 = (ShadowKbarDTO)object1;
            ShadowKbarDTO p2 = (ShadowKbarDTO)object2;
            if(p1.getOpenAmountRate()==null){
                return 1;
            }
            if(p2.getOpenAmountRate()==null){
                return -1;
            }
            if(p1.getOpenAmountRate()==null&&p2.getOpenAmountRate()==null){
                return 0;
            }
            int i = p2.getOpenAmountRate().compareTo(p1.getOpenAmountRate());
            return i;
        }
    }
}
