package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class IndexPeriodReplayComponent {

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);
        List<Map> exportList = Lists.newArrayList();
      /*  TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
        tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
      //  tradeDateQuery.setTradeDateTo(DateUtil.parseDate(kbarDateTo,DateUtil.yyyyMMdd));
        tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);*/
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        query.setKbarDateFrom("20180101");
        List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
        for (int i = 0; i < stockKbarList.size()-1; i++) {
            StockKbar sellStockKbar = stockKbarList.get(i+1);
            StockKbar stockKbar = stockKbarList.get(i);
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
            Map<String,Object> itemMap = new HashMap<>();

            for (int j = 1; j < headList.length; j++) {
                String period = headList[j];
                ThirdSecondTransactionDataDTO fixTimeDataOne = historyTransactionDataComponent.getFixTimeDataOne(list, period);
                BigDecimal premiumRate = PriceUtil.getPricePercentRate(sellStockKbar.getOpenPrice().subtract(fixTimeDataOne.getTradePrice()),fixTimeDataOne.getTradePrice());
                itemMap.put(period,premiumRate);
            }

            itemMap.put("kbarDate",stockKbar.getKbarDate());

            exportList.add(itemMap);
        }
        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);
        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\上证切片收益.xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();

        headList.add("kbarDate");
       // headList.add("09:50");
        Date date = DateUtil.parseDate("20210818093000", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 6){
            date = DateUtil.addMinutes(date, 10);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));

        }
        return headList.toArray(new String[]{});
    }
}
