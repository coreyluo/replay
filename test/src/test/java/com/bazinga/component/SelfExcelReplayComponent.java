package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.dto.SelfExcelImportDTO;
import com.bazinga.dto.SellReplayImportDTO;
import com.bazinga.dto.ZhuanZaiDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SelfExcelReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;


    public void zhuanzhai(){

        File file = new File("E:/excelExport/可转债(1).xlsx");
        try {
            List<ZhuanZaiDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZhuanZaiDTO.class);
            List<String> resultList = importList.stream().map(ZhuanZaiDTO::getStockCode).collect(Collectors.toList());
            log.info("{}",JSONObject.toJSONString(resultList));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void replay(){

        File file = new File("E:/excelExport/龙虎/龙虎榜1年挑选席位.xlsx");
        try {
            List<SelfExcelImportDTO> resultList = Lists.newArrayList();
            List<SelfExcelImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SelfExcelImportDTO.class);
            for (SelfExcelImportDTO importDTO : importList) {
                String stockCode = importDTO.getStockCode().substring(0,6);
                importDTO.setStockCode(stockCode);
                if(importDTO.getAbnormalName().startsWith("连续三个交易日")){
                    continue;
                }
                String dragonDate = DateUtil.format(importDTO.getDragonDate(),DateUtil.yyyyMMdd);
                Date buyDate = commonComponent.afterTradeDate(importDTO.getDragonDate());
                Date sellDate = commonComponent.afterTradeDate(buyDate);
                String buyUniqueKey = stockCode + SymbolConstants.UNDERLINE + DateUtil.format(buyDate,DateUtil.yyyyMMdd);
                StockKbar buyStockKbar = stockKbarService.getByUniqueKey(buyUniqueKey);
                String dragonUniqueKey = stockCode + SymbolConstants.UNDERLINE + dragonDate;
                StockKbar dragonKbar = stockKbarService.getByUniqueKey(dragonUniqueKey);
                if(buyStockKbar ==null){
                    log.info("为获取到K线stockCode{} kbarDate{}",stockCode,DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                    continue;
                }
                log.info("符合买入条件stockCode{} kbarDate{}",stockCode,DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                importDTO.setBuyDate(DateUtil.format(buyDate,DateUtil.yyyyMMdd));
                importDTO.setBuyPrice(buyStockKbar.getOpenPrice());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockCode, sellDate);
                List<ThirdSecondTransactionDataDTO> buyList = historyTransactionDataComponent.getData(stockCode, buyDate);
                if(!CollectionUtils.isEmpty(list)){
                    list = historyTransactionDataComponent.getMorningData(list);
                    Float sellPricef = historyTransactionDataComponent.calAveragePrice(list);
                    BigDecimal sellPrice = new BigDecimal(sellPricef.toString());
                    importDTO.setSellDate(DateUtil.format(sellDate,DateUtil.yyyyMMdd));
                    importDTO.setPremium(PriceUtil.getPricePercentRate(sellPrice.subtract(importDTO.getBuyPrice()),importDTO.getBuyPrice()));
                    ThirdSecondTransactionDataDTO open = buyList.get(0);
                    importDTO.setOpenTradeAmount(open.getTradePrice().multiply(new BigDecimal(open.getTradeQuantity()).multiply(CommonConstant.DECIMAL_HUNDRED)));
                    importDTO.setOpenRate(PriceUtil.getPricePercentRate(open.getTradePrice().subtract(dragonKbar.getClosePrice()),dragonKbar.getClosePrice()));

                    StockKbarQuery query = new StockKbarQuery();
                    query.setKbarDateTo(dragonDate);
                    query.setStockCode(stockCode);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(11);
                    List<StockKbar> kbarList = stockKbarService.listByCondition(query);
                    if(kbarList.size()<11){
                        continue;
                    }
                    importDTO.setDay3Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,3));
                    importDTO.setDay5Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,5));
                    importDTO.setDay10Rate(StockKbarUtil.getNDaysUpperRateDesc(kbarList,10));
                    PlankHighDTO plankHighDTO = PlankHighUtil.calCommonPlank(Lists.reverse(kbarList));
                    importDTO.setPlankHigh(plankHighDTO.getPlankHigh());
                    importDTO.setUnPlank(plankHighDTO.getUnPlank());

                }
                importDTO.setStockName(buyStockKbar.getStockName());
                resultList.add(importDTO);
            }
            ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\龙虎榜1年挑选席位席位次日集合买.xls");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
