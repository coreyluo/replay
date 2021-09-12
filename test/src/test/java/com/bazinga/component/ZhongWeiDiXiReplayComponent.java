package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ZhongWeiDiXiReplayComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private BlockLevelReplayComponent blockLevelReplayComponent;

    public void middle(){
        List<StockMiddleRaiseBuyDTO> dailys = Lists.newArrayList();
        Map<String, List<StockKbar>> middles = getMiddles();
        List<StockMiddleRaiseBuyDTO> dates = Lists.newArrayList();
        for (String tradeDate:middles.keySet()){
            Date date = DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd);
            if(date.before(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd))){
                continue;
            }
            /*if(!tradeDate.equals("20210629")){
                continue;
            }*/
            List<StockKbar> middleStocks = middles.get(tradeDate);
            MiddlePlankBuyDTO middlePlankBuyDTO = middleRateInfo(tradeDate, middles.get(tradeDate));
            if(middlePlankBuyDTO.getCounts()>0&&!CollectionUtils.isEmpty(middlePlankBuyDTO.getMinuteSumDTOS())){
                List<MinuteSumDTO> minuteSumDTOS = middlePlankBuyDTO.getMinuteSumDTOS();
                MinuteSumDTO open = null;
                MinuteSumDTO high  = null;
                for (MinuteSumDTO minuteSumDTO:minuteSumDTOS){
                    if(minuteSumDTO.getAvgRate()==null){
                        continue;
                    }
                    if(minuteSumDTO.getTradeTime().equals("09:25")){
                        open = minuteSumDTO;
                    }
                    if(high==null||minuteSumDTO.getAvgRate().compareTo(high.getAvgRate())==1){
                        high  = minuteSumDTO;
                    }
                }
                if(open!=null&&high!=null){
                    StockMiddleRaiseBuyDTO raiseBuyDTO = new StockMiddleRaiseBuyDTO();
                    raiseBuyDTO.setTradeDate(middlePlankBuyDTO.getTradeDate());
                    raiseBuyDTO.setCounts(middlePlankBuyDTO.getCounts());
                    BigDecimal raise = high.getAvgRate().subtract(open.getAvgRate());
                    raiseBuyDTO.setRaiseRate(raise);
                    raiseBuyDTO.setOpenAvgRate(open.getAvgRate());
                    raiseBuyDTO.setHighAvgRate(high.getAvgRate());
                    raiseBuyDTO.setHighAvgRateTime(high.getTradeTime());
                    raiseBuyDTO.setChangeRateTotal(high.getTotalRate().subtract(open.getTotalRate()));
                    raiseBuyDTO.setKbars(middleStocks);
                    if(raise.compareTo(new BigDecimal("1"))==1) {
                        dates.add(raiseBuyDTO);
                    }
                }

            }
        }

        for (StockMiddleRaiseBuyDTO buyDTO:dates){
            List<StockMiddleRaiseBuyDTO> stockMiddleRaiseBuyDTOS = middleBuyStocks(buyDTO,buyDTO.getKbars());
            dailys.addAll(stockMiddleRaiseBuyDTOS);
        }
        List<Object[]> datas = Lists.newArrayList();
        for(StockMiddleRaiseBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getPlanks());
            list.add(dto.isEndPlank());
            list.add(dto.getTradeDate());
            list.add(dto.getOpenAvgRate());
            list.add(dto.getHighAvgRate());
            list.add(dto.getHighAvgRateTime());
            list.add(dto.getRaiseRate());
            list.add(dto.getChangeRateTotal());
            list.add(dto.getBuyPrice());
            list.add(dto.getBuyRate());
            list.add(dto.getOpenRate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","板高","尾盘是否封住","交易日期","中位板开盘平均涨幅","中位板最高平均涨幅（5分钟）","中位板最高平均涨幅时间","涨幅差值","涨幅差值总和","买入价格","买入时候涨幅","开盘涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("反向低吸",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("反向低吸");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<StockMiddleRaiseBuyDTO> middleBuyStocks(StockMiddleRaiseBuyDTO dto, List<StockKbar> middleStocks){
        List<StockMiddleRaiseBuyDTO> stocks = Lists.newArrayList();
        Map<String, StockKbar> tradeMap = new HashMap<>();
        Map<String, StockKbar> preTradeMap = new HashMap<>();

        Date tradeDate = DateUtil.parseDate(dto.getTradeDate(),DateUtil.yyyyMMdd);
        Date preTradeDate = commonComponent.preTradeDate(tradeDate);
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate(DateUtil.format(tradeDate,DateUtil.yyyyMMdd));
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        for (StockKbar stockKbar:stockKbars){
            tradeMap.put(stockKbar.getStockCode(),stockKbar);
        }
        StockKbarQuery preQuery = new StockKbarQuery();
        preQuery.setKbarDate(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preQuery);
        for (StockKbar stockKbar:preStockKbars){
            preTradeMap.put(stockKbar.getStockCode(),stockKbar);
        }
        Date buyTimeDate = DateUtil.parseDate(dto.getHighAvgRateTime(), DateUtil.HH_MM);
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            boolean haveSame = false;
            for(StockKbar middleKbar:middleStocks){
                if(circulateInfo.getStockCode().equals(middleKbar.getStockCode())){
                    haveSame = true;
                }
            }
            if(!haveSame){
                continue;
            }
            StockKbar stockKbar = tradeMap.get(circulateInfo.getStockCode());
            StockKbar preStockKbar = preTradeMap.get(circulateInfo.getStockCode());
            if(stockKbar!=null&&preStockKbar!=null){
                boolean upperPrice = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice());
                if(upperPrice){
                    List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), DateTimeUtils.getDate000000(tradeDate));
                    for (ThirdSecondTransactionDataDTO data:datas){
                        Date dtoTimeDate = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
                        boolean isUpperPrice = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), data.getTradePrice(), preStockKbar.getClosePrice());
                        boolean flag = false;
                        if(isUpperPrice&&data.getTradeType()==1){
                            flag = true;
                        }
                        if(dtoTimeDate.after(buyTimeDate)){
                            if(!flag){
                                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                                BigDecimal buyRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                                StockMiddleRaiseBuyDTO stock = new StockMiddleRaiseBuyDTO();
                                stock.setTradeDate(dto.getTradeDate());
                                stock.setCounts(dto.getCounts());
                                stock.setHighAvgRate(dto.getHighAvgRate());
                                stock.setHighAvgRateTime(dto.getHighAvgRateTime());
                                stock.setOpenAvgRate(dto.getOpenAvgRate());
                                stock.setChangeRateTotal(dto.getChangeRateTotal());
                                stock.setRaiseRate(dto.getRaiseRate());
                                stock.setStockCode(circulateInfo.getStockCode());
                                stock.setStockName(circulateInfo.getStockName());
                                stock.setStockKbar(stockKbar);
                                stock.setOpenRate(openRate);
                                stock.setBuyRate(buyRate);
                                stock.setBuyPrice(data.getTradePrice());
                                avgPrice(stock);
                                getPlanks(stock.getStockCode(), dto.getKbars().get(0).getKbarDate(),stock);
                                stocks.add(stock);
                            }
                            break;
                        }
                    }
                }

            }
        }
        return stocks;
    }


    public void avgPrice(StockMiddleRaiseBuyDTO middleDto){
        Date afterDate = commonComponent.afterTradeDate(DateUtil.parseDate(middleDto.getTradeDate(), DateUtil.yyyyMMdd));
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(middleDto.getStockCode(), DateTimeUtils.getDate000000(afterDate));
        if(avgPrice!=null){

            BigDecimal rate = PriceUtil.getPricePercentRate(avgPrice.subtract(middleDto.getBuyPrice()), middleDto.getBuyPrice());
            middleDto.setProfit(rate);
        }
    }

    /**
     *
     * @param kbarDate 20210903
     * @param list
     */
    public MiddlePlankBuyDTO middleRateInfo(String kbarDate, List<StockKbar> list){
        MiddlePlankBuyDTO middlePlankBuyDTO = new MiddlePlankBuyDTO();
        Map<String, ThirdSecondTransactionDataDTO> map = new HashMap<>();
        if(CollectionUtils.isEmpty(list)){
            return middlePlankBuyDTO;
        }
        List<MinuteSumDTO>  minuteSumDTOS= Lists.newArrayList();
        middlePlankBuyDTO.setCounts(list.size());
        Date date = commonComponent.afterTradeDate(DateUtil.parseDate(kbarDate, DateUtil.yyyyMMdd));
        Date date0000 = DateTimeUtils.getDate000000(date);
        middlePlankBuyDTO.setTradeDate(DateUtil.format(date,DateUtil.yyyyMMdd));
        for (StockKbar stockKbar:list){
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), date0000);
            for (ThirdSecondTransactionDataDTO data:datas){
                String key = stockKbar.getStockCode() + data.getTradeTime();
                map.put(key,data);
            }
        }
        Date tradeTime = DateUtil.parseDate(DateUtil.format(date0000, DateUtil.yyyy_MM_dd) + " 09:24:00", DateUtil.DEFAULT_FORMAT);
        for (int i=0;i<10;i++){
            tradeTime = DateUtil.addMinutes(tradeTime, 1);
            if(i>=1&&i<=4){
                continue;
            }
            int count = 0;
            BigDecimal totalRate = BigDecimal.ZERO;
            for (StockKbar stockKbar:list){
                String key = stockKbar.getStockCode() + DateUtil.format(tradeTime,DateUtil.HH_MM);
                ThirdSecondTransactionDataDTO dto = map.get(key);
                if(dto!=null){
                    count++;
                    BigDecimal rate = PriceUtil.getPricePercentRate(dto.getTradePrice().subtract(stockKbar.getClosePrice()), stockKbar.getClosePrice());
                    totalRate = totalRate.add(rate);
                }
            }
            MinuteSumDTO minuteSumDTO = new MinuteSumDTO();
            minuteSumDTO.setTradeTime(DateUtil.format(tradeTime,DateUtil.HH_MM));
            if(count!=0){
                BigDecimal avgRate = totalRate.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                minuteSumDTO.setCount(count);
                minuteSumDTO.setTotalRate(totalRate);
                minuteSumDTO.setAvgRate(avgRate);
            }
            minuteSumDTOS.add(minuteSumDTO);
        }
        middlePlankBuyDTO.setMinuteSumDTOS(minuteSumDTOS);
        return middlePlankBuyDTO;
    }

    public Map<String, List<StockKbar>> getMiddles(){
        Map<String, List<StockKbar>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode(),100);
            if(CollectionUtils.isEmpty(stockKBars)){
                continue;
            }
            int planks = 0;
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar stockKbar:stockKBars){
                if(preEndPrice!=null){
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preEndPrice);
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preEndPrice);
                    if(highPlank){
                        i = planks+1;
                    }
                   if(endPlank){
                       planks++;
                   }else{
                       planks = 0;
                   }
                   if(i>=2&&i<=4){
                       List<StockKbar> middles = map.get(stockKbar.getKbarDate());
                       if(middles == null){
                           middles = Lists.newArrayList();
                           map.put(stockKbar.getKbarDate(),middles);
                       }
                       middles.add(stockKbar);
                   }
                }
                i=0;
                preEndPrice = stockKbar.getClosePrice();
            }
        }
        return map;
    }

    public int getPlanks(String stockCode,String tradeDate,StockMiddleRaiseBuyDTO stock){
        List<StockKbar> stockKBars = getStockKBars(stockCode,100);
        if(CollectionUtils.isEmpty(stockKBars)){
            return 0;
        }
        int planks = 0;
        BigDecimal preEndPrice = null;
        for (StockKbar stockKbar:stockKBars){
            if(preEndPrice!=null){
                boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preEndPrice);
                boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preEndPrice);
                if(stockKbar.getKbarDate().equals(tradeDate)){
                    if(highPlank){
                        planks++;
                    }
                    stock.setPlanks(planks);
                    stock.setEndPlank(endPlank);
                    return planks;
                }
                if(endPlank){
                    planks++;
                }else{
                    planks = 0;
                }
            }
            preEndPrice = stockKbar.getClosePrice();
        }
        return 0;
    }


    public List<StockKbar> getStockKBars(String stockCode,int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            List<StockKbar> list = deleteNewStockTimes(reverse, size);
            return list;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<StockKbar> deleteNewStockTimes(List<StockKbar> list,int size){
        List<StockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        StockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }



}
