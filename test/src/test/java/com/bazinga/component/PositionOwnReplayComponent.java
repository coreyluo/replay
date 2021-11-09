package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.CirculateInfoExcelDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.*;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.ExcelExportUtil;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PositionOwnReplayComponent {

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockKbarService stockKbarService;


    public void replayReplankTwo(){

        File file = new File("E:/excelExport/own518.xlsx");
        try {
            List<PositionOwnImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(PositionOwnImportDTO.class);
            for (PositionOwnImportDTO stockPosition : importList) {
                String kbarDate = DateUtil.format(stockPosition.getKbarDate(),DateUtil.yyyyMMdd);

                StockKbarQuery query = new StockKbarQuery();
                query.setStockCode(stockPosition.getStockCode());
                query.setKbarDateTo(kbarDate);
                query.addOrderBy("kbar_date", Sort.SortType.DESC);
                query.setLimit(7);
                List<StockKbar> kbarList = stockKbarService.listByCondition(query);

                if(kbarList.size()<7){
                    continue;
                }
                if(isReplankTwo(kbarList)){
                    log.info("反包二板 stockCode{} kbarDate{}",stockPosition.getStockCode(),kbarDate);
                }

            }

        } catch (Exception e) {
            throw new BusinessException("文件解析及同步异常", e);
        }

    }

    private boolean isReplankTwo(List<StockKbar> kbarList) {

        StockKbar pre1StockKbar = kbarList.get(1);
        StockKbar pre2StockKbar = kbarList.get(2);
        StockKbar pre3StockKbar = kbarList.get(3);
        StockKbar pre4StockKbar = kbarList.get(4);
        StockKbar pre5StockKbar = kbarList.get(5);
        StockKbar pre6StockKbar = kbarList.get(6);

        if(!StockKbarUtil.isUpperPrice(pre1StockKbar,pre2StockKbar)){
            return false;
        }
        if(StockKbarUtil.isUpperPrice(pre2StockKbar,pre3StockKbar)){
            return false;
        }
        if(StockKbarUtil.isUpperPrice(pre3StockKbar,pre4StockKbar)){
            return false;
        }
        if(!StockKbarUtil.isUpperPrice(pre4StockKbar,pre5StockKbar)){
            return false;
        }
        if(!StockKbarUtil.isUpperPrice(pre5StockKbar,pre6StockKbar)){
            return false;
        }
        return true;
    }



    public void replay(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        File file = new File("E:/excelExport/陈1109.xlsx");
        try {
            List<PositionOwnImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(PositionOwnImportDTO.class);
            log.info("data{}", JSONObject.toJSONString(dataList));

            for (PositionOwnImportDTO stockPosition : importList) {
                String kbarDate = DateUtil.format(stockPosition.getKbarDate(),DateUtil.yyyyMMdd);
            /*    if(!"20210728".equals(kbarDate)){
                    continue;
                }*/
                Date afterTradeDate = commonComponent.afterTradeDate(stockPosition.getKbarDate());
                log.info("满足中条件 stockCode{} kbarDate{}", stockPosition.getStockCode(),kbarDate);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockPosition.getStockCode(), afterTradeDate);
                list = historyTransactionDataComponent.getPreHalfOneHourData(list);
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("kbarDate",kbarDate);
                map.put("seal",stockPosition.getSealType());
                String uniqueKey = stockPosition.getStockCode() + SymbolConstants.UNDERLINE + kbarDate;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();

                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    if(!"09:25".equals(transactionDataDTO.getTradeTime())){
                        List<ThirdSecondTransactionDataDTO> minList = tempMap.get(transactionDataDTO.getTradeTime());
                        if(minList == null){
                            minList = new ArrayList<>();
                            minList.add(transactionDataDTO);
                            tempMap.put(transactionDataDTO.getTradeTime(),minList);
                        }else {
                            minList.add(transactionDataDTO);
                        }
                    }else {
                        map.put(transactionDataDTO.getTradeTime(),rate);

                    }
                }
                tempMap.forEach((minStr,minlist)->{
                    for (int j = 0; j < minlist.size(); j++) {
                        ThirdSecondTransactionDataDTO transactionDataDTO = minlist.get(j);
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                        map.put(minStr+ SymbolConstants.UNDERLINE + (j+1),rate);
                    }
                    if(minlist.size()<20){
                        ThirdSecondTransactionDataDTO transactionDataDTO = minlist.get(minlist.size()-1);
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                        for (int j = 20; j > minlist.size() ; j--) {
                            map.put(minStr+ SymbolConstants.UNDERLINE + j,rate);
                        }
                    }
                });
                dataList.add(map);
            }

            Map<String,List<Map>> groupByMap = new HashMap<>();

            for (Map map : dataList) {
                List<Map> mapList = groupByMap.get(map.get("kbarDate").toString());
                if(mapList == null){
                    mapList = new ArrayList<>();
                }
                mapList.add(map);
                groupByMap.put(map.get("kbarDate").toString(),mapList);
            }
            List<Map> exportList = Lists.newArrayList();
            groupByMap.forEach((key,list)->{
                Map map = new HashMap<>();
                map.put("kbarDate",key);
                map.put("count",list.size());
                List<Map> sealMap  = list.stream().filter(itemMap -> "1".equals(itemMap.get("seal").toString())).collect(Collectors.toList());
                map.put("sealPr",new BigDecimal(sealMap.size()*100).divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
                for (int i = 3; i < headList.length; i++) {
                    String attrKey = headList[i];
                    BigDecimal totalRate = BigDecimal.ZERO;
                    BigDecimal preRate = BigDecimal.ZERO;
                    for (Map itemMap : list) {
                        BigDecimal rate = itemMap.get(attrKey) == null ? preRate:new BigDecimal(itemMap.get(attrKey).toString());
                        totalRate = totalRate.add(rate);
                        if(itemMap.get(attrKey) != null){
                            preRate = new BigDecimal(itemMap.get(attrKey).toString());
                        }
                    }
                    map.put(attrKey,totalRate.divide(new BigDecimal(list.size()),2,BigDecimal.ROUND_HALF_UP));
                }
                exportList.add(map);
            });
            excelExportUtil.setData(exportList);
            excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
            excelExportUtil.writeMainData(1);
            try {
                FileOutputStream output=new FileOutputStream("E:\\excelExport\\1109.xls");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new BusinessException("文件解析及同步异常", e);
        }

    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        //    headList.add("stockCode");
        //    headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("sealPr");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 10){
            date = DateUtil.addMinutes(date, 1);
            count++;
            for (int i = 1; i < 21; i++) {
                headList.add(DateUtil.format(date,"HH:mm") + SymbolConstants.UNDERLINE +i);
            }
        }
        return headList.toArray(new String[]{});
    }

}
