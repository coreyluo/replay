package com.bazinga.component;

import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.dto.SellReplayImportDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SellReplayComponent {


    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonComponent commonComponent;


    public void replay(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = new ArrayList<>();
      //  excelExportUtil.setData(dataList);
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        File file = new File("E:/excelExport/陈持仓20211014.xlsx");
        try {
            List<SellReplayImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SellReplayImportDTO.class);
            for (SellReplayImportDTO sellReplayImportDTO : importList) {
                String currentKbarString = DateUtil.format(sellReplayImportDTO.getKbarDate(),DateUtil.yyyyMMdd);
                String uniqueKey =  sellReplayImportDTO.getStockCode() + SymbolConstants.UNDERLINE + currentKbarString;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);

                Date sellDate = commonComponent.afterTradeDate(sellReplayImportDTO.getKbarDate());
                String sellDateString = DateUtil.format(sellDate,DateUtil.yyyyMMdd);
                Map<String,Object> map = new HashMap<>();
                map.put("stockCode",sellReplayImportDTO.getStockCode());
                map.put("stockName",sellReplayImportDTO.getStockName());
                BigDecimal openPrice = BigDecimal.ZERO;
                map.put("sellDate",sellDateString);
                map.put("buyDate",currentKbarString);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellReplayImportDTO.getStockCode(), sellDateString);
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        openPrice = transactionDataDTO.getTradePrice();
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                map.put("openRate", PriceUtil.getPricePercentRate(openPrice.subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                dataList.add(map);
            }

            Map<String,List<Map>> groupByMap = new HashMap<>();

            for (Map map : dataList) {
                List<Map> mapList = groupByMap.get(map.get("sellDate").toString());
                if(mapList == null){
                    mapList = new ArrayList<>();
                }
                mapList.add(map);
                groupByMap.put(map.get("sellDate").toString(),mapList);
            }

            List<Map> exportList = Lists.newArrayList();
            groupByMap.forEach((key,list)->{
                Map map = new HashMap<>();
                map.put("sellDate",key);
                map.put("count",list.size());
                for (int i = 2; i < headList.length; i++) {
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
                FileOutputStream output=new FileOutputStream("E:\\excelExport\\卖出陈持仓20211014.xls");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public void replayStock(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = new ArrayList<>();
        //  excelExportUtil.setData(dataList);
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        File file = new File("E:/excelExport/烂板2板.xlsx");
        try {
            List<SellReplayImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(SellReplayImportDTO.class);
            for (SellReplayImportDTO sellReplayImportDTO : importList) {
                String currentKbarString = DateUtil.format(sellReplayImportDTO.getKbarDate(),DateUtil.yyyyMMdd);
                String uniqueKey =  sellReplayImportDTO.getStockCode() + SymbolConstants.UNDERLINE + currentKbarString;
                StockKbar stockKbar = stockKbarService.getByUniqueKey(uniqueKey);

                Date sellDate = commonComponent.afterTradeDate(sellReplayImportDTO.getKbarDate());
                String sellDateString = DateUtil.format(sellDate,DateUtil.yyyyMMdd);
                Map<String,Object> map = new HashMap<>();
                map.put("stockCode",sellReplayImportDTO.getStockCode());
                map.put("stockName",sellReplayImportDTO.getStockName());
                BigDecimal openPrice = BigDecimal.ZERO;
                map.put("sellDate",sellDateString);
                map.put("buyDate",currentKbarString);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellReplayImportDTO.getStockCode(), sellDateString);
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if("09:25".equals(transactionDataDTO.getTradeTime())){
                        openPrice = transactionDataDTO.getTradePrice();
                    }
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                map.put("openRate", PriceUtil.getPricePercentRate(openPrice.subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                dataList.add(map);
            }




            excelExportUtil.setData(dataList);
            excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
            excelExportUtil.writeMainData(1);

            try {
                FileOutputStream output=new FileOutputStream("E:\\excelExport\\卖出烂板2板均值.xls");
                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
     //   headList.add("stockCode");
     //   headList.add("stockName");
     //   headList.add("openRate");
        headList.add("sellDate");
        headList.add("count");
        headList.add("buyDate");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210531092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }

        date = DateUtil.parseDate("20210531125900", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }
}
