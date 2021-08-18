package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.StockReplayDailyQuery;
import com.bazinga.replay.service.PlankExchangeDailyService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.StockReplayDailyService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockReplayDailyComponent {
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private StockReplayDailyService stockReplayDailyService;

    public void calPreDateAvgPrice(Date date){
        Date preTradeDate = commonComponent.preTradeDate(date);
        StockReplayDailyQuery query = new StockReplayDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(preTradeDate));
        query.setTradeDateTo(DateTimeUtils.getDate235959(preTradeDate));
        List<StockReplayDaily> dailys = stockReplayDailyService.listByCondition(query);
        if(CollectionUtils.isEmpty(dailys)){
            return;
        }
        for (StockReplayDaily daily:dailys){
            List<ThirdSecondTransactionDataDTO> data = currentDayTransactionDataComponent.getData(daily.getStockCode());
            if(CollectionUtils.isEmpty(data)){
                data = currentDayTransactionDataComponent.getData(daily.getStockCode());
            }
            if(CollectionUtils.isEmpty(data)){
                continue;
            }
            List<KBarDTO> stockKBars = getStockKBars(daily.getStockCode());
            if(CollectionUtils.isEmpty(stockKBars)||stockKBars.size()<2){
                continue;
            }
            KBarDTO kBar = stockKBars.get(stockKBars.size() - 1);
            KBarDTO preKbar = stockKBars.get(stockKBars.size() - 2);
            if(!preKbar.getDateStr().equals(DateUtil.format(daily.getTradeDate(),DateUtil.yyyy_MM_dd))){
                continue;
            }
            BigDecimal avgPrice = null;
            boolean upperPrice = PriceUtil.isUpperPrice(daily.getStockCode(), kBar.getStartPrice(), kBar.getEndPrice());
            if(upperPrice) {
                avgPrice = PriceUtil.getPricePercentRate(kBar.getStartPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
            }else {
                Float avgPriceValue = currentDayTransactionDataComponent.calAveragePrice(data);
                float pricePercentRate = PriceUtil.getPricePercentRate(avgPriceValue - preKbar.getHighestPrice().floatValue(), preKbar.getHighestPrice().floatValue());
                avgPrice = new BigDecimal(pricePercentRate).setScale(2,BigDecimal.ROUND_HALF_UP);
            }
            daily.setSellAvg(avgPrice);
            stockReplayDailyService.updateById(daily);
        }
    }

    public void stockReplayDaily(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        for (CirculateInfo circulateInfo:circulateInfos){
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            try {
               /* if (!stockCode.equals("002218")) {
                    continue;
                }*/
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo.getStockCode());
                log.info("复盘数据 k线数据 stockCode:{} stockName:{} kbars:{}", stockCode, stockName, JSONObject.toJSONString(stockKBars));
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                if(!stockKBars.get(stockKBars.size()-1).getDateStr().equals(DateUtil.format(date,DateUtil.yyyy_MM_dd))){
                    log.info("复盘数据 没有获取到当日k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                PlankTypeDTO plankTypeDTO = continuePlankTypeDto(stockKBars, circulateInfo);
                if(plankTypeDTO.isPlank()&&plankTypeDTO.isThirdSecondTransactionIsPlank()){
                    StockReplayDailyQuery query = new StockReplayDailyQuery();
                    query.setStockCode(stockCode);
                    query.setTradeDateFrom(date);
                    query.setTradeDateTo(DateTimeUtils.getDate235959(date));
                    List<StockReplayDaily> dailys = stockReplayDailyService.listByCondition(query);
                    StockReplayDaily daily = new StockReplayDaily();
                    if(!CollectionUtils.isEmpty(dailys)) {
                        daily.setId(dailys.get(0).getId());
                    }
                    daily.setStockCode(circulateInfo.getStockCode());
                    daily.setStockName(circulateInfo.getStockName());
                    daily.setTradeDate(date);
                    int endPlankStatus = plankTypeDTO.isEndPlank() ? 1 : 0;
                    daily.setEndStatus(endPlankStatus);
                    int openPlankStatus = plankTypeDTO.isOpenPlankStatus() ? 1 : 0;
                    daily.setOpenPlankStatus(openPlankStatus);
                    if(!plankTypeDTO.isBeautifulPlank()) {
                        daily.setBeautifulPlankStatus(0);
                        daily.setInsertTime(plankTypeDTO.getInsertTime());
                        getStockPlankType(daily, plankTypeDTO);
                        if (daily.getId() == null) {
                            stockReplayDailyService.save(daily);
                        } else {
                            stockReplayDailyService.updateById(daily);
                        }
                    }
                }
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }

    public String getInsertTime(String stockCode,BigDecimal yesterdayPrice,List<ThirdSecondTransactionDataDTO> list){
        String insertTime = currentDayTransactionDataComponent.insertTime(yesterdayPrice,stockCode,list);
        return insertTime;

    }




    public void getStockPlankType(StockReplayDaily daily,PlankTypeDTO plankTypeDTO){
        int currentPlanks = plankTypeDTO.getPlanks();
        int beforePlanks = plankTypeDTO.getBeforePlanks();
        int space = plankTypeDTO.getSpace();
        int breakDays = space-1;
        if(beforePlanks==0){
            breakDays   = 0;
        }
        daily.setPlankDays(currentPlanks+beforePlanks);
        daily.setBreakDays(breakDays);
        if(breakDays==0){
            String type = daily.getPlankDays()+"连板";
            daily.setPlankType(type);
        }else{
            String type = (daily.getPlankDays()+1)+"天"+(daily.getPlankDays())+"板";
            daily.setPlankType(type);
        }
    }

    public List<KBarDTO> getStockKBars(String stockCode){
        try {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 50);
            List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
            List<KBarDTO> list = deleteNewStockTimes(kbars, 50, stockCode);
            return list;
        }catch (Exception e){
            return null;
        }
    }


    //包括新股最后一个一字板
    public List<KBarDTO> deleteNewStockTimes(List<KBarDTO> list,int size,String stockCode){
        List<KBarDTO> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        if(list.size()<size){
            KBarDTO firstDTO = null;
            BigDecimal preEndPrice = null;
            int i = 0;
            for (KBarDTO dto:list){
                if(preEndPrice!=null&&i==0){
                    boolean endPlank = PriceUtil.isUpperPrice(dto.getEndPrice(), preEndPrice);
                    if(MarketUtil.isChuangYe(stockCode)&&!dto.getDate().before(DateUtil.parseDate("2020-08-24",DateUtil.yyyy_MM_dd))){
                        endPlank = PriceUtil.isUpperPrice(stockCode,dto.getEndPrice(),preEndPrice);
                    }
                    if(!(dto.getHighestPrice().equals(dto.getLowestPrice())&&endPlank)){
                        datas.add(firstDTO);
                        i++;
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }

                if(i==0){
                    firstDTO = dto;
                }
                preEndPrice = dto.getEndPrice();
            }
        }else{
            return list;
        }
        return datas;
    }

    //判断连板天数
    public PlankTypeDTO continuePlankTypeDto(List<KBarDTO> kbars, CirculateInfo circulateInfo){
        PlankTypeDTO plankTypeDTO = new PlankTypeDTO();
        plankTypeDTO.setPlank(false);
        plankTypeDTO.setEndPlank(false);
        if(kbars.size()<2){
            return plankTypeDTO;
        }
        List<KBarDTO> reverse = Lists.reverse(kbars);
        BigDecimal preEndPrice = null;
        BigDecimal preHighPrice = null;
        BigDecimal preLowerPrice = null;
        BigDecimal preOpenPrice = null;
        Date preDate = null;
        int space = 0;
        int current = 0;
        int before = 0;
        int i=0;
        for (KBarDTO kBarDTO:reverse){
            i++;
            if(preEndPrice!=null) {
                BigDecimal endPrice = kBarDTO.getEndPrice();
                boolean highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preHighPrice,endPrice);
                boolean endPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preEndPrice,endPrice);
                boolean openPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preOpenPrice,endPrice);
                if(i==2){
                    if(highPlank){
                        plankTypeDTO.setOpenPlankStatus(openPlank);
                        if(preLowerPrice.equals(preHighPrice)){
                            plankTypeDTO.setBeautifulPlank(true);
                            plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                        }else{
                            List<ThirdSecondTransactionDataDTO> list = currentDayTransactionDataComponent.getData(circulateInfo.getStockCode());
                            if(CollectionUtils.isEmpty(list)){
                                list = currentDayTransactionDataComponent.getData(circulateInfo.getStockCode());
                            }
                            int transactionDataPlank = currentDayTransactionDataComponent.thirdSecondTransactionDataPlank(endPrice, circulateInfo.getStockCode(), list);
                            if(transactionDataPlank==1||transactionDataPlank==-1){
                                plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                            }
                            String insertTime = getInsertTime(circulateInfo.getStockCode(),endPrice,list);
                            Date insertDate = null;
                            if(!StringUtils.isBlank(insertTime)){
                                insertDate = DateUtil.parseDate(DateUtil.format(preDate, DateUtil.yyyy_MM_dd) + " " + insertTime + ":00", DateUtil.DEFAULT_FORMAT);
                            }
                            plankTypeDTO.setInsertTime(insertDate);
                        }
                        plankTypeDTO.setPlank(highPlank);
                        plankTypeDTO.setEndPlank(endPlank);
                        current++;
                    }else{
                        return plankTypeDTO;
                    }
                }
                if(i>2) {
                    if (endPlank) {
                        if (space == 0) {
                            current++;
                        }
                        if (space == 1) {
                            before++;
                        }
                    } else {
                        space++;
                    }
                    if (space >= 2) {
                        break;
                    }
                }
            }
            preEndPrice = kBarDTO.getEndPrice();
            preHighPrice = kBarDTO.getHighestPrice();
            preLowerPrice = kBarDTO.getLowestPrice();
            preDate = kBarDTO.getDate();
            preOpenPrice = kBarDTO.getStartPrice();
        }
        plankTypeDTO.setPlanks(current);
        plankTypeDTO.setBeforePlanks(before);
        plankTypeDTO.setSpace(space);
        return plankTypeDTO;
    }
}
