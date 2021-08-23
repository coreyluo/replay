package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MiddlePlankReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public void invoke(){

        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        // excelExportUtil.set
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
      //  circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {

            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20200710");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 6; i < stockKbars.size()-2; i++) {

                StockKbar aftstockKbar = stockKbars.get(i+1);
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+2);
                List<StockKbar> kbarList = stockKbars.subList(i - 6, i + 1);
                if(StockKbarUtil.isUpperPrice(aftstockKbar,stockKbar)){
                    continue;
                }
                if(!StockKbarUtil.isUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                int plank = calSerialsPlank(kbarList);
                if(plank<2 || plank >4){
                    continue;
                }
                log.info("满足中位股条件 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    map.put(transactionDataDTO.getTradeTime(),rate);
                }
                dataList.add(map);
            }
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

       /* for (TradeDatePool tradeDatePool : tradeDatePools) {
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
           // List<Map>

        }*/
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\middlePlankduan.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
    //    headList.add("stockCode");
    //    headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("09:25");
        headList.add("09:30");
        Date date = DateUtil.parseDate("20210818093000", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
     /*   headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }*/
        return headList.toArray(new String[]{});
    }


    private int calSerialsPlank(List<StockKbar> stockKbarList) {
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
}
