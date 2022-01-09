package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PlankHighUtil;
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

@Slf4j
@Component
public class MiddlePlankUpdateReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private CommonReplayComponent commonReplayComponent;

    public void invoke(){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
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
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());
        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        for (CirculateInfo circulateInfo : circulateInfos) {
          /*  if(!"002011".equals(circulateInfo.getStockCode())){
                continue;
            }*/
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setKbarDateFrom("20210401");
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);

            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<8){
                continue;
            }
            for (int i = 15; i < stockKbars.size()-1; i++) {

                // StockKbar aftstockKbar = stockKbars.get(i+1);
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar preStockKbar = stockKbars.get(i-1);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                List<StockKbar> kbarList = stockKbars.subList(i - 7, i + 1);
                List<StockKbar> kbar15List = stockKbars.subList(i - 15, i + 1);
                List<StockKbar> kbar10List = stockKbars.subList(i - 10, i + 1);
                int plank = calSerialsPlank(kbarList);
               /* if(plank<2 || plank > 4 ){
                    continue;
                }*/
                if(plank < 3){
                    continue;
                }
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }

              /*  Integer oneLinePlank = PlankHighUtil.calOneLinePlank(kbarList);
                if(plank == oneLinePlank){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
                if(oneLinePlank >= 2){
                    log.info("纯一字板stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/
                String uniqueKey = preStockKbar.getKbarDate() + SymbolConstants.UNDERLINE + stockKbar.getStockCode();

                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    log.info("新股判定 stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }
               /* BigDecimal highLowRatio = getHighLowRatio(kbar15List,stockKbar.getAdjHighPrice());
                if(highLowRatio.compareTo(new BigDecimal("1.80"))>0){
                    log.info("满足大于1.8系数 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    continue;
                }*/

                log.info("满足中位股条件 stockCode{} sellKbarDate{}", stockKbar.getStockCode(),sellStockKbar.getKbarDate());
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                if(CollectionUtils.isEmpty(list)){
                    continue;
                }
               // list = historyTransactionDataComponent.getPreOneHourData(list);
                if(CollectionUtils.isEmpty(list)|| list.size()<3){
                    log.info("分时成交行情不足stockCode{} kbarDate{}",sellStockKbar.getStockCode(),sellStockKbar.getKbarDate());
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("stockCode",sellStockKbar.getStockCode());
                map.put("stockName",sellStockKbar.getStockName());
                map.put("preTradeAmount",stockKbar.getTradeAmount());
                map.put("kbarDate",sellStockKbar.getKbarDate());
                Map<String,List<ThirdSecondTransactionDataDTO>> tempMap = new HashMap<>();
                BigDecimal sellUpperPrice = PriceUtil.calUpperPrice(sellStockKbar.getStockCode(),stockKbar.getClosePrice());

                List<ThirdSecondTransactionDataDTO> afterList = historyTransactionDataComponent.getAfterFixTimeData(list, "14:30");
                BigDecimal highPrice = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO transactionDataDTO : afterList) {
                    if(transactionDataDTO.getTradePrice().compareTo(highPrice)>0){
                        highPrice = transactionDataDTO.getTradePrice();
                    }
                }

                map.put("openRate",PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                map.put("highRate",PriceUtil.getPricePercentRate(highPrice.subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));
                map.put("closeRate",PriceUtil.getPricePercentRate(sellStockKbar.getClosePrice().subtract(stockKbar.getClosePrice()),stockKbar.getClosePrice()));

                map.put("sealType",sellStockKbar.getHighPrice().compareTo(sellStockKbar.getClosePrice())==0);
                map.put("tradeAmount",sellStockKbar.getTradeAmount());
               /* if(sellUpperPrice.compareTo(list.get(0).getTradePrice())==0){
                    continue;
                }*/

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
            Map<String,BigDecimal> openRateMap = new HashMap<>();
            BigDecimal preTradeAmount = BigDecimal.ZERO;
            BigDecimal sealTradeAmount = BigDecimal.ZERO;
            BigDecimal unSealTradeAmount = BigDecimal.ZERO;
            BigDecimal openTotalRate = BigDecimal.ZERO;
            BigDecimal closeTotalRate = BigDecimal.ZERO;
            BigDecimal highTotalRate = BigDecimal.ZERO;



            for (Map itemMap : list) {
                preTradeAmount = preTradeAmount.add(new BigDecimal(itemMap.get("preTradeAmount").toString()));
                openTotalRate = openTotalRate.add(new BigDecimal(itemMap.get("openRate").toString()));
                highTotalRate = highTotalRate.add(new BigDecimal(itemMap.get("highRate").toString()));
                closeTotalRate = closeTotalRate.add(new BigDecimal(itemMap.get("closeRate").toString()));
                boolean sealType = (boolean)itemMap.get("sealType");
                if(sealType){
                    sealTradeAmount = sealTradeAmount.add(new BigDecimal(itemMap.get("tradeAmount").toString()));
                }else {
                    unSealTradeAmount = unSealTradeAmount.add(new BigDecimal(itemMap.get("tradeAmount").toString()));
                }
            }
            map.put("preTradeAmount",preTradeAmount);
            map.put("sealTradeAmount",sealTradeAmount);
            map.put("unSealTradeAmount",unSealTradeAmount);
            map.put("shOpenRate",shOpenRateMap.get(key));
            map.put("openTotalRate",openTotalRate);
            map.put("highTotalRate",highTotalRate);
            map.put("closeTotalRate",closeTotalRate);
           // map.put("openRate",PriceUtil.getPricePercentRate(sell))
            exportList.add(map);
        });

        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\连扳大于2去新股收盘成交累计值.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        //  headList.add("stockCode");
        //  headList.add("stockName");
        headList.add("kbarDate");
        headList.add("count");
        headList.add("preTradeAmount");
        headList.add("sealTradeAmount");
        headList.add("unSealTradeAmount");
        headList.add("shOpenRate");
        headList.add("openTotalRate");
        headList.add("highTotalRate");
        headList.add("closeTotalRate");
        return headList.toArray(new String[]{});
    }

    private int calSerialsPlank(List<StockKbar> stockKbarList) {
        int planks = 1;
        int unPlanks = 0;
        for (int i = stockKbarList.size() - 1; i > 1; i--) {
            StockKbar stockKbar = stockKbarList.get(i-1);
            StockKbar preStockKbar = stockKbarList.get(i - 2);
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
    private BigDecimal calDay10Rate(List<StockKbar> kbar15List) {
        StockKbar stockKbar = kbar15List.get(kbar15List.size() - 1);
        StockKbar day10StockKbar = kbar15List.get(0);

        if(stockKbar.getAdjFactor().compareTo(day10StockKbar.getAdjFactor())==0){
            return PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(day10StockKbar.getClosePrice()),day10StockKbar.getClosePrice());
        }else {
            return PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(day10StockKbar.getAdjClosePrice()),day10StockKbar.getAdjClosePrice());
        }
    }

    private BigDecimal getHighLowRatio(List<StockKbar> kbar15List,BigDecimal adjHighPrice) {
        BigDecimal lowestPrice= kbar15List.get(0).getAdjLowPrice();
        for (StockKbar stockKbar : kbar15List) {
            if(stockKbar.getAdjLowPrice().compareTo(BigDecimal.ZERO)<0){
                lowestPrice = stockKbar.getAdjLowPrice();
            }
        }
        return adjHighPrice.divide(lowestPrice,2, BigDecimal.ROUND_HALF_UP);
    }
}
