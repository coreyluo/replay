package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.PlankExchangeAmountDTO;
import com.bazinga.dto.PlankTimeLevelDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PUBLIC_MEMBER;
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
public class PlankTimeLevelComponent {
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
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    public void plankExchangeAmountInfo(){
        Map<String, List<PlankTimeLevelDTO>> map = judgePlankInfo();
        Map<String, List<PlankTimeLevelDTO>> levelMap = getLevel(map);
        List<Object[]> datas = Lists.newArrayList();
        for (String tradeDate:levelMap.keySet()) {
            List<PlankTimeLevelDTO> buys = levelMap.get(tradeDate);
            int i = 0;
            for (PlankTimeLevelDTO dto : buys) {
                i++;
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getTradeDate());
                list.add(dto.getBuyTime());
                list.add(dto.getAllLevel());
                list.add(dto.getMorningLevel());
                list.add(dto.getAfternoonLevel());
                list.add(dto.getPlankLevel());
                list.add(dto.getEndFlag());
                list.add(dto.getRateDay3());
                list.add(dto.getRateDay5());
                list.add(dto.getRateDay10());
                list.add(dto.getPlanks());
                list.add(dto.getProfit());
                Object[] objects = list.toArray();
                datas.add(objects);
            }
        }
        String[] rowNames = {"index","股票代码","股票名称","交易日期","买入时间","排名","上午排名","下午排名","板高排名","尾盘是否封住（1封住 0未封住）","3日涨幅","5日涨幅","10日涨幅","板高","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("买入时间排序",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("买入时间排序");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public Map<String, List<PlankTimeLevelDTO>> judgePlankInfo(){
        Map<String, List<PlankTimeLevelDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        int m = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            m++;
            /*if(m>1000){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            /*if(circulateInfo.getStockCode().equals("600383")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(20);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                if(preKbar!=null) {
                    PlankTimeLevelDTO buyDTO = new PlankTimeLevelDTO();
                    buyDTO.setStockCode(circulateInfo.getStockCode());
                    buyDTO.setStockName(circulateInfo.getStockName());
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    boolean endPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice());
                    if(endPlank) {
                        buyDTO.setEndFlag(1);
                    }else{
                        buyDTO.setEndFlag(0);
                    }
                    if(highPlank) {
                        String buyTime = firstBuyTime(stockKbar, preKbar.getClosePrice(), buyDTO);
                        if (!StringUtils.isBlank(buyTime)) {
                            Integer planks = calPlanks(limitQueue);
                            buyDTO.setPlanks(planks);
                            calProfit(stockKbars, buyDTO, stockKbar);
                            calBeforeRateDay(stockKbars, buyDTO, stockKbar);
                            List<PlankTimeLevelDTO> list = map.get(stockKbar.getKbarDate());
                            if (list == null) {
                                list = new ArrayList<>();
                                map.put(stockKbar.getKbarDate(), list);
                            }
                            list.add(buyDTO);
                        }
                    }

                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public Map<String,List<PlankTimeLevelDTO>> getLevel(Map<String,List<PlankTimeLevelDTO>> map){
        for (String tradeDate:map.keySet()){
            List<PlankTimeLevelDTO> plankTimeLevelDTOS = map.get(tradeDate);
            List<PlankTimeLevelDTO> levels = PlankTimeLevelDTO.timeIntSort(plankTimeLevelDTOS);
            int i=0;
            int j=0;
            int k=0;
            Map<String, List<PlankTimeLevelDTO>> levelMap = new HashMap<>();
            for (PlankTimeLevelDTO levelDTO:levels){
                i++;
                levelDTO.setAllLevel(i);
                if(levelDTO.getTimeInt()>120000) {
                    k++;
                    levelDTO.setAfternoonLevel(k);
                }
                if(levelDTO.getTimeInt()<120000) {
                    j++;
                    levelDTO.setMorningLevel(j);
                }
                List<PlankTimeLevelDTO> plankLevels = levelMap.get(String.valueOf(levelDTO.getPlanks()));
                if (plankLevels==null){
                    plankLevels = new ArrayList<>();
                    levelMap.put(String.valueOf(levelDTO.getPlanks()),plankLevels);
                }
                plankLevels.add(levelDTO);
            }
            for (String planksStr:levelMap.keySet()){
                List<PlankTimeLevelDTO> plankLevels = levelMap.get(planksStr);
                int h=0;
                for (PlankTimeLevelDTO plankTimeLevelDTO:plankLevels){
                    h++;
                    plankTimeLevelDTO.setPlankLevel(h);
                }
            }
            map.put(tradeDate,levels);
        }
        return map;
    }

    public String firstBuyTime(StockKbar stockKbar,BigDecimal preEndPrice,PlankTimeLevelDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean isPlank = true;
        int times = 0;
        String tradeTimeStamp = "09:25";
        for (ThirdSecondTransactionDataDTO data:datas){
            if(tradeTimeStamp.equals(data.getTradeTime())){
                times++;
            }else{
                times = 0;
                tradeTimeStamp = data.getTradeTime();
            }
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            if(data.getTradeTime().equals("09:25")){
                if(!upperPrice){
                    isPlank = false;
                }
                continue;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
                if(!isPlank){
                    String tradeTime = data.getTradeTime();
                    int timeInt = tradeTimeInt(tradeTime, times);
                    buyDTO.setTimeInt(timeInt);
                    buyDTO.setBuyTime(tradeTime);
                    return data.getTradeTime();
                }
                isPlank = true;
            }else{
                isPlank = false;
            }
        }
        return null;
    }
    public static int tradeTimeInt(String tradeTime,int times){
        String tradeTimeStr = tradeTime.replace(":", "");
        if(tradeTimeStr.startsWith("0")){
            tradeTimeStr = tradeTimeStr.substring(1);
        }
        int second = times * 3;
        if(second>=10){
            tradeTimeStr = tradeTimeStr + (times * 3);
        }else{
            tradeTimeStr = tradeTimeStr + "0"+(times * 3);
        }
        Integer timeInt = Integer.valueOf(tradeTimeStr);
        return timeInt;
    }




    public Integer calPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<20){
            return 0;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int planks  = 1;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i<=1){
                continue;
            }
            if(nextKbar!=null) {
                boolean upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!upperPrice) {
                    upperPrice = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if(!upperPrice){
                    return planks;
                }else{
                    planks++;
                }
            }
            nextKbar = stockKbar;
        }
        return planks;

    }

    public void calProfit(List<StockKbar> stockKbars,PlankTimeLevelDTO buyDTO,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjHighPrice()), buyStockKbar.getAdjHighPrice());
                buyDTO.setProfit(profit);
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public void calBeforeRateDay(List<StockKbar> stockKbars,PlankTimeLevelDTO buyDTO,StockKbar buyStockKbar){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        StockKbar endStockKbar = null;
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endStockKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(i>12){
                return;
            }
            if(stockKbar.getKbarDate().equals(buyStockKbar.getKbarDate())){
                flag = true;
            }
        }
    }



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            stockKbars = stockKbars.subList(20, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }


    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,StockKbar kbar){
        BigDecimal reason = null;
        if(!(kbar.getClosePrice().equals(kbar.getAdjClosePrice()))&&!(kbar.getOpenPrice().equals(kbar.getAdjOpenPrice()))){
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }
    }

    public static void main(String[] args) {
       /* List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11);
        List<Integer> integers = list.subList(5, list.size());
        System.out.println(integers);*/

       /* int tradeTimeInt = tradeTimeInt("10:35", 10);
        System.out.println(tradeTimeInt);*/
        List<PlankTimeLevelDTO> list = new ArrayList<>();
        PlankTimeLevelDTO dto1 = new PlankTimeLevelDTO();
        dto1.setTimeInt(95018);
        PlankTimeLevelDTO dto2 = new PlankTimeLevelDTO();
        dto2.setTimeInt(93218);
        PlankTimeLevelDTO dto3 = new PlankTimeLevelDTO();
        dto3.setTimeInt(105018);
        PlankTimeLevelDTO dto4 = new PlankTimeLevelDTO();
        dto4.setTimeInt(94218);
        list.add(dto1);
        list.add(dto2);
        list.add(dto3);
        list.add(dto4);
        List<PlankTimeLevelDTO> levels = PlankTimeLevelDTO.timeIntSort(list);
        System.out.println(levels);
    }


}
