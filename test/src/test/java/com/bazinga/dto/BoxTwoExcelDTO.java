package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Data
public class BoxTwoExcelDTO {
    @ExcelElement("stockCode")
    private String stockCode;
    @ExcelElement("stockName")
    private String stockName;
    @ExcelElement("流通z")
    private Long circulateZ;
    @ExcelElement("总股本")
    private Long totalCirculateZ;
    @ExcelElement("tradeDate")
    private String tradeDate;
    @ExcelElement("买入前低点涨幅")
    private BigDecimal beforeHighLowRate;
    @ExcelElement("第一次高点涨幅")
    private BigDecimal firstHighRate;
    @ExcelElement("中间低点幅度")
    private BigDecimal afterHighLowRate;
    @ExcelElement("买入价格")
    private BigDecimal buyPrice;
    @ExcelElement("买入时间")
    private String buyTime;
    @ExcelElement("买入时涨速")
    private BigDecimal buyTimeRaiseRate;
    @ExcelElement("开盘涨幅")
    private BigDecimal openRate;
    @ExcelElement("第一次高点时间")
    private String firstHighTime;
    @ExcelElement("第一次高点时候涨速")
    private BigDecimal firstHighRaiseRate;
    @ExcelElement("买入相对第一次高点时间")
    private Integer betweenTime;
    @ExcelElement("买入时候成交额")
    private BigDecimal buyTimeTradeAmount;
    @ExcelElement("前一日成交额")
    private BigDecimal preTradeAmount;
    @ExcelElement("前一日几连板")
    private Integer prePlanks;
    @ExcelElement("前一日封住数量")
    private Integer preEndPlanks;
    @ExcelElement("3日涨幅")
    private BigDecimal rateDay3;
    @ExcelElement("5日涨幅")
    private BigDecimal rateDay5;
    @ExcelElement("10日涨幅")
    private BigDecimal rateDay10;
    @ExcelElement("30日涨幅")
    private BigDecimal rateDay30;
    private BigDecimal avgAmountDay5;
    private Integer levelBuyTime;
    @ExcelElement("盈利")
    private BigDecimal profit;

    @ExcelElement("jishu")
    private Integer buyTimes;

    private String plankBuyTime;
    private Integer endPlankFlag;
    private BigDecimal plankBuyPrice;
    private BigDecimal plankAdjPrice;
    private BigDecimal plankProfit;

    private String twoHighTime;
    private String twoBuyTime;
    private BigDecimal twoBuyPrice;
    private BigDecimal twoProfit;


    public static List<BoxTwoExcelDTO> buyTimeSort(List<BoxTwoExcelDTO> list){
        Collections.sort(list,new BoxTwoExcelDTO.BuyTimeComparator());
        return list;
    }

    static class BuyTimeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BoxTwoExcelDTO p1 = (BoxTwoExcelDTO)object1;
            BoxTwoExcelDTO p2 = (BoxTwoExcelDTO)object2;
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
