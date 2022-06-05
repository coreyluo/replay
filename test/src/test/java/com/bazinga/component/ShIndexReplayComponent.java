package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.SHIndexReplayDTO;
import com.bazinga.dto.ShIndexReplayJumpDTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.xuxueli.poi.excel.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ShIndexReplayComponent {

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private CommonReplayComponent commonReplayComponent;




    public void replayUpDown(){
        Map<String, BigDecimal> shOpenRateMap = commonReplayComponent.initShOpenRateMap();
        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20190226",DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);

        List<ShIndexReplayJumpDTO>  resultList = new ArrayList<>();
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData("999999", tradeDatePool.getTradeDate());
            if(CollectionUtils.isEmpty(list)){
                continue;
            }
            ThirdSecondTransactionDataDTO open = list.get(0);
            ThirdSecondTransactionDataDTO close = list.get(list.size() - 1);
            List<ThirdSecondTransactionDataDTO> fixList = historyTransactionDataComponent.getFixTimeData(list, "10:00");
            log.info("kbarDate{} fixSize{}",kbarDate,fixList.size());
            ThirdSecondTransactionDataDTO buy = historyTransactionDataComponent.getFixTimeDataOne(list, "10:00");
            Map<String,List<SHIndexReplayDTO>> changeMap = new HashMap<>();

            String lastDirection = "";
            for (int i = 1; i < fixList.size(); i++) {
                ThirdSecondTransactionDataDTO transactionDataDTO = fixList.get(i);
                ThirdSecondTransactionDataDTO preTransactionDataDTO = fixList.get(i-1);
                String currentDirection = "";
                if(transactionDataDTO.getTradePrice().compareTo(preTransactionDataDTO.getTradePrice())>=0){
                    currentDirection = "上涨";
                }
                if(transactionDataDTO.getTradePrice().compareTo(preTransactionDataDTO.getTradePrice())<0){
                    currentDirection = "下跌";
                }

                BigDecimal changePoint = transactionDataDTO.getTradePrice().subtract(preTransactionDataDTO.getTradePrice());
                Long changeAmount = transactionDataDTO.getTradeQuantity()*100L;
                List<SHIndexReplayDTO> shIndexReplayDTOS = changeMap.computeIfAbsent(currentDirection, k -> new ArrayList<>());
                SHIndexReplayDTO shIndexReplayDTO = new SHIndexReplayDTO();
                shIndexReplayDTO.setDirection(currentDirection);
                shIndexReplayDTO.setChangeAmount(changeAmount);
                shIndexReplayDTO.setChangePoint(changePoint);
                shIndexReplayDTOS.add(shIndexReplayDTO);

            }
            ShIndexReplayJumpDTO exportDTO = new ShIndexReplayJumpDTO();
            exportDTO.setKbarDate(kbarDate);
            exportDTO.setFirstAmount(open.getTradeQuantity() *100L);
            if(shOpenRateMap.get(kbarDate)!=null){
                exportDTO.setOpenRate(shOpenRateMap.get(kbarDate));
            }
            exportDTO.setClosePoint(close.getTradePrice());
            exportDTO.setBuyPoint(buy.getTradePrice());
            changeMap.forEach((direction,groupByList)->{
                BigDecimal totalChangePoint = BigDecimal.ZERO;
                Long totalChangeAmount = 0L;
                for (SHIndexReplayDTO shIndexReplayDTO : groupByList) {
                    totalChangeAmount = totalChangeAmount + shIndexReplayDTO.getChangeAmount();
                    totalChangePoint = totalChangePoint.add(shIndexReplayDTO.getChangePoint());
                }
                if("上涨".equals(direction)){
                    exportDTO.setUp(direction);
                    exportDTO.setUpCount(groupByList.size());
                    exportDTO.setUpChangePoint(totalChangePoint);
                    exportDTO.setUpAmount(totalChangeAmount);
                }

                if("下跌".equals(direction)){
                    exportDTO.setDown(direction);
                    exportDTO.setDownCount(groupByList.size());
                    exportDTO.setDownChangePoint(totalChangePoint);
                    exportDTO.setDownAmount(totalChangeAmount);
                }
            });
            resultList.add(exportDTO);
        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\上证分时上涨下跌聚合1000.xls");

    }



    public void replay(){

        TradeDatePoolQuery tradeQuery = new TradeDatePoolQuery();
        tradeQuery.setTradeDateFrom(DateUtil.parseDate("20190226",DateUtil.yyyyMMdd));
        tradeQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeQuery);

        List<SHIndexReplayDTO>  resultList = new ArrayList<>();
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData("999999", tradeDatePool.getTradeDate());

            List<ThirdSecondTransactionDataDTO> fixList = historyTransactionDataComponent.getFixTimeData(list, "09:36");
            log.info("kbarDate{} fixSize{}",kbarDate,fixList.size());
            int beginIndex = 0;
            if(fixList.size()>120){
                beginIndex = 10;
            }else {
                beginIndex = 12;
            }

            Map<String,List<SHIndexReplayDTO>> changeMap = new HashMap<>();

            String lastDirection = "";
            for (int i = beginIndex + 1; i < fixList.size(); i++) {
                ThirdSecondTransactionDataDTO transactionDataDTO = fixList.get(i);
                ThirdSecondTransactionDataDTO preTransactionDataDTO = fixList.get(i-1);
                String currentDirection = "";
                if(transactionDataDTO.getTradePrice().compareTo(preTransactionDataDTO.getTradePrice())>0){
                    if(transactionDataDTO.getTradeQuantity()> preTransactionDataDTO.getTradeQuantity()){
                        currentDirection = "放量上涨";
                    }
                    if(transactionDataDTO.getTradeQuantity() < preTransactionDataDTO.getTradeQuantity()){
                        currentDirection = "缩量上涨";
                    }
                }
                if(transactionDataDTO.getTradePrice().compareTo(preTransactionDataDTO.getTradePrice())<0){
                    if(transactionDataDTO.getTradeQuantity()> preTransactionDataDTO.getTradeQuantity()){
                        currentDirection = "放量下跌";
                    }
                    if(transactionDataDTO.getTradeQuantity() < preTransactionDataDTO.getTradeQuantity()){
                        currentDirection = "缩量下跌";
                    }
                }
                if(!StringUtils.isBlank(lastDirection)&& !StringUtils.isBlank(currentDirection) && !currentDirection.equals(lastDirection)){
                    BigDecimal changePoint = transactionDataDTO.getTradePrice().subtract(preTransactionDataDTO.getTradePrice());
                    Long changeAmount = transactionDataDTO.getTradeQuantity()*100L - preTransactionDataDTO.getTradeQuantity() *100L;
                    List<SHIndexReplayDTO> shIndexReplayDTOS = changeMap.computeIfAbsent(currentDirection, k -> new ArrayList<>());
                    SHIndexReplayDTO shIndexReplayDTO = new SHIndexReplayDTO();
                    shIndexReplayDTO.setDirection(currentDirection);
                    shIndexReplayDTO.setChangeAmount(changeAmount);
                    shIndexReplayDTO.setChangePoint(changePoint);
                    shIndexReplayDTOS.add(shIndexReplayDTO);
                }
                lastDirection = currentDirection;
            }
            SHIndexReplayDTO exportDTO = new SHIndexReplayDTO();
            exportDTO.setKbarDate(kbarDate);

            changeMap.forEach((direction,groupByList)->{
                BigDecimal totalChangePoint = BigDecimal.ZERO;
                Long totalChangeAmount = 0L;
                for (SHIndexReplayDTO shIndexReplayDTO : groupByList) {
                    totalChangePoint = totalChangePoint.add(shIndexReplayDTO.getChangePoint());
                    totalChangeAmount = totalChangeAmount + shIndexReplayDTO.getChangeAmount();
                }
                if("放量上涨".equals(direction)){
                    exportDTO.setDirection(direction);
                    exportDTO.setChangePoint(totalChangePoint);
                    exportDTO.setChangCount(groupByList.size());
                    exportDTO.setChangeAmount(totalChangeAmount);
                }

                if("放量下跌".equals(direction)){
                    exportDTO.setDirection2(direction);
                    exportDTO.setChangePoint2(totalChangePoint);
                    exportDTO.setChangCount2(groupByList.size());
                    exportDTO.setChangeAmount2(totalChangeAmount);
                }

                if("缩量上涨".equals(direction)){
                    exportDTO.setDirection3(direction);
                    exportDTO.setChangePoint3(totalChangePoint);
                    exportDTO.setChangCount3(groupByList.size());
                    exportDTO.setChangeAmount3(totalChangeAmount);
                }
                if("缩量下跌".equals(direction)){
                    exportDTO.setDirection4(direction);
                    exportDTO.setChangePoint4(totalChangePoint);
                    exportDTO.setChangCount4(groupByList.size());
                    exportDTO.setChangeAmount4(totalChangeAmount);
                }
            });
            resultList.add(exportDTO);
        }

        ExcelExportUtil.exportToFile(resultList, "E:\\trendData\\上证分时方向聚合.xls");

    }
}
