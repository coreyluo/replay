package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.BoxStockBuyDTO;
import com.bazinga.dto.YiZhiHangYeBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class YiZhiHangYeComponent {
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
    private StockAverageLineService stockAverageLineService;
    @Autowired
    private BlockInfoService blockInfoService;
    @Autowired
    private BlockStockDetailService blockStockDetailService;

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);
    private static final ExecutorService BOX_ONE_STOCK_POOL = ThreadPoolUtils.create(8, 16, 512, "quoteCancelOrderPool");


    public void oneStockBox(){
        /*List<MonthDTO> months = Lists.newArrayList();
        MonthDTO monthDTO1 = new MonthDTO();
        monthDTO1.setStartMonth("20210101");
        monthDTO1.setStartMonth("20210301");
        MonthDTO monthDTO2 = new MonthDTO();
        monthDTO2.setStartMonth("20210301");
        monthDTO2.setStartMonth("20210601");
        MonthDTO monthDTO3 = new MonthDTO();
        monthDTO3.setStartMonth("20210601");
        monthDTO3.setStartMonth("20210901");
        MonthDTO monthDTO4 = new MonthDTO();
        monthDTO4.setStartMonth("20210901");
        monthDTO4.setStartMonth("20220101");
        months.add(monthDTO1);
        months.add(monthDTO2);
        months.add(monthDTO3);
        months.add(monthDTO4);*/


        List<BoxStockBuyDTO> results = getStockUpperShowInfo();
        List<BoxStockBuyDTO> dailys = Lists.newArrayList();
        dailys.addAll(results);
        List<BoxStockBuyDTO> sorts = Lists.newArrayList();
        List<Object[]> datas = Lists.newArrayList();
            for (BoxStockBuyDTO dto : sorts) {
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getCirculateZ());
                list.add(dto.getTotalCirculateZ());
                list.add(dto.getTradeDate());
                list.add(dto.getBuyTime());
                list.add(dto.getBuyPrice());
                list.add(dto.getOpenRate());
                list.add(dto.getBuyTimeTradeAmount());
                list.add(dto.getFirstHighRate());
                list.add(dto.getFirstHighTime());
                list.add(dto.getPreTradeAmount());
                list.add(dto.getPrePlanks());
                list.add(dto.getPreEndPlanks());
                list.add(dto.getBeforeHighLowRate());
                list.add(dto.getAfterHighLowRate());
                list.add(dto.getRateDay3());
                list.add(dto.getRateDay5());
                list.add(dto.getRateDay10());
                list.add(dto.getRateDay30());
                list.add(dto.getBetweenTime());
                list.add(dto.getBuyTimeRaiseRate());
                list.add(dto.getFirstHighRaiseRate());
                list.add(dto.getAvgAmountDay5());
                list.add(dto.getLevelBuyTime());
                list.add(dto.getProfit());

                Object[] objects = list.toArray();
                datas.add(objects);
            }

            String[] rowNames = {"index", "stockCode", "stockName", "流通z", "总股本", "tradeDate", "买入时间", "买入价格", "开盘涨幅", "买入时候成交额", "第一次高点涨幅", "第一次高点时间", "前一日成交额", "前一日几连板", "前一日封住数量", "买入前低点涨幅", "中间低点幅度", "3日涨幅", "5日涨幅", "10日涨幅", "30日涨幅", "买入相对第一次高点时间","买入时涨速","第一次高点时候涨速","5日平均成交额", "买入时间排名", "盈利"};
            PoiExcelUtil poiExcelUtil = new PoiExcelUtil("个股箱体", rowNames, datas);
            try {
                poiExcelUtil.exportExcelUseExcelTitle("个股箱体");
            } catch (Exception e) {
                log.info(e.getMessage());
            }
    }



    public List<BoxStockBuyDTO> getStockUpperShowInfo()  {
        Map<String, BlockInfo> blocks = getAllHangYe();
        Map<String, List<BlockStockDetail>> details = getAllHangYeDetails();
        List<BoxStockBuyDTO> results = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String,List<YiZhiHangYeBuyDTO>> yiZhiStockMap = new HashMap<>();
        int count = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            System.out.println(circulateInfo.getStockCode() + count);
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            //query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar : stockKbars) {
                if (DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd).before(DateUtil.parseDate("20190701", DateUtil.yyyyMMdd))) {
                    continue;
                }
                if (DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd).after(DateUtil.parseDate("20191001", DateUtil.yyyyMMdd))) {
                    continue;
                }
                if (preStockKbar != null) {
                    boolean openPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getOpenPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    boolean highPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    boolean yiZhi = false;
                    if(openPlank){
                        yiZhi = isYiZhi(stockKbar, preStockKbar);
                    }
                    if(highPlank) {
                        String buyTime = getBuyTime(stockKbar, preStockKbar);
                        YiZhiHangYeBuyDTO buyDTO = new YiZhiHangYeBuyDTO();
                        buyDTO.setStockCode(circulateInfo.getStockCode());
                        buyDTO.setStockName(circulateInfo.getStockName());
                        buyDTO.setStockKbar(stockKbar);
                        buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setYiZhiFlag(yiZhi);
                        buyDTO.setBuyTime(buyTime);
                        getAllDayProfit(stockKbars, stockKbar, buyDTO);
                    }

                }
                preStockKbar = stockKbar;
            }

        }
        return results;
    }
    public boolean isYiZhi(StockKbar stockKbar,StockKbar preStockKbar){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        int i= 0 ;
        for (ThirdSecondTransactionDataDTO data:datas){
            i++;
            if(i==2){
                boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), data.getTradePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                if(upperPrice&&data.getTradeType()==1){
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public String getBuyTime(StockKbar stockKbar,StockKbar preStockKbar){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean plankFlag = true;
        int i= 0 ;
        for (ThirdSecondTransactionDataDTO data:datas){
            i++;
            if(data.getTradeTime().startsWith("09:33")||data.getTradeTime().startsWith("09:34")||data.getTradeTime().startsWith("09:35")||data.getTradeTime().startsWith("09:36")){
                return null;
            }
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), data.getTradePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
            if(i==1){
                if(!upperPrice){
                    plankFlag = false;
                }
            }
            if(i>=2){
                if(upperPrice&&data.getTradeType()==1){
                    if(!plankFlag){
                        return data.getTradeTime();
                    }
                    plankFlag = true;
                }else{
                    plankFlag = false;
                }
            }
        }
        return null;
    }

    public void getAllDayProfit(List<StockKbar> stockKbars,StockKbar stockKbar,YiZhiHangYeBuyDTO buyDTO){
        boolean flag = false;
        int i = 0;
        for (StockKbar kbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = kbar.getTradeAmount().divide(new BigDecimal(kbar.getTradeQuantity()*100));
                BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(stockKbar.getAdjHighPrice()), stockKbar.getAdjHighPrice());
                buyDTO.setProfit(rate);
            }
            if(kbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public Map<String, BlockInfo> getAllHangYe(){
        Map<String, BlockInfo> map = new HashMap<>();
        List<BlockInfo> hangYes = Lists.newArrayList();
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        for (BlockInfo blockInfo:blockInfos){
            if(blockInfo.getBlockCode().startsWith("8803")||blockInfo.getBlockCode().startsWith("8804")||blockInfo.getBlockCode().startsWith("8805")){
                map.put(blockInfo.getBlockCode(),blockInfo);
            }
        }
        return map;
    }
    public Map<String, List<BlockStockDetail>> getAllHangYeDetails(){
        Map<String, List<BlockStockDetail>> map = new HashMap<>();
        List<BlockInfo> hangYes = Lists.newArrayList();
        List<BlockInfo> blockInfos = blockInfoService.listByCondition(new BlockInfoQuery());
        for (BlockInfo blockInfo:blockInfos){
            if(blockInfo.getBlockCode().startsWith("8803")||blockInfo.getBlockCode().startsWith("8804")){
                hangYes.add(blockInfo);
            }
        }
        for (BlockInfo blockInfo:hangYes){
            BlockStockDetailQuery query = new BlockStockDetailQuery();
            query.setBlockCode(blockInfo.getBlockCode());
            List<BlockStockDetail> details = blockStockDetailService.listByCondition(query);
            map.put(blockInfo.getBlockCode(),details);
        }
        return map;
    }


    public void calBeforeRate(List<StockKbar> stockKbars,StockKbar buyKbar,BoxStockBuyDTO buyDTO){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i = 0;
        StockKbar lastKbar = null;
        StockKbar nextKbar = null;
        int planks = 0;
        boolean continueFlag = true;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i>=2){
                boolean upperFlag = PriceUtil.isHistoryUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice(), nextKbar.getKbarDate());
                if (upperFlag&&continueFlag){
                    planks++;
                }else{
                    buyDTO.setPrePlanks(planks);
                    continueFlag = false;
                }
            }
            if(i>=1){
                totalAmount = totalAmount.add(stockKbar.getTradeAmount());
                if(i==5){
                    BigDecimal avgAmount = totalAmount.divide(new BigDecimal(5), 2, BigDecimal.ROUND_HALF_UP);
                    buyDTO.setAvgAmountDay5(avgAmount);
                }
            }
            if(i==1){
                lastKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay10(rate);
            }
            if(i==31){
                BigDecimal rate = PriceUtil.getPricePercentRate(lastKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buyDTO.setRateDay30(rate);
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            nextKbar = stockKbar;
        }
    }

    public Map<String, Integer> endPlanksMap(List<CirculateInfo> circulateInfos){
        Map<String, Integer> map = new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            //query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                if(preStockKbar!=null){
                    boolean historyPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(historyPrice){
                        Integer counts = map.get(stockKbar.getKbarDate());
                        if(counts==null){
                            counts = 0;
                        }
                        counts = counts+1;
                        map.put(stockKbar.getKbarDate(),counts);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return map;
    }
    public Map<String,String> getPreTradeDate(){
        Map<String, String> map = new HashMap<>();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        String preTradeDate = null;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if(preTradeDate!=null){
                map.put(format,preTradeDate);
            }
            preTradeDate = format;
        }
        return map;
    }

    public void calProfit(List<StockKbar> stockKbars,StockKbar buyKbar,BoxStockBuyDTO buyDTO){
        boolean flag = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
                if(!CollectionUtils.isEmpty(datas)){
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    Integer count = 0;
                    for (ThirdSecondTransactionDataDTO data:datas){
                        count = count+data.getTradeQuantity();
                        totalAmount = totalAmount.add(data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity())));
                        if(data.getTradeTime().startsWith("13")){
                            break;
                        }
                    }
                    if(count>0){
                        BigDecimal avgPrice = totalAmount.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP);
                        BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                        BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(buyDTO.getBuyPrice(), buyKbar);
                        BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
                        buyDTO.setProfit(profit);
                        return;
                    }
                }

            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
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

    public BoxStockBuyDTO calBoxBuy(StockKbar stockKbar,CirculateInfo circulateInfo,StockKbar preStockKbar){
        BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getHighPrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
        if(highRate.compareTo(new BigDecimal(3))<=0){
            return null;
        }
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        BoxStockBuyDTO buyDTO = null;
        int index = 0;
        int firstHighIndex = 0;
        int buyIndex = 0;
        String firstHighTime = null;
        BigDecimal firstHighPrice = null;
        BigDecimal firstHighTimeRate = null;
        BigDecimal beforeBuyTradeAmount = BigDecimal.ZERO;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(60);
        for (ThirdSecondTransactionDataDTO data:datas){
            index++;
            limitQueue.offer(data);
            BigDecimal tradePrice = data.getTradePrice();
            BigDecimal tradeAmount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            beforeBuyTradeAmount = beforeBuyTradeAmount.add(tradeAmount);
            BigDecimal rate = PriceUtil.getPricePercentRate(tradePrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            if(firstHighTime!=null){
                Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
                Date firstHighDate = DateUtil.parseDate(firstHighTime, DateUtil.HH_MM);
                Date newHighDate = DateUtil.addStockMarketMinutes(firstHighDate, 30);
                if(data.getTradePrice().compareTo(firstHighPrice)>0){
                    if(date.after(newHighDate)){
                        buyIndex = index;
                        BigDecimal firtHighRate = PriceUtil.getPricePercentRate(firstHighPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                        BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                        Integer betweenMinute = DateUtil.calBetweenStockMinute(firstHighTime, data.getTradeTime());
                        BigDecimal buyTimeRaiseRate = calRaiseRate(limitQueue, stockKbar, preStockKbar);
                        buyDTO = new BoxStockBuyDTO();
                        buyDTO.setStockCode(stockKbar.getStockCode());
                        buyDTO.setStockName(stockKbar.getStockName());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setBuyPrice(data.getTradePrice());
                        buyDTO.setBuyTime(data.getTradeTime());
                        buyDTO.setBuyTimeRaiseRate(buyTimeRaiseRate);
                        buyDTO.setCirculateZ(circulateInfo.getCirculateZ());
                        buyDTO.setTotalCirculateZ(circulateInfo.getCirculate());
                        buyDTO.setFirstHighRate(firtHighRate);
                        buyDTO.setFirstHighTime(firstHighTime);
                        buyDTO.setFirstHighRaiseRate(firstHighTimeRate);
                        buyDTO.setBuyTimeTradeAmount(beforeBuyTradeAmount);
                        buyDTO.setPreTradeAmount(preStockKbar.getTradeAmount());
                        buyDTO.setBetweenTime(betweenMinute);
                        buyDTO.setOpenRate(openRate);
                        break;
                    }else{
                        firstHighTime = data.getTradeTime();
                        firstHighPrice = data.getTradePrice();
                        firstHighTimeRate = calRaiseRate(limitQueue, stockKbar, preStockKbar);
                        firstHighIndex = index;
                    }
                }
            }
            if(firstHighTime==null && rate.compareTo(new BigDecimal("3"))>0){
                firstHighTime = data.getTradeTime();
                firstHighPrice = tradePrice;
                firstHighTimeRate = calRaiseRate(limitQueue, stockKbar, preStockKbar);
                firstHighIndex = index;
            }
        }
        if(buyDTO!=null){
            int i = 0;
            BigDecimal beforeLowPrice = null;
            BigDecimal betweenLowPrice = null;
            for (ThirdSecondTransactionDataDTO data:datas){
                i++;
                if(i<=firstHighIndex){
                    if(beforeLowPrice==null||data.getTradePrice().compareTo(beforeLowPrice)<0){
                        beforeLowPrice = data.getTradePrice();
                    }
                }
                if(i>firstHighIndex&&i<buyIndex){
                    if(betweenLowPrice==null||data.getTradePrice().compareTo(betweenLowPrice)<0){
                        betweenLowPrice = data.getTradePrice();
                    }
                }
                if(i>=buyIndex){
                    break;
                }
            }
            BigDecimal beforeLowRate = PriceUtil.getPricePercentRate(beforeLowPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            BigDecimal betweenLowRate = PriceUtil.getPricePercentRate(betweenLowPrice.subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            buyDTO.setBeforeHighLowRate(beforeLowRate);
            buyDTO.setAfterHighLowRate(betweenLowRate);
        }
        return buyDTO;
    }

    public BigDecimal calRaiseRate(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,StockKbar stockKbar,StockKbar preStockKbar){
        if(limitQueue==null||limitQueue.size()<2){
            return null;
        }
        ThirdSecondTransactionDataDTO last = limitQueue.getLast();
        ThirdSecondTransactionDataDTO first = limitQueue.peek();
        BigDecimal subtract = last.getTradePrice().subtract(first.getTradePrice());
        BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(subtract, stockKbar);
        BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice, preStockKbar.getAdjClosePrice());
        return rate;
    }

    public static void main(String[] args) {
        /*Date date = DateUtil.addStockMarketMinutes(DateUtil.parseDate("11:29", DateUtil.HH_MM), 30);
        System.out.println(DateUtil.format(date,DateUtil.HH_MM));*/
        Integer integer = DateUtil.calBetweenStockMinute("11:23", "11:29");
        System.out.println(integer);
    }

}
