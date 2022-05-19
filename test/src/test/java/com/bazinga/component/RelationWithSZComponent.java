package com.bazinga.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.HighPlankBuyDTO;
import com.bazinga.dto.PlankTimeLevelDTO;
import com.bazinga.dto.RelativeWithSzBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class RelationWithSZComponent {
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
    @Autowired
    private RedisMoniorService redisMoniorService;
    public void relativeWithSZInfo(){
        Map<String, List<RelativeWithSzBuyDTO>> map = raiseInfo();
        List<Object[]> datas = Lists.newArrayList();

        for (String tradeDate : map.keySet()) {
            List<RelativeWithSzBuyDTO> relativeWithSzBuyDTOS = map.get(tradeDate);
            for (RelativeWithSzBuyDTO dto:relativeWithSzBuyDTOS) {
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getTradeDate());
                list.add(dto.getCirculate());
                list.add(dto.getCirculateZ());
                list.add(dto.getBuyPrice());
                list.add(dto.getRateDay1());
                list.add(dto.getRateDay2());
                list.add(dto.getRateDay3());
                list.add(dto.getRateDay3Total());
                list.add(dto.getLevel());
                list.add(dto.getLowAddHigh());
                list.add(dto.getBeforeRateDay3());
                list.add(dto.getBeforeRateDay5());
                list.add(dto.getBeforeRateDay10());
                list.add(dto.getBeforeRateDay60());
                list.add(dto.getProfit());
                list.add(dto.getProfitEnd());
                Object[] objects = list.toArray();
                datas.add(objects);
            }
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","总股本","流通z","买入价格","第一天","第二天","第三天","3天总背离值","排名","最高加最低","3日涨幅","5日涨幅","10日涨幅","60日涨幅","板高","收盘盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("背离买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("背离买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void getUpperInfo(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            String stockCode = circulateInfo.getStockCode();
           /* if(!stockCode.equals("002011")){
                continue;
            }*/
            String value = getToShareInfo(stockCode);
            RedisMonior redisKey = redisMoniorService.getByRedisKey(stockCode);
            if(redisKey!=null){
                continue;
            }
            RedisMonior redisMonior = new RedisMonior();
            redisMonior.setRedisKey(circulateInfo.getStockCode());
            redisMonior.setRedisValue(value);
            if(StringUtils.isNotBlank(value)){
                redisMoniorService.save(redisMonior);
            }else{
                redisMonior.setRedisValue("test");
                redisMoniorService.save(redisMonior);
            }
        }
    }
    public String getToShareInfo(String stockCode){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "stk_limit");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        String tsStockCode = stockCode.startsWith("6") ? stockCode + ".SH" : stockCode + ".SZ";
        paramsdate.put("ts_code", tsStockCode);
        paramsdate.put("start_date", "20180101");
        paramsdate.put("end_date", "20220516");
        paramMap.put("params", paramsdate);
        paramMap.put("fields", "trade_date,pre_close,up_limit");
        int times = 1;
        while (times <= 3){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if(CollectionUtils.isEmpty(fields)){
                    return null;
                }
                String value = convertToStr(fields);
                return value;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}", times,stockCode, e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public String convertToStr(JSONArray jsonArray){
        List<String> list = Lists.newArrayList();
        for (int i = 0;i<jsonArray.size();i++){
            JSONArray json = jsonArray.getJSONArray(i);
            String tradeDate = json.get(0).toString();
            if(json.get(1)==null||json.get(2)==null){
                continue;
            }
            BigDecimal preClose = new BigDecimal(json.get(1).toString());
            BigDecimal upPrice = new BigDecimal(json.get(2).toString());
            if(preClose.compareTo(BigDecimal.ZERO)==0){
                continue;
            }
            BigDecimal rate = PriceUtil.getPricePercentRate(upPrice.subtract(preClose), preClose);
            if(rate.compareTo(new BigDecimal(6))==-1){
                list.add(tradeDate);
            }
        }
        String str = "";
        if(!CollectionUtils.isEmpty(list)){
            int size = list.size();
            int i=0;
            for (String tradeDate:list){
                i++;
                if(i==size){
                    str = str+tradeDate;
                }else {
                    str = str + tradeDate + ",";
                }
            }
        }
        return str;
    }

    public Map<String, List<RelativeWithSzBuyDTO>> raiseInfo(){
        Map<String, List<RelativeWithSzBuyDTO>> map = new HashMap<>();
        Map<String, List<StockKbar>> szKbarMap = szRaiseInfo();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        int m = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            m++;
            System.out.println(circulateInfo.getStockCode());
            /*if(!circulateInfo.getStockCode().equals("605319")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> rateQueue = new LimitQueue<>(4);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                rateQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(date.after(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))){
                    continue;
                }
                List<String> olds = Lists.newArrayList();
                RedisMonior redisMonior = redisMoniorService.getByRedisKey(circulateInfo.getStockCode());
                if(redisMonior!=null&&!redisMonior.getRedisValue().equals("test")){
                    String[] split = redisMonior.getRedisValue().split(",");
                    List<String> strings = Arrays.asList(split);
                    olds.addAll(strings);
                }

                if(preKbar!=null) {
                    RelativeWithSzBuyDTO buyDTO = new RelativeWithSzBuyDTO();
                    buyDTO.setStockCode(circulateInfo.getStockCode());
                    buyDTO.setStockName(circulateInfo.getStockName());
                    buyDTO.setCirculate(circulateInfo.getCirculate());
                    buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    buyDTO.setBuyPrice(stockKbar.getClosePrice());
                    buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    boolean havePlank = isHavePlank(rateQueue);
                    if(!havePlank){
                        List<StockKbar> szKbars = szKbarMap.get(stockKbar.getKbarDate());
                        calRelativeRate(szKbars,rateQueue,buyDTO);
                        if(buyDTO.getRateDay3Total()!=null&&buyDTO.getRateDay3Total().compareTo(new BigDecimal("1.5"))==1){
                            List<RelativeWithSzBuyDTO> buyDTOS = map.get(stockKbar.getKbarDate());
                            if(buyDTOS==null){
                                buyDTOS = Lists.newArrayList();
                                map.put(stockKbar.getKbarDate(),buyDTOS);
                            }
                            calBeforeRateDay(stockKbars,buyDTO,stockKbar);
                            calProfit(stockKbars,buyDTO,stockKbar);
                            if(!olds.contains(stockKbar.getKbarDate())) {
                                buyDTOS.add(buyDTO);
                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        getLevelInfo(map);
        return map;
    }

    public boolean isHavePlank(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(stockKbars==null){
            return true;
        }
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(preStockKbar!=null){
                boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                if(historyUpperPrice){
                    return true;
                }
            }
            preStockKbar = stockKbar;
        }
        return false;
    }

    public void getLevelInfo(Map<String,List<RelativeWithSzBuyDTO>> buyDTOMapS){
        for (String tradeDate:buyDTOMapS.keySet()){
            List<RelativeWithSzBuyDTO> buyDTOS = buyDTOMapS.get(tradeDate);
            List<RelativeWithSzBuyDTO> relativeWithSzBuyDTOS = RelativeWithSzBuyDTO.rateDay3TotalSort(buyDTOS);
            int i = 0;
            for (RelativeWithSzBuyDTO buyDTO:relativeWithSzBuyDTOS){
                i++;
                buyDTO.setLevel(i);
            }
        }
    }

    public void calRelativeRate(List<StockKbar> szKbars,LimitQueue<StockKbar> limitQueue,RelativeWithSzBuyDTO buyDTO){
        if(limitQueue.size()<4){
            return;
        }
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        int i = 0;
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(i==1){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                BigDecimal rateSz = PriceUtil.getPricePercentRate(szKbars.get(1).getClosePrice().subtract(szKbars.get(0).getClosePrice()), szKbars.get(0).getAdjClosePrice());
                BigDecimal relativeRate = rate.subtract(rateSz);
                buyDTO.setRateDay1(relativeRate);
            }
            if(i==2){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                BigDecimal rateSz = PriceUtil.getPricePercentRate(szKbars.get(2).getClosePrice().subtract(szKbars.get(1).getClosePrice()), szKbars.get(1).getAdjClosePrice());
                BigDecimal relativeRate = rate.subtract(rateSz);
                buyDTO.setRateDay2(relativeRate);
            }
            if(i==3){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                BigDecimal rateSz = PriceUtil.getPricePercentRate(szKbars.get(3).getClosePrice().subtract(szKbars.get(2).getClosePrice()), szKbars.get(2).getAdjClosePrice());
                BigDecimal relativeRate = rate.subtract(rateSz);
                buyDTO.setRateDay3(relativeRate);
            }
            preStockKbar = stockKbar;
            i++;
        }

        BigDecimal rate = PriceUtil.getPricePercentRate(stockKbars.get(3).getAdjClosePrice().subtract(stockKbars.get(0).getAdjClosePrice()), stockKbars.get(0).getAdjClosePrice());
        BigDecimal rateSz = PriceUtil.getPricePercentRate(szKbars.get(3).getClosePrice().subtract(szKbars.get(0).getClosePrice()), szKbars.get(0).getClosePrice());
        BigDecimal relativeRate = rate.subtract(rateSz);
        buyDTO.setRateDay3Total(relativeRate);
    }

    public Map<String,List<StockKbar>> szRaiseInfo(){
        Map<String, List<StockKbar>> map = new HashMap<>();
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(4);
        for (StockKbar stockKbar:stockKbars){
            limitQueue.offer(stockKbar);
            List<StockKbar> kbars = limitQueueToList(limitQueue);
            if(limitQueue.size()==4){
                map.put(stockKbar.getKbarDate(),kbars);
            }
        }
        return map;
    }

    public List<StockKbar> limitQueueToList(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<4){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        return list;
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

    public boolean firstBuyTime(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(upperPrice && tradeType==1){
               return true;
            }
        }
        return false;
    }


    public void calProfit(List<StockKbar> stockKbars,RelativeWithSzBuyDTO buyDTO,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                //BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjClosePrice()), buyStockKbar.getAdjClosePrice());
                BigDecimal endProfit = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyStockKbar.getAdjClosePrice()), buyStockKbar.getAdjClosePrice());
                buyDTO.setProfit(profit);
                buyDTO.setProfitEnd(endProfit);
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public void calBeforeRateDay(List<StockKbar> stockKbars,RelativeWithSzBuyDTO buyDTO,StockKbar buyStockKbar){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        StockKbar endStockKbar = null;
        boolean flag = false;
        int i=0;
        BigDecimal highPrice = null;
        BigDecimal lowPrice = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endStockKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setBeforeRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setBeforeRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setBeforeRateDay10(rate);
            }
            if(i==61){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setBeforeRateDay60(rate);
            }
            if(i>0&&i<=60){
                if(highPrice==null||stockKbar.getAdjHighPrice().compareTo(highPrice)==1){
                    highPrice = stockKbar.getAdjHighPrice();
                }
                if(lowPrice==null||stockKbar.getAdjLowPrice().compareTo(lowPrice)==-1){
                    lowPrice = stockKbar.getAdjLowPrice();
                }
            }
            if(i>61){
                break;
            }
            if(stockKbar.getKbarDate().equals(buyStockKbar.getKbarDate())){
                flag = true;
            }
        }
        if(highPrice!=null&&lowPrice!=null){
            buyDTO.setLowAddHigh(highPrice.add(lowPrice));
        }
    }
    public BigDecimal getPercentAmount(StockKbar highStockKbar,BigDecimal plankPrice){
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(highStockKbar.getStockCode(), highStockKbar.getKbarDate());
        if(avgPrice!=null) {
            avgPrice = chuQuanAvgPrice(avgPrice, highStockKbar);
            BigDecimal percent = (plankPrice.subtract(avgPrice)).divide(avgPrice, 4, BigDecimal.ROUND_HALF_UP);
            return percent;
        }
        return null;
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
           // stockKbars = stockKbars.subList(20, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<StockKbar> best = commonComponent.deleteNewStockTimes(stockKbars, 2000);
            return best;
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
