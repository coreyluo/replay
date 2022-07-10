package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.convert.ThirdSecondTransactionDataDTOConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class CurrentDayTransactionDataComponent {

    public List<ThirdSecondTransactionDataDTO> getData(String stockCode){
        List<ThirdSecondTransactionDataDTO> resultList = Lists.newArrayList();
        try {
            int loopTimes = 0;
            int count = 600;
            while (loopTimes < 30 && (CollectionUtils.isEmpty(resultList) || !"09:25".equals(resultList.get(0).getTradeTime()))) {
                DataTable transactionData = TdxHqUtil.getTransactionData(stockCode, loopTimes * count, count);
                if (transactionData == null) {
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> list = ThirdSecondTransactionDataDTOConvert.currentConvert(transactionData);
                resultList.addAll(0, list);
                loopTimes++;
            }
        }catch (Exception e){
            log.error("当前日期分时成交数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return resultList;
    }


    public Integer plankTimes(List<ThirdSecondTransactionDataDTO> list,BigDecimal yesterdayPrice,String stockCode){
        int plankTimes = 0;
        try {
            if(CollectionUtils.isEmpty(list)){
                return plankTimes;
            }
            boolean isOpen = true;
            for (ThirdSecondTransactionDataDTO dto:list){
                Integer tradeType = dto.getTradeType();
                boolean isUpper = PriceUtil.isUpperPrice(stockCode,dto.getTradePrice(), yesterdayPrice);
                if(isUpper&&tradeType==2&&dto.getTradeTime().equals("09:25")){
                    isOpen=false;
                    continue;
                }
                if(isOpen&&isUpper&&tradeType==1){
                    plankTimes++;
                }
                if(isUpper&&tradeType==1){
                    isOpen = false;
                }else {
                    isOpen = true;
                }
            }
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return plankTimes;

    }


    public Integer calPlankOpenTimes(List<ThirdSecondTransactionDataDTO> list,BigDecimal yesterdayPrice,String stockCode){
        int plankOpenTimes = 0;
        try {
            if(CollectionUtils.isEmpty(list)){
                return plankOpenTimes;
            }
            boolean beforePlankType = false;
            boolean currentPlankType = false;
            for (ThirdSecondTransactionDataDTO dto:list){
                if(dto.getTradeTime().startsWith("15")){
                    continue;
                }
                Integer tradeType = dto.getTradeType();
                boolean isUpper = PriceUtil.isUpperPrice(stockCode,dto.getTradePrice(), yesterdayPrice);
                if(isUpper&&tradeType==2&&dto.getTradeTime().equals("09:25")){
                    beforePlankType = true;
                    currentPlankType = true;
                    continue;
                }
                if(isUpper&&tradeType==1){
                    currentPlankType = true;
                }else {
                    currentPlankType = false;
                }
                if(beforePlankType&&!currentPlankType){
                    plankOpenTimes++;
                }
                beforePlankType = currentPlankType;
            }
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return plankOpenTimes;

    }

    public boolean isPlank(List<ThirdSecondTransactionDataDTO> list,BigDecimal yesterdayPrice,String stockCode){
        try{
            if(CollectionUtils.isEmpty(list)){
                return false;
            }
            for (ThirdSecondTransactionDataDTO dto:list){
                Integer tradeType = dto.getTradeType();
                boolean isUpper = PriceUtil.isUpperPrice(stockCode,dto.getTradePrice(), yesterdayPrice);
                if(isUpper&&tradeType==1){
                    return true;
                }
            }
        }catch (Exception e){
            log.error("分时成交统计数据判断是否上板异常 stockCode:{}",stockCode);
        }
        return false;
    }
    public Date plankTime(List<ThirdSecondTransactionDataDTO> list,BigDecimal yesterdayPrice,String stockCode){
        try{
            if(CollectionUtils.isEmpty(list)){
                return null;
            }
            for (ThirdSecondTransactionDataDTO dto:list){
                boolean isUpper = PriceUtil.isUpperPrice(stockCode,dto.getTradePrice(), yesterdayPrice);
                if(isUpper&&dto.getTradeType()==2&&dto.getTradeTime().equals("09:25")){
                    String realTradeTime = DateUtil.format(new Date(),DateUtil.yyyy_MM_dd)+" 09:25:03";
                    return DateUtil.parseDate(realTradeTime,DateUtil.DEFAULT_FORMAT);
                }
                Integer tradeType = dto.getTradeType();
                if(isUpper&&tradeType==1){
                    String tradeTime = dto.getTradeTime();
                    String realTradeTime = DateUtil.format(new Date(),DateUtil.yyyy_MM_dd)+" "+tradeTime+":00";
                    return DateUtil.parseDate(realTradeTime,DateUtil.DEFAULT_FORMAT);
                }
            }
        }catch (Exception e){
            log.error("分时成交统计数据判断上板时间异常 stockCode:{}",stockCode);
        }
        return null;
    }

    public boolean isEndPlank(List<ThirdSecondTransactionDataDTO> list,BigDecimal yesterdayPrice,String stockCode){
        try{
            if(CollectionUtils.isEmpty(list)){
                return false;
            }
            boolean firstBeforeEnd = false;
            List<ThirdSecondTransactionDataDTO> reverse = Lists.reverse(list);
            for (ThirdSecondTransactionDataDTO dto:reverse){
                boolean isUpper = PriceUtil.isUpperPrice(stockCode,dto.getTradePrice(), yesterdayPrice);
                if(firstBeforeEnd){
                    if(isUpper&&dto.getTradeType()==1){
                        return true;
                    }else{
                        return false;
                    }
                }
                if(dto.getTradeTime().equals("15:00")){
                    firstBeforeEnd = true;
                }
            }
        }catch (Exception e){
            log.error("分时成交统计数据判断尾盘是否封住异常 stockCode:{}",stockCode);
        }
        return false;
    }

    public Float calAveragePrice(List<ThirdSecondTransactionDataDTO> list){
        float totalPrice = 0;
        Integer totalQuantity = 0;
        for (ThirdSecondTransactionDataDTO item : list) {
            totalPrice += item.getTradeQuantity() * item.getTradePrice().floatValue();
            totalQuantity += item.getTradeQuantity();
        }
        return (float) (Math.round(totalPrice / totalQuantity * 100)) / 100;
    }

    public BigDecimal calAvgPrice(String stockCode) {
        try{
            List<ThirdSecondTransactionDataDTO> datas = getData(stockCode);
            if(CollectionUtils.isEmpty(datas)){
                return null;
            }
            BigDecimal avgPrice = new BigDecimal(calAveragePrice(datas)).setScale(2,BigDecimal.ROUND_HALF_UP);
            return avgPrice;
        } catch (Exception e) {
            log.info("当日计算均价异常 stockCode:{}",stockCode);
            return null;
        }
    }


    public int currentDateSellAmount(String stockCode,int count){
        int amount = 0;
        try{
            DataTable transactionData = TdxHqUtil.getTransactionData(stockCode, 0, count);
            if(transactionData ==null ){
                return amount;
            }
            List<ThirdSecondTransactionDataDTO> list = ThirdSecondTransactionDataDTOConvert.currentConvert(transactionData);
            for (ThirdSecondTransactionDataDTO dto:list){
                if(dto.getTradeType()==1){
                    System.out.println(JSONObject.toJSONString(dto));
                    log.info("计算当前最近几条分时成交数据stockCode：{} data：{}",stockCode,JSONObject.toJSONString(dto));
                    amount = amount+dto.getTradeQuantity();
                }
            }
        }catch (Exception e){
            log.error("获取分时成交近几条数据异常");
            amount = 0;
        }
        return amount;

    }

    public Date getPlankTimeFromTranscation(String stockCode,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = getData(stockCode);
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            String tradeTime = data.getTradeTime();
            BigDecimal tradePrice = data.getTradePrice();
            Integer tradeType = data.getTradeType();
            boolean upperPrice = PriceUtil.isUpperPrice(stockCode, tradePrice, preEndPrice);
            if(tradeTime.equals("09:25")&&upperPrice){
                Date date = DateUtil.parseDate(DateUtil.format(new Date(), DateUtil.yyyy_MM_dd) + " 09:25:00", DateUtil.DEFAULT_FORMAT);
                return date;
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            if(upperPrice&&tradeType==1){
                Date date = DateUtil.parseDate(DateUtil.format(new Date(), DateUtil.yyyy_MM_dd) + " "+tradeTime+":00", DateUtil.DEFAULT_FORMAT);
                return date;
            }
        }
        return null;
    }

    /**
     * 允许买入时间 null 查询分时成交异常
     * @param yesterdayPrice
     * @param stockCode
     * @return
     */
    public String insertTime(BigDecimal yesterdayPrice,String stockCode,List<ThirdSecondTransactionDataDTO> list){
        try {
            if(CollectionUtils.isEmpty(list)){
                log.error("查询不到当日分时成交数据计算上板时间 stockCode:{}",stockCode);
                return null;
            }
            boolean canBuy  = false;
            for (ThirdSecondTransactionDataDTO dto:list){
                String tradeTime = dto.getTradeTime();
                BigDecimal tradePrice = dto.getTradePrice();
                Integer tradeType = dto.getTradeType();
                boolean isUpperPrice = PriceUtil.isUpperPrice(stockCode,tradePrice,yesterdayPrice);
                boolean isSell = false;
                if(tradeType==null || tradeType!=0){
                    isSell = true;
                }
                boolean isPlank = false;
                if(isSell&&isUpperPrice){
                    isPlank = true;
                }
                if(!isPlank){
                    canBuy = true;
                }
                if(canBuy&&isPlank){
                    return tradeTime;
                }
            }
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return null;
    }

    /**
     *
     * @param yesterdayPrice
     * @param stockCode
     * @param list
     * @return 0 未上板 1 上板  -1 没有拿到数据无法判断
     */
    public int thirdSecondTransactionDataPlank(BigDecimal yesterdayPrice,String stockCode,List<ThirdSecondTransactionDataDTO> list){
        try {
            if(CollectionUtils.isEmpty(list)){
                log.error("查询不到当日分时成交数据计算上板时间 stockCode:{}",stockCode);
                return -1;
            }
            for (ThirdSecondTransactionDataDTO dto:list){
                BigDecimal tradePrice = dto.getTradePrice();
                Integer tradeType = dto.getTradeType();
                boolean isUpperPrice = PriceUtil.isUpperPrice(stockCode,tradePrice,yesterdayPrice);
                boolean isSell = false;
                if(tradeType==null || tradeType!=0){
                    isSell = true;
                }
                if(isSell&&isUpperPrice){
                    return 1;
                }
            }
            return 0;
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return 0;
    }
}
