package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.dto.RankDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.StockKbarService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StockReplayByGroupComponent {

    @Autowired
    private RedisMoniorService redisMoniorService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void replay(String kbarDateFrom ,String kbarDateTo){
        Workbook workbook = ExcelExportUtil.creatWorkBook("XLS");
        ExcelExportUtil excelExportUtil = new ExcelExportUtil();
        excelExportUtil.setWorkbook(workbook);
        excelExportUtil.setTitle("");
        excelExportUtil.setSheet(workbook.createSheet("sheet1"));

        Map<String,List<RankDTO>> resultMap = new HashMap<>();
        String[] headList = getHeadList();
        excelExportUtil.setHeadKey(headList);
        String redisKey = "OPEN_AMOUNT_RANK"+ kbarDateFrom + SymbolConstants.UNDERLINE + kbarDateTo;
        RedisMonior byRedisKey = redisMoniorService.getByRedisKey(redisKey);
        if(byRedisKey!=null){
            JSONObject jsonObject = JSONObject.parseObject(byRedisKey.getRedisValue());
            jsonObject.forEach((key,value)->{
                resultMap.put(key,JSONObject.parseArray(value.toString(), RankDTO.class));
            });
        }
        List<Map> exportList = Lists.newArrayList();

        resultMap.forEach((kbarDate,rankList)->{
           // if("20220309".equals(kbarDate)){
                Map<String,BigDecimal> tempRateMap = new HashMap<>();
                for (RankDTO rankDTO : rankList) {
                    if(rankDTO.getRank()>100){
                        continue;
                    }
                    StockKbarQuery query = new StockKbarQuery();
                    query.setStockCode(rankDTO.getStockCode());
                    query.setKbarDateTo(kbarDate);
                    query.addOrderBy("kbar_date", Sort.SortType.DESC);
                    query.setLimit(2);
                    List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
                    if(CollectionUtils.isEmpty(stockKbarList)){
                        continue;
                    }
                    StockKbar preStockKbar = stockKbarList.get(0);
                    List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(rankDTO.getStockCode(), kbarDate);
                    for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                        tempRateMap.put(rankDTO.getStockCode() + transactionDataDTO.getTradeTime(),rate);
                    }
                }
                Map map = new HashMap<>();
                map.put("kbarDate",kbarDate);
                log.info("kbarDate{} 完成",kbarDate);
                for (int i =1; i < headList.length; i++) {
                    String attrKey = headList[i];
                    BigDecimal totalRate = BigDecimal.ZERO;
                    for (RankDTO rankDTO : rankList) {
                        BigDecimal rate = tempRateMap.get(rankDTO.getStockCode() + attrKey);
                        if(rate !=null){
                            totalRate = totalRate.add(rate);
                        }
                    }
                    map.put(attrKey,totalRate);
                }
                exportList.add(map);
          //  }

        });


        excelExportUtil.setData(exportList);
        excelExportUtil.writeTableHead(headList,workbook.createCellStyle(), 0);
        excelExportUtil.writeMainData(1);

        try {
            FileOutputStream output=new FileOutputStream("E:\\excelExport\\竞价100聚合走势"+kbarDateFrom+"_"+kbarDateTo+".xls");
            workbook.write(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("kbarDate");
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 30){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }


}
