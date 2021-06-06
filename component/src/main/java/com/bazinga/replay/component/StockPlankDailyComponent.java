package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.service.CirculateInfoAllService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.StockRehabilitationService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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


    public void stockPlankDailyStatistic(Date date){
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
                /*if (!stockCode.equals("603787")) {
                    continue;
                }*/
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo);
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
                PlankTypeEnum plankTypeEnum = getPlankTypeEnum(plankTypeDTO);
                if (plankTypeEnum == null) {
                    log.info("复盘数据 没有板 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                saveStockPlankDaily(stockCode,stockName,date,plankTypeDTO,plankTypeEnum);
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }

    public void saveStockPlankDaily(String stockCode,String stockName,Date date,PlankTypeDTO plankTypeDTO,PlankTypeEnum plankTypeEnum){
        StockPlankDaily daily = new StockPlankDaily();
        daily.setStockCode(stockCode);
        daily.setStockName(stockName);
        daily.setPlankType(plankTypeEnum.getCode());
        if(plankTypeDTO.isEndPlank()) {
            daily.setEndStatus(1);
        }else{
            daily.setEndStatus(0);
        }
        daily.setTradeDate(date);
        daily.setCreateTime(new Date());
        stockPlankDailyService.save(daily);
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
        Date preDate = null;
        int space = 0;
        int current = 0;
        int before = 0;
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
            preDate  = kBarDTO.getDate();
        }
        plankTypeDTO.setPlanks(current);
        plankTypeDTO.setBeforePlanks(before);
        plankTypeDTO.setSpace(space);
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


    public List<KBarDTO> getStockKBars(CirculateInfo circulateInfo){
        try {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 50);
            List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
            List<KBarDTO> list = deleteNewStockTimes(kbars, 50, circulateInfo.getStockCode());
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


}
