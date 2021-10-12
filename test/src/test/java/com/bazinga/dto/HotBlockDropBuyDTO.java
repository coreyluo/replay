package com.bazinga.dto;


import com.bazinga.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class HotBlockDropBuyDTO {

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    private Long circulateZ;

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

    private String blockCode;
    private String blockName;
    private Integer openBlockLevel;
    private BigDecimal openBlockRate;

    private BigDecimal blockRaiseRate;
    private Integer blockRaiseLevel;
    private String blockRaiseDateStr;
    private Integer raiseDays;
    private BigDecimal blockDropRate;
    private Integer blockDropLevel;

    private BigDecimal openRate;

    private BigDecimal raiseDayRate;
    private BigDecimal dropDayRate;
    private Long dropDayExchange;

    private Integer dropDayLevel;
    private Integer raiseDayLevel;
    private Integer buyDayLevel;

    private String raiseDayPlankTime;

    private BigDecimal beforeRate3;
    private BigDecimal beforeRate5;
    private BigDecimal beforeRate10;
    private BigDecimal beforeCloseRate;
    private Long beforeAvgExchangeDay5;
    private Integer dropDayReds;
    private Integer dropDayGreens;
    private boolean dropDayPlankFlag=false;
    private Integer dropDayBlockPlanks;

    private Integer beforePlankDay5;
    private Integer beforeOpenPlankDay5;
    private Boolean raiseNextDayOpenPlankFlag;
    private BigDecimal raiseNextDayOpenRate;

    private boolean beforePlankDay3=false;

    private BigDecimal raiseDayBlockRate5;
    private BigDecimal raiseDayBlockRate10;

    private BigDecimal raiseDayRate5;
    private BigDecimal raiseDayRate10;

    private boolean dropDayHavePlank;
    private boolean dropDayEndPlank;
    private int score=0;

    private BigDecimal blockDropDayTotalExchangeMoney;
    private BigDecimal blockInterDayEndRate;
    private BigDecimal blockInterDayRelativeRate;
    private BigDecimal blockInterDayTotalExchangeMoney;
    private BigDecimal blockRaiseDayTotalExchangeMoney;
    private BigDecimal blockRaiseDayBeforeDay5AvgExchangeMoney;



    private BigDecimal profit;


    public static List<HotBlockDropBuyDTO> beforeRate5Sort(List<HotBlockDropBuyDTO> list){
        Collections.sort(list,new BeforeRate5Comparator());
        return list;
    }

    static class BeforeRate5Comparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            HotBlockDropBuyDTO p1 = (HotBlockDropBuyDTO)object1;
            HotBlockDropBuyDTO p2 = (HotBlockDropBuyDTO)object2;
            if(p1.getBeforeRate5()==null){
                return 1;
            }
            if(p2.getBeforeRate5()==null){
                return -1;
            }
            int i = p2.getBeforeRate5().compareTo(p1.getBeforeRate5());
            return i;
        }
    }

    public static List<HotBlockDropBuyDTO> dropDayExchangeSort(List<HotBlockDropBuyDTO> list){
        Collections.sort(list,new DropDayExchangeComparator());
        return list;
    }

    static class DropDayExchangeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            HotBlockDropBuyDTO p1 = (HotBlockDropBuyDTO)object1;
            HotBlockDropBuyDTO p2 = (HotBlockDropBuyDTO)object2;
            if(p1.getDropDayExchange()==null){
                return 1;
            }
            if(p2.getDropDayExchange()==null){
                return -1;
            }
            int i = p2.getDropDayExchange().compareTo(p1.getDropDayExchange());
            return i;
        }
    }


    public static List<HotBlockDropBuyDTO> beforePlankDay5Sort(List<HotBlockDropBuyDTO> list){
        Collections.sort(list,new BeforePlankDay5Comparator());
        return list;
    }

    static class BeforePlankDay5Comparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            HotBlockDropBuyDTO p1 = (HotBlockDropBuyDTO)object1;
            HotBlockDropBuyDTO p2 = (HotBlockDropBuyDTO)object2;
            if(p1.getBeforePlankDay5()==null){
                return 1;
            }
            if(p2.getBeforePlankDay5()==null){
                return -1;
            }
            int i = p2.getBeforePlankDay5().compareTo(p1.getBeforePlankDay5());
            return i;
        }
    }

    public static List<HotBlockDropBuyDTO> plankTimeSort(List<HotBlockDropBuyDTO> list){
        Collections.sort(list,new PlankTimeComparator());
        return list;
    }

    static class PlankTimeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            HotBlockDropBuyDTO p1 = (HotBlockDropBuyDTO)object1;
            HotBlockDropBuyDTO p2 = (HotBlockDropBuyDTO)object2;
            Date p1Time = DateUtil.parseDate(p1.getRaiseDayPlankTime(),DateUtil.HH_MM);
            Date p2Time = DateUtil.parseDate(p2.getRaiseDayPlankTime(),DateUtil.HH_MM);
            if(p2Time.before(p1Time)){
                return 1;
            }else{
                return -1;
            }
        }
    }
}
