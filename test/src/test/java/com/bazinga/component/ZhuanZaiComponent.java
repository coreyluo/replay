package com.bazinga.component;

import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ZhuanZaiComponent {
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
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private DropFactorService dropFactorService;

    public static Map<String,BlockLevelDTO> levelMap = new ConcurrentHashMap<>(8192);

    public void zhuanZaiBuy(List<ZhuanZaiExcelDTO> zhuanZais){
        List<Object[]> datas = Lists.newArrayList();
        List<ZhuanZaiBuyDTO> alls = new ArrayList<>();
        Map<String, List<String>> plankMap = new HashMap<>();
        Map<String,List<LevelDTO>> levelMap = new HashMap<>();
        for (ZhuanZaiExcelDTO zhuanZai:zhuanZais){
            System.out.println(zhuanZai.getStockCode());
           /* if(!zhuanZai.getStockCode().equals("123084")){
                continue;
            }*/
            List<StockKbar> kbars = getKbars(zhuanZai.getStockCode(), zhuanZai.getStockName());
            List<ZhuanZaiBuyDTO> zhuanZaiBuyDTOS = buyReason(kbars,plankMap,zhuanZai,levelMap);
            alls.addAll(zhuanZaiBuyDTOS);
            /*if(alls.size()>=30){
                break;
            }*/
        }
        for (String key:levelMap.keySet()){
            List<LevelDTO> levelDTOS = levelMap.get(key);
            Collections.sort(levelDTOS);
        }
        Map<String, Map<String, Integer>> levelResultMap = leveInfo(levelMap);
        for (ZhuanZaiBuyDTO zhuanZaiBuyDTO:alls){
            Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(zhuanZaiBuyDTO.getTradeDate(), DateUtil.yyyyMMdd));
            List<String> stocks = plankMap.get(DateUtil.format(preTradeDate, DateUtil.yyyyMMdd));
            if(!CollectionUtils.isEmpty(stocks)){
                zhuanZaiBuyDTO.setHavePlank(stocks.size());
            }else{
                zhuanZaiBuyDTO.setHavePlank(0);
            }
            Map<String, Integer> levelInfoMap = levelResultMap.get(zhuanZaiBuyDTO.getTradeDate());
            if(levelInfoMap!=null&&levelInfoMap.get(zhuanZaiBuyDTO.getStockCode())!=null){
                zhuanZaiBuyDTO.setLevel(levelInfoMap.get(zhuanZaiBuyDTO.getStockCode()));
            }
        }
        for(ZhuanZaiBuyDTO dto:alls){
            if(DateUtil.parseDate(dto.getTradeDate(),DateUtil.yyyyMMdd).before(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd))){
                continue;
            }
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getMarketAmount());
            list.add(dto.getTradeDate());
            list.add(dto.getBuyPrice());
            list.add(dto.getRelativeOpenRate());
            list.add(dto.getBuyTime());
            list.add(dto.getHavePlank());
            list.add(dto.getLevel());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","市值","交易日期","买入价格","买入时候相对开盘涨幅","买入时间","前一天几只票停牌过","排名","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("转债",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("转债");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<ZhuanZaiBuyDTO> buyReason(List<StockKbar> stockKbars,Map<String, List<String>> plankMap,ZhuanZaiExcelDTO zhuanZai,Map<String,List<LevelDTO>> levelMap){
        List<ZhuanZaiBuyDTO> buys = new ArrayList<>();
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=1){
            return buys;
        }
        BigDecimal preEndPrice = null;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<StockKbar>(2);
        for (StockKbar stockKbar:stockKbars){
            /*if(stockKbar.getKbarDate().equals("20211028")){
                System.out.println("1111111111");
            }*/
            if(preEndPrice!=null) {
                BigDecimal upperPrice = calUpperPrice(preEndPrice);
                if(stockKbar.getHighPrice().compareTo(upperPrice)>=0){
                    List<String> stocks = plankMap.get(stockKbar.getKbarDate());
                    if(stocks==null){
                        stocks = new ArrayList<>();
                        plankMap.put(stockKbar.getKbarDate(),stocks);
                    }
                    stocks.add(stockKbar.getStockCode());
                }
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(!CollectionUtils.isEmpty(datas)){
                    List<ThirdSecondTransactionDataDTO> avgPriceCalDto = new ArrayList<>();
                    List<ThirdSecondTransactionDataDTO> afterBuys = new ArrayList<>();
                    ThirdSecondTransactionDataDTO preDto = null;
                    boolean buyFlag = false;
                    boolean firstTime = true;
                    ZhuanZaiBuyDTO zhuanZaiBuyDTO = new ZhuanZaiBuyDTO();
                    LevelDTO levelDTO = new LevelDTO();
                    levelDTO.setKey(stockKbar.getStockCode());
                    Map<String, BigDecimal> sellMap = new HashMap<>();
                    boolean planked = false;
                    for (ThirdSecondTransactionDataDTO data:datas){
                        avgPriceCalDto.add(data);
                        if(data.getTradePrice().compareTo(upperPrice)>=0){
                            planked = true;
                        }
                        BigDecimal avgPrice = calAvgPrice(avgPriceCalDto);
                        if(buyFlag){
                            afterBuys.add(data);
                            BigDecimal raiseRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(zhuanZaiBuyDTO.getBuyPrice()), preEndPrice);
                            raiseSell(raiseRate,sellMap,data.getTradePrice());
                            BigDecimal relativeAvgPriceRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(avgPrice), preEndPrice);
                            dropSell(relativeAvgPriceRate,sellMap,data.getTradePrice());
                        }
                        Date time = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
                        Date time1000 = DateUtil.parseDate("10:00", DateUtil.HH_MM);
                        if(!time.before(time1000)&& !buyFlag && firstTime){
                            firstTime = false;
                            if(preDto!=null) {
                                BigDecimal currentRate = PriceUtil.getPricePercentRate(preDto.getTradePrice().subtract(preEndPrice), preEndPrice);
                                levelDTO.setRate(currentRate);
                                List<LevelDTO> levelDTOS = levelMap.get(stockKbar.getKbarDate());
                                if (levelDTOS == null) {
                                    levelDTOS = Lists.newArrayList();
                                    levelMap.put(stockKbar.getKbarDate(), levelDTOS);
                                }
                                levelDTOS.add(levelDTO);
                                if (preDto.getTradePrice().compareTo(stockKbar.getOpenPrice()) == 1 && preDto.getTradePrice().compareTo(avgPrice) == 1) {
                                    buyFlag = true;
                                    zhuanZaiBuyDTO.setStockCode(stockKbar.getStockCode());
                                    zhuanZaiBuyDTO.setStockName(stockKbar.getStockName());
                                    zhuanZaiBuyDTO.setBuyTime(preDto.getTradeTime());
                                    zhuanZaiBuyDTO.setTradeDate(stockKbar.getKbarDate());
                                    zhuanZaiBuyDTO.setBuyPrice(preDto.getTradePrice());
                                    zhuanZaiBuyDTO.setMarketAmount(zhuanZai.getMarketAmount());
                                    BigDecimal rate = PriceUtil.getPricePercentRate(preDto.getTradePrice().subtract(stockKbar.getOpenPrice()), preEndPrice);
                                    zhuanZaiBuyDTO.setRelativeOpenRate(rate);
                                    boolean havaPlank = havaPlank(limitQueue);
                                    if(!havaPlank && !planked) {
                                        buys.add(zhuanZaiBuyDTO);
                                    }
                                }
                            }
                        }
                        if(firstTime) {
                            preDto = data;
                        }
                    }
                    if(buyFlag){
                        endSell(sellMap,stockKbar.getClosePrice());
                        calProfit(sellMap,zhuanZaiBuyDTO,preEndPrice);
                    }

                }
            }
            limitQueue.offer(stockKbar);
            preEndPrice = stockKbar.getClosePrice();
        }
        return buys;
    }
    public boolean havaPlank(LimitQueue<StockKbar> stockKbars){
        if(CollectionUtils.isEmpty(stockKbars)){
            return false;
        }
        Iterator<StockKbar> iterator = stockKbars.iterator();
        BigDecimal preEndPrice = null;
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            if(preEndPrice!=null){
                BigDecimal upperPrice = calUpperPrice(preEndPrice);
                if(next.getHighPrice().compareTo(upperPrice)>=0){
                    return true;
                }
            }
            preEndPrice = next.getClosePrice();
        }
        return false;
    }

    public Map<String,Map<String,Integer>> leveInfo(Map<String,List<LevelDTO>> levelMap){
        Map<String,Map<String,Integer>> resultMap  = new HashMap<>();
        for (String key:levelMap.keySet()){
            Map<String, Integer> map = new HashMap<>();
            List<LevelDTO> levelDTOS = levelMap.get(key);
            if(!CollectionUtils.isEmpty(levelDTOS)){
                int i = 0;
                for (LevelDTO levelDTO:levelDTOS){
                    i++;
                    map.put(levelDTO.getKey(),Integer.valueOf(i));
                }
            }
            resultMap.put(key,map);
        }
        return resultMap;
    }

    public void raiseSell(BigDecimal raiseRate,Map<String,BigDecimal> sellMap,BigDecimal price){
        int maxIndex = raiseRate.divide(new BigDecimal("0.5"), 0, BigDecimal.ROUND_DOWN).intValue();
        if(maxIndex<1){
            return;
        }
        for (int i=1 ; i<=maxIndex;i++){
            if(i>20){
                return;
            }
            String key = String.valueOf(i);
            BigDecimal sellPrice = sellMap.get(key);
            if(sellPrice==null){
                sellMap.put(key,price);
            }
        }
    }

    public void dropSell(BigDecimal relativeAvgPriceRate,Map<String,BigDecimal> sellMap,BigDecimal price){
       if(relativeAvgPriceRate.compareTo(new BigDecimal("-1"))==-1){
           for (int i=1 ; i<=20;i++){
               String key = String.valueOf(i);
               BigDecimal sellPrice = sellMap.get(key);
               if(sellPrice==null){
                   sellMap.put(key,price);
               }
           }
       }

    }

    public void endSell(Map<String,BigDecimal> sellMap,BigDecimal price){
        for (int i=1 ; i<=20;i++){
            String key = String.valueOf(i);
            BigDecimal sellPrice = sellMap.get(key);
            if(sellPrice==null){
                sellMap.put(key,price);
            }
        }
    }

    public void calProfit(Map<String,BigDecimal> sellMap,ZhuanZaiBuyDTO buyDTO,BigDecimal preEndPrice){
        BigDecimal profit = null;
        for (int i=1 ; i<=20;i++){
            String key = String.valueOf(i);
            BigDecimal sellPrice = sellMap.get(key);
            BigDecimal rate = PriceUtil.getPricePercentRate(sellPrice.subtract(buyDTO.getBuyPrice()), preEndPrice);
            if(profit==null){
                profit = rate;
            }else{
                profit = profit.add(rate);
            }
        }
        if(profit!=null){
            BigDecimal divide = profit.divide(new BigDecimal(20), 2, BigDecimal.ROUND_HALF_UP);
            buyDTO.setProfit(divide);
        }

    }

    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(0.1);
        BigDecimal divide = a.divide(new BigDecimal(0.5), 0, BigDecimal.ROUND_DOWN);
        System.out.println(divide);

    }


    public BigDecimal calAvgPrice(List<ThirdSecondTransactionDataDTO> dtos){
        if(CollectionUtils.isEmpty(dtos)){
            return null;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        for (ThirdSecondTransactionDataDTO dto:dtos){
            Integer tradeQuantity = dto.getTradeQuantity();
            BigDecimal tradeAmount = dto.getTradePrice().multiply(new BigDecimal(tradeQuantity));
            totalCount = totalCount+tradeQuantity;
            totalAmount  = totalAmount.add(tradeAmount);
        }
        if(totalCount!=0){
            BigDecimal divide = totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            return divide;
        }
        return null;
    }

    public static BigDecimal calUpperPrice(BigDecimal yesterdayPrice) {
        return yesterdayPrice.multiply(CommonConstant.UPPER_RATE300).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public List<StockKbar> getKbars(String stockCode,String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 100);
        List<StockKbar> stockKbars = StockKbarConvert.convert(securityBars,stockCode,stockName);
        return stockKbars;
    }



}
