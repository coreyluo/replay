package com.bazinga.replay.convert;

import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author yunshan
 * @date 2019/3/4
 */
public class KBarDTOConvert {

    public static List<StockKbar> convertStockKbar (String stockCode,DataTable dataTable){
        int rows = dataTable.rows();
        List<StockKbar> kBarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            StockKbar stockKbar = new StockKbar();
            stockKbar.setOpenPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setClosePrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setHighPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setLowPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            stockKbar.setTradeQuantity(Long.valueOf(row[5])/100);
            stockKbar.setTradeAmount(new BigDecimal(row[6]).setScale(2,BigDecimal.ROUND_HALF_UP));
            stockKbar.setStockCode(stockCode);
            Date tradeDate = DateUtil.parseDate(row[0], DateUtil.yyyyMMdd);
            stockKbar.setUniqueKey(stockCode+ SymbolConstants.UNDERLINE + row[0]);
            stockKbar.setKbarDate(row[0]);
            stockKbar.setAdjFactor(new BigDecimal("1.0"));
            kBarList.add(stockKbar);

        }
        return kBarList;
    }

    public static List<KBarDTO> convertKBar(DataTable dataTable){
        int rows = dataTable.rows();
        List<KBarDTO> kBarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            KBarDTO commonQuoteDTO = new KBarDTO();
            commonQuoteDTO.setStartPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setEndPrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setHighestPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setLowestPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setTotalExchange(Long.valueOf(row[5])/100);
            commonQuoteDTO.setTotalExchangeMoney(Double.valueOf(row[6]).longValue());
            Date tradeDate = DateUtil.parseDate(row[0], DateUtil.yyyyMMdd);
            commonQuoteDTO.setDate(tradeDate);
            commonQuoteDTO.setDateStr(DateUtil.format(tradeDate,DateUtil.yyyy_MM_dd));
            kBarList.add(commonQuoteDTO);

        }
        return kBarList;
    }

    public static KBarDTO convertSZKBar(DataTable dataTable){
        KBarDTO commonQuoteDTO = new KBarDTO();
        if(dataTable.rows()<=0){
            return null;
        }
        for(int i=0;i<1;i++){
            String[] row = dataTable.getRow(i);
            commonQuoteDTO.setStartPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setEndPrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setHighestPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setLowestPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setTotalExchange(Long.valueOf(row[5])*100);
            commonQuoteDTO.setTotalExchangeMoney(Double.valueOf(row[6]).longValue());
            Date tradeDate = DateUtil.parseDate(row[0], DateUtil.yyyyMMdd);
            commonQuoteDTO.setDate(tradeDate);
            commonQuoteDTO.setDateStr(row[0]);
        }
        if(StringUtils.isBlank(commonQuoteDTO.getDateStr())){
            return null;
        }
        return commonQuoteDTO;
    }


    public static List<KBarDTO> convertZghs(DataTable dataTable){
        int rows = dataTable.rows();
        List<KBarDTO> kBarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            KBarDTO commonQuoteDTO = new KBarDTO();
            commonQuoteDTO.setStartPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setEndPrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setHighestPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setLowestPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setTotalExchange(Long.valueOf(row[5])/100);
            commonQuoteDTO.setTotalExchangeMoney(Double.valueOf(row[6]).longValue());
            Date tradeDate = DateUtil.parseDate(row[0], DateUtil.yyyyMMdd);
            commonQuoteDTO.setDate(tradeDate);
            commonQuoteDTO.setDateStr(DateUtil.format(tradeDate,DateUtil.yyyy_MM_dd));
            kBarList.add(commonQuoteDTO);

        }
        return kBarList;
    }

    public static List<KBarDTO> convertSpecial(DataTable dataTable){
        int rows = dataTable.rows();
        List<KBarDTO> kBarList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            KBarDTO commonQuoteDTO = new KBarDTO();
            commonQuoteDTO.setStartPrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setEndPrice(new BigDecimal(row[2]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setHighestPrice(new BigDecimal(row[3]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setLowestPrice(new BigDecimal(row[4]).setScale(2, BigDecimal.ROUND_HALF_UP));
            commonQuoteDTO.setTotalExchange(Long.valueOf(row[5])/100);
            commonQuoteDTO.setTotalExchangeMoney(Double.valueOf(row[6]).longValue());
            Date tradeDate = analyDate(row[0]);
            commonQuoteDTO.setDate(tradeDate);
            commonQuoteDTO.setDateStr(DateUtil.format(tradeDate,DateUtil.DEFAULT_FORMAT));
            kBarList.add(commonQuoteDTO);

        }
        return kBarList;
    }


    public static Date analyDate(String dateStr){
        HashMap<String, String> monthMap = new HashMap<>();
        monthMap.put("19","01");
        monthMap.put("18","02");
        monthMap.put("17","03");
        monthMap.put("16","04");
        monthMap.put("15","05");
        monthMap.put("14","06");
        monthMap.put("13","07");
        monthMap.put("12","08");
        monthMap.put("11","09");
        monthMap.put("10","10");

        HashMap<String, String> dayMap = new HashMap<>();
        dayMap.put("17","31");
        dayMap.put("18","30");
        dayMap.put("19","29");
        dayMap.put("20","28");
        dayMap.put("21","27");
        dayMap.put("22","26");
        dayMap.put("23","25");
        dayMap.put("24","24");
        dayMap.put("25","23");
        dayMap.put("26","22");
        dayMap.put("27","21");
        dayMap.put("28","20");
        dayMap.put("29","19");
        dayMap.put("30","18");
        dayMap.put("31","17");
        dayMap.put("32","16");
        dayMap.put("33","15");
        dayMap.put("34","14");
        dayMap.put("35","13");
        dayMap.put("36","12");
        dayMap.put("37","11");
        dayMap.put("38","10");
        dayMap.put("39","09");
        dayMap.put("40","08");
        dayMap.put("41","07");
        dayMap.put("42","06");
        dayMap.put("43","05");
        dayMap.put("44","04");
        dayMap.put("45","03");
        dayMap.put("46","02");
        dayMap.put("47","01");

        if(StringUtils.isBlank(dateStr)){
            return null;
        }
        String date = null;
        if(dateStr.startsWith("1989")){
            String monthStr = dateStr.substring(6,8);
            String dayStr = dateStr.substring(10, 12);
            String hourStr = dateStr.substring(13, 18);
            date = "2020-" + monthMap.get(monthStr) + "-" + dayMap.get(dayStr) + " " + hourStr + ":00";
        }else{
            date = dateStr + ":00";
        }
        Date tradeDate = DateUtil.parseDate(date, DateUtil.DEFAULT_FORMAT);
        return tradeDate;
    }



}
