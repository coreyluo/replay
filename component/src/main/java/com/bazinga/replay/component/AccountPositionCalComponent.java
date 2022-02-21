package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankHighDTO;
import com.bazinga.replay.dto.PositionCalDTO;
import com.bazinga.replay.dto.SellGroupDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.PlankHighUtil;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AccountPositionCalComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;

    private static Map<String,String> ACCOUNT_NAME_MAP = new HashMap<>();

    static {
        ACCOUNT_NAME_MAP.put("398000086400","老窝");
        ACCOUNT_NAME_MAP.put("398000103912","赵");
        ACCOUNT_NAME_MAP.put("398000131333","杜");
        ACCOUNT_NAME_MAP.put("398000104352","产品");
        ACCOUNT_NAME_MAP.put("398000104348","大佬");
        ACCOUNT_NAME_MAP.put("398000102550","问");
        ACCOUNT_NAME_MAP.put("398000104865","产品二");
    }

    public void cal(String preName){
        Date currentTradeDate = commonComponent.getCurrentTradeDate();
        //currentTradeDate = DateUtil.parseDate("20220211",DateUtil.yyyyMMdd);
        String kbarDate = DateUtil.format(currentTradeDate,DateUtil.yyyyMMdd);
        Date preTradeDate = commonComponent.preTradeDate(currentTradeDate);
        String preKbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
        List<PositionCalDTO> resultList = Lists.newArrayList();

        try {
            File orderFile = new File("E:\\positionCal\\"+preName+"_当日委托_"+kbarDate+".csv");
            List<String> orderList = FileUtils.readLines(orderFile, "GBK");
            File importFile = new File("E:\\positionCal\\收益\\"+ACCOUNT_NAME_MAP.get(preName)+SymbolConstants.UNDERLINE +"收益"+preKbarDate+".xls");
            List<PositionCalDTO> importList = new Excel2JavaPojoUtil(importFile).excel2JavaPojo(PositionCalDTO.class);


            Map<String, SellGroupDTO> sellAmountMap = new HashMap<>();
            for (int i = 0; i < orderList.size(); i++) {
                String objectString = orderList.get(i);
                String[] objArr = objectString.split(SymbolConstants.COMMA);
                String stockCode =  objArr[1];
                String direction = objArr[3];
                String tradeTime = objArr[14];
                if("-".equals(tradeTime)){
                    continue;
                }
                if("卖出".equals(direction)){
                    SellGroupDTO sellGroupDTO = sellAmountMap.get(stockCode);
                    if(sellGroupDTO==null){
                        sellGroupDTO = new SellGroupDTO();
                        sellGroupDTO.setStockCode(stockCode);
                        sellGroupDTO.setSellQuantity(Integer.valueOf(objArr[7]));
                        sellGroupDTO.setSellAmount(new BigDecimal(objArr[8]));
                        sellAmountMap.put(stockCode,sellGroupDTO);
                    } else {
                        sellGroupDTO.setSellQuantity(sellGroupDTO.getSellQuantity() + Integer.valueOf(objArr[7]));
                        sellGroupDTO.setSellAmount(sellGroupDTO.getSellAmount().add(new BigDecimal(objArr[8])));
                    }
                }
            }

            log.info("卖出聚合结果{}", JSONObject.toJSONString(sellAmountMap));

            Map<String, List<PositionCalDTO>> unResultMap = importList.stream().filter(item -> item.getPremiumRate() == null).collect(Collectors.groupingBy(PositionCalDTO::getStockCode));

            unResultMap.forEach((stockCode,unResultList)->{

                SellGroupDTO sellGroupDTO = sellAmountMap.get(stockCode);
                if(sellGroupDTO==null){
                    log.info("当前未结算票没有卖出记录stockCode{}",stockCode);
                }else {
                    int unCalQuantiy = unResultList.stream().mapToInt(PositionCalDTO::getTradeQuantity).sum();
                    if(sellGroupDTO.getSellQuantity().equals(unCalQuantiy)){
                        log.info("满足结算条件stockCode{}",stockCode);
                        if(unResultList.size()==1){
                            PositionCalDTO positionCalDTO = unResultList.get(0);
                            positionCalDTO.setTradeQuantity(0);
                            if(positionCalDTO.getSellAmount()==null){
                                positionCalDTO.setSellAmount(sellGroupDTO.getSellAmount());
                            }else {
                                positionCalDTO.setSellAmount(positionCalDTO.getSellAmount().add(sellGroupDTO.getSellAmount()));
                            }
                            positionCalDTO.setPremium(positionCalDTO.getSellAmount().subtract(positionCalDTO.getBuyAmount()));
                            positionCalDTO.setPremiumRate(PriceUtil.getPricePercentRate(positionCalDTO.getPremium(),positionCalDTO.getBuyAmount()).divide(CommonConstant.DECIMAL_HUNDRED,4,RoundingMode.HALF_UP));
                        }else {
                            for (int i = 0; i < unResultList.size(); i++) {
                                PositionCalDTO positionCalDTO = unResultList.get(i);
                                BigDecimal quantityRate = new BigDecimal(positionCalDTO.getTradeQuantity().toString()).divide(new BigDecimal(String.valueOf(unCalQuantiy)),3, RoundingMode.HALF_UP);
                                positionCalDTO.setTradeQuantity(0);
                                BigDecimal sellAmount = sellGroupDTO.getSellAmount().multiply(quantityRate).setScale(2, RoundingMode.HALF_UP);
                                if(positionCalDTO.getSellAmount()==null){
                                    positionCalDTO.setSellAmount(sellAmount);
                                }else {
                                    positionCalDTO.setSellAmount(positionCalDTO.getSellAmount().add(sellAmount));
                                }
                                positionCalDTO.setPremium(positionCalDTO.getSellAmount().subtract(positionCalDTO.getBuyAmount()));
                                positionCalDTO.setPremiumRate(PriceUtil.getPricePercentRate(positionCalDTO.getPremium(),positionCalDTO.getBuyAmount()).divide(CommonConstant.DECIMAL_HUNDRED,4,RoundingMode.HALF_UP));
                            }
                        }
                    }else {
                        log.info("不满足结算条件需要更新卖出金额stockCode{}",stockCode);
                        if(unResultList.size()==1){
                            PositionCalDTO positionCalDTO = unResultList.get(0);
                            positionCalDTO.setTradeQuantity(positionCalDTO.getTradeQuantity()-sellGroupDTO.getSellQuantity());
                            if(positionCalDTO.getSellAmount()==null){
                                positionCalDTO.setSellAmount(sellGroupDTO.getSellAmount());
                            }else {
                                positionCalDTO.setSellAmount(positionCalDTO.getSellAmount().add(sellGroupDTO.getSellAmount()));
                            }
                        }else {
                            Integer totalSubQuantity = 0;
                            for (int i = 0; i < unResultList.size(); i++) {
                                PositionCalDTO positionCalDTO = unResultList.get(i);
                                BigDecimal quantityRate = new BigDecimal(positionCalDTO.getTradeQuantity().toString()).divide(new BigDecimal(String.valueOf(unCalQuantiy)),3, RoundingMode.HALF_UP);
                                Integer subtractQuantity = new BigDecimal(sellGroupDTO.getSellQuantity().toString()).multiply(quantityRate).intValue();
                                totalSubQuantity = totalSubQuantity + subtractQuantity;
                                if(i== unResultList.size()-1){
                                    positionCalDTO.setTradeQuantity(positionCalDTO.getTradeQuantity()-(sellGroupDTO.getSellQuantity()- totalSubQuantity));
                                }else {
                                    positionCalDTO.setTradeQuantity(positionCalDTO.getTradeQuantity()- subtractQuantity);
                                }
                                BigDecimal sellAmount = sellGroupDTO.getSellAmount().multiply(quantityRate).setScale(2, RoundingMode.HALF_UP);
                                if(positionCalDTO.getSellAmount()==null){
                                    positionCalDTO.setSellAmount(sellAmount);
                                }else {
                                    positionCalDTO.setSellAmount(positionCalDTO.getSellAmount().add(sellAmount));
                                }
                            }
                        }


                    }

                }


            });

            resultList.addAll(importList);
            Map<String,PositionCalDTO> absortMap = new HashMap<>();
            Map<String,PositionCalDTO> plankMap = new HashMap<>();
            for (int i = 1; i < orderList.size(); i++) {

                String objectString = orderList.get(i);
                String[] objArr = objectString.split(SymbolConstants.COMMA);
                String direction = objArr[3];
                String tradeTime = objArr[14];
                if("-".equals(tradeTime)){
                    continue;
                }
                if("买入".equals(direction)){
                    PositionCalDTO positionCalDTO = new PositionCalDTO();
                    positionCalDTO.setTradeDate(DateUtil.format(currentTradeDate,DateUtil.yyyyMMdd));
                    positionCalDTO.setOrderTime(objArr[0]);
                    positionCalDTO.setStockCode(objArr[1]);
                    if(positionCalDTO.getStockCode().startsWith("1")){
                        continue;
                    }
                    positionCalDTO.setStockName(objArr[2]);
                    positionCalDTO.setTradeQuantity(Integer.valueOf(objArr[7]));
                    positionCalDTO.setBuyAmount(new BigDecimal(objArr[8]));
                    BigDecimal orderPrice = new BigDecimal(objArr[5]);
                    positionCalDTO.setTradeTime(tradeTime);
                    Date orderDate = DateUtil.parseDate(positionCalDTO.getOrderTime().startsWith("9")?"0"+positionCalDTO.getOrderTime():positionCalDTO.getOrderTime(), DateUtil.HH_MM_SS);
                    Date tradeDate = DateUtil.parseDate(positionCalDTO.getTradeTime().startsWith("9")?"0"+positionCalDTO.getTradeTime():positionCalDTO.getTradeTime(), DateUtil.HH_MM_SS);
                    long subTimeLong = tradeDate.getTime() - orderDate.getTime();
                    long hour = subTimeLong/(1000*60*60);
                    long min = subTimeLong%(1000*60*60)/(1000*60);
                    long second = subTimeLong%(1000*60)/(1000);

                    String subtractTime = hour + ":" +(min>9?min:"0"+min)+":"+ (second>9?second:"0"+second);
                    positionCalDTO.setSubtractTime(subtractTime);

                    String uniqueKey = positionCalDTO.getStockCode() +SymbolConstants.UNDERLINE + kbarDate;
                    StockKbar byUniqueKey = stockKbarService.getByUniqueKey(uniqueKey);
                    if(byUniqueKey ==null){
                        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, positionCalDTO.getStockCode(), 0, 1);
                        List<StockKbar> stockKbarList = KBarDTOConvert.convertStockKbar(positionCalDTO.getStockCode(),securityBars);
                        if(CollectionUtils.isEmpty(stockKbarList)){
                            throw new Exception(positionCalDTO.getStockCode());
                        }else {
                            byUniqueKey = stockKbarList.get(0);
                        }
                    }
                    StockKbarQuery query = new StockKbarQuery();
                    query.setStockCode(positionCalDTO.getStockCode());
                    query.setKbarDateTo(kbarDate);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(10);
                    List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                    if(kbarList.size()==0){
                        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, positionCalDTO.getStockCode(), 0, 10);
                        kbarList= KBarDTOConvert.convertStockKbar(positionCalDTO.getStockCode(),securityBars);
                    }
                    StockKbar stockKbar = kbarList.get(0);
                    StockKbar preStockKbar = kbarList.get(1);
                    PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(Lists.reverse(kbarList));
                    Integer sealType=0;
                    if(StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                        sealType = byUniqueKey.getClosePrice().compareTo(orderPrice)==0?1:0;
                        if(orderPrice.compareTo(stockKbar.getHighPrice())==0){
                            positionCalDTO.setBuyStrategy("打板");
                            if (doMultiOrder(plankMap, positionCalDTO)){
                                continue;
                            }
                        }else {
                            positionCalDTO.setBuyStrategy("低吸");
                        }
                    }else {
                        positionCalDTO.setBuyStrategy("低吸");
                    }
                    if("低吸".equals(positionCalDTO.getBuyStrategy())){
                        if (doMultiOrder(absortMap, positionCalDTO)){
                            continue;
                        }
                        positionCalDTO.setPlankHigh(plankHighDTO.getPlankHigh()-1 + "+");
                    }else {
                        if(plankHighDTO.getUnPlank()==0){
                            positionCalDTO.setPlankHigh(plankHighDTO.getPlankHigh().toString());
                        }else {
                            int plank = PlankHighUtil.calTodaySerialsPlank(Lists.reverse(kbarList));
                            positionCalDTO.setPlankHigh(plankHighDTO.getPlankHigh()-plank + "+"+ plank);
                        }
                        positionCalDTO.setMode("自动化普打");
                    }
                    positionCalDTO.setSealType(sealType);
                    positionCalDTO.setAccountName(preName);

                    resultList.add(positionCalDTO);
                }



            }
            log.info("");
            ExcelExportUtil.exportToFile(resultList, "E:\\positionCal\\收益\\"+ACCOUNT_NAME_MAP.get(preName)+SymbolConstants.UNDERLINE +"收益"+kbarDate+".xls");

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }


    }

    private boolean doMultiOrder(Map<String, PositionCalDTO> cacheMap, PositionCalDTO positionCalDTO) {
        PositionCalDTO orignalDTO = cacheMap.get(positionCalDTO.getStockCode());
        if(orignalDTO !=null){
            BigDecimal buyAmount = positionCalDTO.getBuyAmount();
            orignalDTO.setBuyAmount(orignalDTO.getBuyAmount().add(buyAmount));
            orignalDTO.setTradeQuantity(orignalDTO.getTradeQuantity() + positionCalDTO.getTradeQuantity());
            return true;
        }else {
            cacheMap.put(positionCalDTO.getStockCode(),positionCalDTO);
        }
        return false;
    }

}
