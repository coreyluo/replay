package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.PlankHighDTO;
import com.bazinga.replay.component.CommonComponent;
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
import com.bazinga.util.PlankHighUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.bazinga.util.PlankHighUtil.calSerialsPlank;
import static com.bazinga.util.PlankHighUtil.isTodayFirstPlank;

@Component
@Slf4j
public class MarketPlankReplayComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CommonComponent commonComponent;

    public void replay(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        List<Map> dataList = Lists.newArrayList();
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);

        TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20210418",DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);

        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        circulateInfos = circulateInfos.stream().filter(item->!item.getStockCode().startsWith("3")).collect(Collectors.toList());

        for (CirculateInfo circulateInfo : circulateInfos) {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.setKbarDateFrom("20210501");
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars) || stockKbars.size()<15){
                continue;
            }
            for (int i = 7; i < stockKbars.size()-1; i++) {
                StockKbar stockKbar = stockKbars.get(i);
                StockKbar sellStockKbar = stockKbars.get(i+1);
                StockKbar preStockKbar = stockKbars.get(i-1);
                if(!StockKbarUtil.isHighUpperPrice(stockKbar,preStockKbar)){
                    continue;
                }
                if(commonComponent.isNewStock(stockKbar.getStockCode(),stockKbar.getKbarDate())){
                    continue;
                }
                PlankHighDTO plankHighDTO = PlankHighUtil.calTodayPlank(stockKbars.subList(i - 7, i + 1));
                if(plankHighDTO.getPlankHigh()>0 && plankHighDTO.getPlankHigh()<3 && plankHighDTO.getUnPlank()==0){

                    List<ThirdSecondTransactionDataDTO> todayList = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                    if(CollectionUtils.isEmpty(todayList)){
                        continue;
                    }
                    boolean upperSFlag = false;
                    String plankTime="";
                    ThirdSecondTransactionDataDTO open = todayList.get(0);
                    if(open.getTradePrice().compareTo(stockKbar.getHighPrice())==0){
                        open.setTradeType(1);
                    }
                    for (int j = 1; j < todayList.size(); j++) {
                        ThirdSecondTransactionDataDTO transactionDataDTO = todayList.get(j);
                        ThirdSecondTransactionDataDTO preTransactionDataDTO = todayList.get(j-1);
                        if(transactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice()) ==0 && transactionDataDTO.getTradeType() ==1){
                            if(preTransactionDataDTO.getTradePrice().compareTo(stockKbar.getHighPrice())<0 || preTransactionDataDTO.getTradeType()!=1){
                                log.info("出现涨停S stockCode{} kbarDate{}",stockKbar.getStockCode(),stockKbar.getKbarDate());
                                upperSFlag = true;
                                plankTime = transactionDataDTO.getTradeTime();
                                break;
                            }
                        }

                    }
                    if(!upperSFlag){
                        continue;
                    }

                    int plankInteger= Integer.parseInt(plankTime.replaceAll(":", ""));
                    if(plankInteger >= 1400){
                        continue;
                    }

                    log.info("满足前半小时首板二板条件 stockCode{} kbarDate{}", stockKbar.getStockCode(),stockKbar.getKbarDate());
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(sellStockKbar.getStockCode(), sellStockKbar.getKbarDate());
                    Map<String, Object> map = new HashMap<>();
                    map.put("stockCode",stockKbar.getStockCode());
                    map.put("stockName",stockKbar.getStockName());
                    map.put("kbarDate",sellStockKbar.getKbarDate());
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                        map.put(transactionDataDTO.getTradeTime(),rate);
                    }
                    dataList.add(map);


                }

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
        List<Map> mapList = groupByMap.get("20211122");
        List<Object> stockList = mapList.stream().map(item -> item.get("stockCode")).collect(Collectors.toList());
        log.info("具体个股{}", JSONObject.toJSONString(stockList));
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
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\市场2点前首板不含新股.xls");
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
        headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }
}
