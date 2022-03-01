package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.enums.PlankSignEnum;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockPlankDailyComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private StockRehabilitationService stockRehabilitationService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private CirculateInfoService circulateInfoService;


    public void stockPlankDailyStatistic(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        for (CirculateInfo circulateInfo:circulateInfos){
            if(!circulateInfo.getStockCode().equals("603056")){
                continue;
            }
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            try {
                /*if (!stockCode.equals("603787")) {
                    continue;
                }*/
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo.getStockCode());
                log.info("复盘数据 k线数据 stockCode:{} stockName:{} kbars:{}", stockCode, stockName, JSONObject.toJSONString(stockKBars));
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                PlankTypeDTO plankTypeDTO = continuePlankTypeDto(stockKBars, circulateInfo);
                PlankTypeEnum plankTypeEnum = getPlankTypeEnum(plankTypeDTO);
                if (plankTypeEnum == null) {
                    log.info("复盘数据 没有板 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                beforeRate(plankTypeDTO,stockKBars,DateUtil.format(date,DateUtil.yyyy_MM_dd));
                saveStockPlankDaily(stockCode,stockName,date,plankTypeDTO,plankTypeEnum);
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }


    public void handleStopTradeStock(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("603056")){
                continue;
            }*/
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            try {
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo.getStockCode());
                log.info("复盘数据 k线数据 stockCode:{} stockName:{} kbars:{}", stockCode, stockName, JSONObject.toJSONString(stockKBars));
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                KBarDTO kBarDTO = stockKBars.get(stockKBars.size() - 1);
                KBarDTO preKbarDTO = stockKBars.get(stockKBars.size() - 2);
                if(kBarDTO.getDateStr().equals(DateUtil.format(date,DateUtil.yyyy_MM_dd))&&(kBarDTO.getTotalExchange()==null||kBarDTO.getTotalExchange()<1)){
                    StockPlankDailyQuery query = new StockPlankDailyQuery();
                    query.setUniqueKey(stockCode+"_"+DateUtil.format(preKbarDTO.getDate(),DateUtil.yyyyMMdd));
                    List<StockPlankDaily> dailies = stockPlankDailyService.listByCondition(query);
                    if(!CollectionUtils.isEmpty(dailies)){
                        StockPlankDaily stockPlankDaily = new StockPlankDaily();
                        BeanUtils.copyProperties(stockPlankDaily,dailies.get(0));
                        stockPlankDaily.setId(null);
                        stockPlankDaily.setTradeDate(DateTimeUtils.getDate000000(date));
                        stockPlankDaily.setUniqueKey(stockCode+"_"+DateUtil.format(date,DateUtil.yyyyMMdd));
                        stockPlankDaily.setCreateTime(new Date());
                        stockPlankDailyService.save(stockPlankDaily);
                    }
                }
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }

    public void saveStockPlankDaily(String stockCode,String stockName,Date date,PlankTypeDTO plankTypeDTO,PlankTypeEnum plankTypeEnum){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setUniqueKey(stockCode+"_"+DateUtil.format(date,DateUtil.yyyyMMdd));
        List<StockPlankDaily> dailies = stockPlankDailyService.listByCondition(query);
        if(!CollectionUtils.isEmpty(dailies)){
            log.info("stockPlankDaily复盘已经存在stockCode:{}",stockCode);
            return;
        }
        StockPlankDaily daily = new StockPlankDaily();
        daily.setStockCode(stockCode);
        daily.setStockName(stockName);
        daily.setPlankType(plankTypeEnum.getCode());
        if(plankTypeDTO.isEndPlank()) {
            daily.setEndStatus(1);
        }else{
            daily.setEndStatus(0);
        }
        daily.setBeforeRateFive(plankTypeDTO.getBeforeRate5());
        daily.setBeforeRateTen(plankTypeDTO.getBeforeRate10());
        daily.setBeforeRateFifteen(plankTypeDTO.getBeforeRate15());
        daily.setExchangeQuantity(plankTypeDTO.getExchangeQuantity());
        daily.setBadPlankType(0);
        daily.setTradeDate(date);
        if(plankTypeDTO.isContinueFlag()) {
            daily.setContinuePlankType(1);
        }else{
            daily.setContinuePlankType(0);
        }
        daily.setCreateTime(new Date());
        daily.setUniqueKey(stockCode+"_"+DateUtil.format(date,DateUtil.yyyyMMdd));
        stockPlankDailyService.save(daily);
    }


    public void beforeRate(PlankTypeDTO plankTypeDTO, List<KBarDTO> kbars,String tradeDateStr){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        int i = 0;
        boolean flag = false;
        BigDecimal endPrice = null;
        for (KBarDTO kBarDTO:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endPrice = kBarDTO.getEndPrice();
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getEndPrice()), kBarDTO.getEndPrice());
                plankTypeDTO.setBeforeRate5(rate);
            }

            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getEndPrice()), kBarDTO.getEndPrice());
                plankTypeDTO.setBeforeRate10(rate);
            }

            if(i==16){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getEndPrice()), kBarDTO.getEndPrice());
                plankTypeDTO.setBeforeRate15(rate);
            }
            if(i>16){
                return;
            }
            if(kBarDTO.getDateStr().equals(tradeDateStr)){
                flag = true;
                plankTypeDTO.setExchangeQuantity(kBarDTO.getTotalExchange()*100);
            }
        }
    }

    public PlankTypeEnum getPlankTypeEnum(PlankTypeDTO plankTypeDTO){
        if(plankTypeDTO==null || !plankTypeDTO.isPlank()){
            return null;
        }
        if(plankTypeDTO.getSpace()>0&& plankTypeDTO.getBeforePlanks()==1&&plankTypeDTO.getPlanks()==1){
            return PlankTypeEnum.THREE_DAY_TWO_PLANK;
        }
        if(plankTypeDTO.getSpace()>0&& plankTypeDTO.getBeforePlanks()>0){
            return PlankTypeEnum.FOUR_DAY_THREE_PLANK;
        }
        if(plankTypeDTO.getContinuePlanks()==plankTypeDTO.getPlanks()){
            plankTypeDTO.setContinueFlag(true);
        }
        if(plankTypeDTO.getPlanks()>=5){
            return PlankTypeEnum.FIFTH_PLANK;
        }
        PlankTypeEnum plankTypeEnum = PlankTypeEnum.getByCode(plankTypeDTO.getPlanks());
        return plankTypeEnum;
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
        KBarDTO preKbar = null;
        Date preDate = null;
        int space = 0;
        int current = 0;
        int before = 0;
        int continuePlanks = 0;
        boolean continueFlag = true;
        int i=0;
        for (KBarDTO kBarDTO:reverse){
            i++;
            if(preEndPrice!=null) {
                BigDecimal endPrice = kBarDTO.getEndPrice();
                if(i==2||i==3){
                    BigDecimal factor = getFactor(circulateInfo.getStockCode(), preDate);
                    if(factor!=null) {
                        endPrice = endPrice.multiply(factor).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    //添加到除权未板池
                    if(i==2&&factor!=null){
                        boolean highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preHighPrice,endPrice);
                        if(!highPlank){
                            saveStockRehabilitation(circulateInfo.getStockCode(),circulateInfo.getStockName(),preDate);
                        }
                    }
                }
                boolean highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preHighPrice,endPrice);
                boolean endPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preEndPrice,endPrice);
                if(i==2){
                    if(highPlank){
                        plankTypeDTO.setPlank(highPlank);
                        plankTypeDTO.setEndPlank(endPlank);
                        current++;
                    }else{
                        return plankTypeDTO;
                    }
                    if(highPlank && preKbar.getHighestPrice().compareTo(preKbar.getLowestPrice())==0){
                        continuePlanks++;
                    }else{
                        continueFlag  = false;
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
                    if(continueFlag){
                        if(highPlank && preKbar.getHighestPrice().compareTo(preKbar.getLowestPrice())==0){
                            continuePlanks++;
                        }
                    }else{
                        continueFlag = false;
                    }
                    if (space >= 2) {
                        break;
                    }

                }
            }
            preEndPrice = kBarDTO.getEndPrice();
            preHighPrice = kBarDTO.getHighestPrice();
            preKbar  = kBarDTO;
            preDate  = kBarDTO.getDate();
        }
        plankTypeDTO.setPlanks(current);
        plankTypeDTO.setBeforePlanks(before);
        plankTypeDTO.setSpace(space);
        plankTypeDTO.setContinuePlanks(continuePlanks);
        return plankTypeDTO;
    }
    public void saveStockRehabilitation(String stockCode,String stockName,Date tradeDate){
        Date preTradeDate = commonComponent.preTradeDate(tradeDate);
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setStockCode(stockCode);
        query.setTradeDateFrom(DateTimeUtils.getDate000000(preTradeDate));
        query.setTradeDateTo(DateTimeUtils.getDate235959(preTradeDate));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        int plankTypes = 0;
        if(!CollectionUtils.isEmpty(stockPlankDailies)){
            plankTypes = stockPlankDailies.get(0).getPlankType();
        }
        StockRehabilitation stockRehabilitation = new StockRehabilitation();
        stockRehabilitation.setStockCode(stockCode);
        stockRehabilitation.setStockName(stockName);
        stockRehabilitation.setTradeDateStamp(DateUtil.format(tradeDate,DateUtil.yyyy_MM_dd));
        stockRehabilitation.setYesterdayPlankType(plankTypes);
        stockRehabilitationService.save(stockRehabilitation);
    }


    public List<KBarDTO> getStockKBars(String stockCode){
        try {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY,stockCode, 0, 50);
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


    public BigDecimal getFactor(String stockCode,Date tradeDate){
        Date preTradeDate = commonComponent.preTradeDate(tradeDate);
        String fromDate = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
        Map<String, AdjFactorDTO> adjFactorMap = stockKbarComponent.getAdjFactorMap(stockCode, fromDate);
        AdjFactorDTO adjFactorDTO = adjFactorMap.get(DateUtil.format(tradeDate, DateUtil.yyyyMMdd));
        AdjFactorDTO preAdjFactorDTO = adjFactorMap.get(DateUtil.format(preTradeDate, DateUtil.yyyyMMdd));
        if(adjFactorDTO==null||preAdjFactorDTO==null||adjFactorDTO.getAdjFactor()==null||preAdjFactorDTO.getAdjFactor()==null){
            return null;
        }
        if(adjFactorDTO.getAdjFactor().equals(preAdjFactorDTO.getAdjFactor())){
            return null;
        }
        BigDecimal factor = preAdjFactorDTO.getAdjFactor().divide(adjFactorDTO.getAdjFactor(), 4, BigDecimal.ROUND_HALF_UP);
        return factor;
    }


    public void calMax100DaysPriceForTwoPlank(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setStockCode(stockPlankDaily.getStockCode());
                stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                stockKbarQuery.setLimit(100);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
                String dateStr = DateUtil.format(date, DateUtil.yyyyMMdd);
                StockKbar highKbar = null;
                StockKbar currentDayKbar = null;
                for (StockKbar kbar : stockKbars) {
                    if (!kbar.getKbarDate().equals(dateStr)) {
                        if (highKbar == null || kbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice()) == 1) {
                            highKbar = kbar;
                        }
                    } else {
                        currentDayKbar = kbar;
                    }
                }
                if (highKbar != null && currentDayKbar != null) {
                    BigDecimal twoPlankUpperPrice = PriceUtil.calUpperPrice(stockPlankDaily.getStockCode(), currentDayKbar.getClosePrice());
                    BigDecimal max100PriceScale = twoPlankUpperPrice.divide(highKbar.getAdjHighPrice(), 2, BigDecimal.ROUND_HALF_UP);
                    stockPlankDaily.setMax100PriceScale(max100PriceScale);
                    BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockPlankDaily.getStockCode(), DateUtil.parseDate(highKbar.getKbarDate(), DateUtil.yyyyMMdd));
                    if (avgPrice != null) {
                        BigDecimal adjAvgPrice = avgPrice.multiply(highKbar.getAdjHighPrice().divide(highKbar.getHighPrice(), 2, BigDecimal.ROUND_HALF_UP));
                        BigDecimal max100AvgPriceScale = twoPlankUpperPrice.divide(adjAvgPrice, 2, BigDecimal.ROUND_HALF_UP);
                        stockPlankDaily.setMax100AvgPriceScale(max100AvgPriceScale);
                    }
                    stockPlankDailyService.updateById(stockPlankDaily);
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }

    }

    public void calMin15DaysPriceForTwoPlank(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setStockCode(stockPlankDaily.getStockCode());
                stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                stockKbarQuery.setLimit(16);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
                String dateStr = DateUtil.format(date, DateUtil.yyyyMMdd);
                StockKbar lowKbar = null;
                StockKbar currentDayKbar = null;
                for (StockKbar kbar : stockKbars) {
                    if (!kbar.getKbarDate().equals(dateStr)) {
                        if (lowKbar == null || kbar.getAdjLowPrice().compareTo(lowKbar.getAdjLowPrice()) == -1) {
                            lowKbar = kbar;
                        }
                    } else {
                        currentDayKbar = kbar;
                    }
                }
                if (lowKbar != null && currentDayKbar != null) {
                    BigDecimal twoPlankUpperPrice = PriceUtil.calUpperPrice(stockPlankDaily.getStockCode(), currentDayKbar.getClosePrice());
                    BigDecimal min15PriceScale = twoPlankUpperPrice.divide(lowKbar.getAdjLowPrice(), 2, BigDecimal.ROUND_HALF_UP);
                    stockPlankDaily.setMin15PriceScale(min15PriceScale);
                    stockPlankDailyService.updateById(stockPlankDaily);
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }

    }

    public void calSubNewStock(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        String dateStr = DateUtil.format(date, DateUtil.yyyy_MM_dd);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockPlankDaily.getStockCode(), 0, 50);
                List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
                if(CollectionUtils.isEmpty(kbars)){
                    continue;
                }
                if(kbars.size()<50){
                    boolean isContinuePlank = true;
                    BigDecimal preEndPrice = null;
                    KBarDTO firstOpenKbar = null;
                    for (KBarDTO dto:kbars){
                        if(preEndPrice!=null){
                            boolean endPlank = PriceUtil.isUpperPrice(dto.getEndPrice(), preEndPrice);
                            if(isContinuePlank && !endPlank){
                                isContinuePlank = false;
                                firstOpenKbar = dto;
                            }
                            if(dto.getDateStr().equals(dateStr)){
                                if(firstOpenKbar==null){
                                    //执行次新逻辑
                                    stockPlankDaily.setPlankSign(PlankSignEnum.SUB_NEW.getCode());
                                    stockPlankDailyService.updateById(stockPlankDaily);
                                }
                                if(firstOpenKbar!=null&&firstOpenKbar.getDateStr().equals(dto.getDateStr())){
                                    //执行次新逻辑
                                    stockPlankDaily.setPlankSign(PlankSignEnum.SUB_NEW.getCode());
                                    stockPlankDailyService.updateById(stockPlankDaily);
                                }
                            }
                        }
                        preEndPrice = dto.getEndPrice();
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }

    }

    public void insertTime(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        String dateStr = DateUtil.format(date, DateUtil.yyyy_MM_dd);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, stockPlankDaily.getStockCode(), 0, 10);
                List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
                if(CollectionUtils.isEmpty(kbars)){
                    continue;
                }
                BigDecimal preEndPrice = null;
                for (KBarDTO kBarDTO:kbars){
                    if(preEndPrice!=null&&kBarDTO.getDateStr().equals(dateStr)) {
                        List<ThirdSecondTransactionDataDTO> datas = currentDayTransactionDataComponent.getData(stockPlankDaily.getStockCode());
                        String insertTime = currentDayTransactionDataComponent.insertTime(preEndPrice, stockPlankDaily.getStockCode(), datas);
                        Date insertDate = null;
                        if(!StringUtils.isBlank(insertTime)){
                            insertDate = DateUtil.parseDate(DateUtil.format(date, DateUtil.yyyy_MM_dd) + " " + insertTime + ":00", DateUtil.DEFAULT_FORMAT);
                        }
                        stockPlankDaily.setInsertTime(insertDate);
                        stockPlankDailyService.updateById(stockPlankDaily);
                    }
                    preEndPrice = kBarDTO.getEndPrice();
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


    public void realPlanks(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        String dateStr = DateUtil.format(date, DateUtil.yyyy_MM_dd);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                String stockCode = stockPlankDaily.getStockCode();
                List<KBarDTO> stockKBars = getStockKBars(stockPlankDaily.getStockCode());
                log.info("复盘数据 k线数据 stockCode:{} stockName:{} kbars:{}", stockCode, stockPlankDaily.getStockName(), JSONObject.toJSONString(stockKBars));
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockPlankDaily.getStockName());
                    continue;
                }
                CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
                circulateInfoQuery.setStockCode(stockCode);
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
                if(CollectionUtils.isEmpty(circulateInfos)){
                    return;
                }
                CirculateInfo circulateInfo = circulateInfos.get(0);
                PlankTypeDTO plankTypeDTO = continuePlankTypeDto(stockKBars, circulateInfo);
                stockPlankDaily.setRealPlanks(plankTypeDTO.getPlanks());
                stockPlankDailyService.updateById(stockPlankDaily);

            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }
    public void middlePlanks(Date date){
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> stockPlankDailies = stockPlankDailyService.listByCondition(query);
        for (StockPlankDaily stockPlankDaily:stockPlankDailies){
            try {
                String stockCode = stockPlankDaily.getStockCode();
                StockKbarQuery kbarQuery = new StockKbarQuery();
                kbarQuery.setStockCode(stockCode);
                kbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
                List<StockKbar> stockKBars = stockKbarService.listByCondition(kbarQuery);
                if (CollectionUtils.isEmpty(stockKBars) || stockKBars.size()<7) {
                    log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockPlankDaily.getStockName());
                    continue;
                }
                if(!stockKBars.get(0).getKbarDate().equals(DateUtil.format(date,DateUtil.yyyyMMdd))){
                    log.info("复盘数据 没有获取到当日k线数据 stockCode:{} stockName:{}", stockCode, stockPlankDaily.getStockName());
                    continue;
                }

                Integer middlePlanks = calTodayPlank(stockKBars);
                stockPlankDaily.setMiddlePlanks(middlePlanks);
                stockPlankDailyService.updateById(stockPlankDaily);

            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


    public static Integer calTodayPlank(List<StockKbar> stockKbarList) {
        if(stockKbarList.size()<2){
            return 1;
        }
        stockKbarList = Lists.reverse(stockKbarList);
        int planks = 1;
        int unPlanks = 0;
        boolean prePlank = true;
        for (int i = stockKbarList.size() - 2; i > 0; i--) {
            StockKbar stockKbar = stockKbarList.get(i);
            StockKbar preStockKbar = stockKbarList.get(i - 1);
            if (PriceUtil.isUpperPrice(stockKbar.getClosePrice(), preStockKbar.getClosePrice())) {
                planks++;
                prePlank = true;
            } else {
                unPlanks++;
                if(unPlanks>=2){
                    if(prePlank){
                        return planks;
                    }else {
                        return planks;
                    }
                }
                prePlank = false;

            }

        }
        return planks;
    }

    public Integer calMiddlePlanks(List<StockKbar> stockKbars){
        StockKbar  nextKbar = null;
        int planks = 0;
        int unPlanks = 0;
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getTradeQuantity()==null||stockKbar.getTradeQuantity()==0){
                continue;
            }
            if(nextKbar!=null){
                boolean upperPrice = PriceUtil.isUpperPrice(nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if(!upperPrice){
                    upperPrice = PriceUtil.isUpperPrice(nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(upperPrice){
                    planks++;
                }else{
                    unPlanks++;
                }
                if(unPlanks>=2){
                    break;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;
    }


    public void superFactor(Date date){
        Date date000000 = DateTimeUtils.getDate000000(date);
        Date date235959 = DateTimeUtils.getDate235959(date);
        StockPlankDailyQuery query = new StockPlankDailyQuery();
        query.setTradeDateFrom(date000000);
        query.setTradeDateTo(date235959);
        List<StockPlankDaily> dailies = stockPlankDailyService.listByCondition(query);
        for (StockPlankDaily stockPlankDaily:dailies){
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setStockCode(stockPlankDaily.getStockCode());
            stockKbarQuery.setLimit(100);
            stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            if(CollectionUtils.isEmpty(stockKbars)){
                stockPlankDaily.setKbarCounts(0);
                stockPlankDailyService.updateById(stockPlankDaily);
                continue;
            }
            stockPlankDaily.setKbarCounts(stockKbars.size());
            int i = 0;
            BigDecimal plankHighPrice = null;
            BigDecimal lowPrice = null;
            for (StockKbar kbar:stockKbars){
                i++;
                if(i<=15){
                    if(lowPrice==null||kbar.getAdjLowPrice().compareTo(lowPrice)==-1){
                        lowPrice = kbar.getAdjLowPrice();
                    }
                   if(i==1){
                       plankHighPrice = kbar.getAdjHighPrice();
                   }
                }
            }
            if(plankHighPrice!=null&&lowPrice!=null){
                BigDecimal divide = plankHighPrice.divide(lowPrice,2,BigDecimal.ROUND_HALF_UP);
                stockPlankDaily.setDay15HighLow(divide);
            }

            int seriesPlanks = 1;
            StockKbar nextKbar = null;
            int j = 0;
            for (StockKbar kbar:stockKbars){
                j++;
                if(j>=3){
                    boolean endPlank = PriceUtil.isUpperPrice(kbar.getStockCode(), nextKbar.getClosePrice(), kbar.getClosePrice());
                    if(!endPlank){
                        endPlank = PriceUtil.isUpperPrice(kbar.getStockCode(), nextKbar.getAdjClosePrice(),kbar.getAdjClosePrice());
                    }
                    if(endPlank){
                        seriesPlanks++;
                    }else{
                        break;
                    }
                }
                nextKbar = kbar;
            }
            stockPlankDaily.setSeriesPlanks(seriesPlanks);
            stockPlankDailyService.updateById(stockPlankDaily);
        }



    }

}
