package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BadPeopleBuyDTO;
import com.bazinga.dto.FeiDaoBuyDTO;
import com.bazinga.dto.FeiDaoRateDTO;
import com.bazinga.dto.StockPlankTimeInfoDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
public class BadPeopleComponent {
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
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    public List<ThsBlockInfo> THS_BLOCK_INFOS = Lists.newArrayList();
    public Map<String,List<ThsBlockStockDetail>> THS_BLOCK_STOCK_DETAIL_MAP = new HashMap<>();

    public void jieFeiDaoInfo(){
        List<FeiDaoBuyDTO> feiDaos = getFeiDao();
        List<Object[]> datas = Lists.newArrayList();
        for(FeiDaoBuyDTO dto:feiDaos){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getPlanks());
            list.add(dto.getBuyTime());
            list.add(dto.getPlankSecond());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","买入日期","板高","开板时间","板住时间","溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("10天5板数据",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("接飞刀");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }




    public List<FeiDaoBuyDTO> getFeiDao(){
        List<FeiDaoBuyDTO> result = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            /*if(!circulateInfo.getStockCode().equals("001234")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(12);
            LimitQueue<Object> limitQueue31 = new LimitQueue<>(31);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                limitQueue31.offer(stockKbar);
                if(preKbar!=null) {
                    Boolean highPlank = PriceUtil.isUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice());
                    BadPeopleBuyDTO badPeopleBuyDTO = new BadPeopleBuyDTO();
                    if(highPlank){
                        Integer planks = calEndPlanks(limitQueue);
                        if(stockKbar.getLowPrice().compareTo(preKbar.getClosePrice())!=1) {
                            if (planks != null && planks >= 1) {
                                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                                boolean havePlank = isPlank(stockKbar, preKbar.getClosePrice(), datas);
                                if (havePlank) {
                                    List<FeiDaoBuyDTO> buyDTOS = buyTime(stockKbar, preKbar.getClosePrice(), datas);
                                    BigDecimal avgPrice = calProfit(stockKbars, stockKbar.getKbarDate());
                                    for (FeiDaoBuyDTO buyDTO:buyDTOS){
                                        buyDTO.setStockCode(circulateInfo.getStockCode());
                                        buyDTO.setStockName(circulateInfo.getStockName());
                                        buyDTO.setCirculateZ(new BigDecimal(circulateInfo.getCirculateZ()).divide(new BigDecimal(100000000),2,BigDecimal.ROUND_HALF_UP));
                                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                                        BigDecimal rate = PriceUtil.getPricePercentRate(avgPrice.subtract(buyDTO.getBuyAvgPrice()), buyDTO.getBuyAvgPrice());
                                        buyDTO.setProfit(rate);
                                        buyDTO.setPlanks(planks+1);
                                        result.add(buyDTO);
                                    }
                                }
                            }
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return result;
    }

    public void isBadManPlank(LimitQueue<StockKbar> limitQueue,BadPeopleBuyDTO badPeopleBuyDTO){
       /* if(limitQueue.size()<30){
            return;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        List<StockKbar> list = Lists.newArrayList();
        while (iterator.hasNext()){
            StockKbar stockKbar = iterator.next();
            list.add(stockKbar);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        int i  = 0;
        StockKbar highKbar = null;
        int highDays = 0;
        for (StockKbar stockKbar:reverse){
            if(i>=1&&i<=10){
                if(highKbar==null||stockKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())==1){
                    highKbar = stockKbar;
                    highDays = i;
                }
            }
            i++;
        }
        int j=0;
        StockKbar lowKbar  = null;
        int lowDays = 0;
        for (StockKbar stockKbar:reverse){
            if(j)
            if(j>highDays && j<=highDays+20){
                if(stockKbar.getAdjHighPrice().compareTo(highKbar.getAdjHighPrice())==1){
                    return;
                }
                if(lowKbar==null||stockKbar.getAdjLowPrice().compareTo(lowKbar.getAdjLowPrice())==-1){
                    lowKbar = stockKbar;
                    lowDays = j;
                }
            }
            j++;
        }*/
    }

    public List<FeiDaoBuyDTO> buyTime(StockKbar stockKbar,BigDecimal preEndPrice,List<ThirdSecondTransactionDataDTO> datas){
        List<FeiDaoBuyDTO> result = Lists.newArrayList();
        List<FeiDaoBuyDTO> list = Lists.newArrayList();
        boolean isPlank = false;
        boolean buyFlag = false;
        int times = 0;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            Integer tradeType = data.getTradeType();
            boolean isPlankS = false;
            if (tradeType == 1 && upperPrice) {
                isPlankS = true;
            }
            if(isPlank&&!isPlankS){
                buyFlag = true;
                FeiDaoBuyDTO feiDaoBuyDTO = new FeiDaoBuyDTO();
                LimitQueue<ThirdSecondTransactionDataDTO> limitQueue100 = new LimitQueue<>(100);
                feiDaoBuyDTO.setLimitQueue100(limitQueue100);
                feiDaoBuyDTO.setPlankSecond(times);
                feiDaoBuyDTO.setBuyTime(data.getTradeTime());
                list.add(feiDaoBuyDTO);
            }
            if(buyFlag){
                FeiDaoBuyDTO feiDaoBuyDTO = list.get(list.size() - 1);
                if(feiDaoBuyDTO.getLimitQueue100().size()<100) {
                    feiDaoBuyDTO.getLimitQueue100().offer(data);
                }
            }
            if(isPlankS){
                times = times+3;
                isPlank = true;
                buyFlag = false;
            }else{
                times   = 0;
                isPlank = false;
            }
        }
        for (FeiDaoBuyDTO buyDTO:list){
            buyAvgPrice(preEndPrice,buyDTO);
            if(buyDTO.getBuyAvgPrice()==null){
                continue;
            }
            result.add(buyDTO);
        }
        return result;
    }

    public void buyAvgPrice(BigDecimal preEndPrice,  FeiDaoBuyDTO buyDTO){
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue100 = buyDTO.getLimitQueue100();
        if(limitQueue100==null||limitQueue100.size()<1){
            return;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue100.iterator();
        int i = 0;
        boolean buyFlag = true;
        BigDecimal lowPrice = null;
        String firstBuyTime = null;
        while(iterator.hasNext()){
            i++;
            ThirdSecondTransactionDataDTO data = iterator.next();
            if(i<=10){
                if(data.getTradePrice().compareTo(preEndPrice)!=1){
                    if(firstBuyTime==null) {
                        firstBuyTime = data.getTradeTime();
                    }
                    buyFlag = true;
                }
            }
            if(lowPrice==null||data.getTradePrice().compareTo(lowPrice)==-1){
                lowPrice = data.getTradePrice();
            }

        }
        List<FeiDaoRateDTO> buys = Lists.newArrayList();
        BigDecimal priceTotal = BigDecimal.ZERO;
        if(buyFlag){
            BigDecimal rate = PriceUtil.getPricePercentRate(lowPrice.subtract(preEndPrice), preEndPrice);
            if(rate.compareTo(new BigDecimal(0))!=1){
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(preEndPrice);
                buys.add(buy);
                priceTotal = priceTotal.add(preEndPrice);
            }
            if(rate.compareTo(new BigDecimal(-2))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-2), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-4))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-4), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-6))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-6), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
            if(rate.compareTo(new BigDecimal(-8))!=1){
                BigDecimal price = PriceUtil.absoluteRateToPrice(new BigDecimal(-8), preEndPrice);
                FeiDaoRateDTO buy = new FeiDaoRateDTO();
                buy.setBuyPrice(price);
                buys.add(buy);
                priceTotal = priceTotal.add(price);
            }
        }
        if(buys.size()>0){
            BigDecimal divide = priceTotal.divide(new BigDecimal(buys.size()), 2, BigDecimal.ROUND_HALF_UP);
            buyDTO.setBuyAvgPrice(divide);
        }

    }




    public boolean isPlank(StockKbar stockKbar,BigDecimal preEndPrice,List<ThirdSecondTransactionDataDTO> datas){
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice);
            Integer tradeType = data.getTradeType();
            if(tradeType==1&&upperPrice){
                return true;
            }
        }
        return false;
    }

    public Integer calEndPlanks(LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<3){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> reverse = Lists.reverse(list);
        StockKbar nextKbar = null;
        int planks  = 0;
        int i = 0;
        for (StockKbar stockKbar:reverse){
            if(i>=2) {
                boolean endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getClosePrice(), stockKbar.getClosePrice());
                if (!endUpper) {
                    endUpper = PriceUtil.isUpperPrice(nextKbar.getStockCode(), nextKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice());
                }
                if (endUpper) {
                    planks++;
                } else {
                    return planks;
                }
            }
            i++;
            nextKbar = stockKbar;
        }
        return planks;
    }


    public BigDecimal calProfit(List<StockKbar> stockKbars,String buyDate){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                return avgPrice;
            }
            if(stockKbar.getKbarDate().equals(buyDate)){
                flag = true;
            }
        }
        return null;
    }



    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<21){
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
        StockPlankTimeInfoDTO infoDTO1 = new StockPlankTimeInfoDTO();
        infoDTO1.setPlanks(1);
        StockPlankTimeInfoDTO infoDTO2 = new StockPlankTimeInfoDTO();
        infoDTO2.setPlanks(3);
        StockPlankTimeInfoDTO infoDTO3 = new StockPlankTimeInfoDTO();
        infoDTO3.setPlanks(3);
        StockPlankTimeInfoDTO infoDTO4 = new StockPlankTimeInfoDTO();
        infoDTO4.setPlanks(4);
        StockPlankTimeInfoDTO infoDTO5 = new StockPlankTimeInfoDTO();
        infoDTO5.setPlanks(6);
        List<StockPlankTimeInfoDTO> list = Lists.newArrayList();
        list.add(infoDTO2);
        list.add(infoDTO4);
        list.add(infoDTO1);
        list.add(infoDTO2);
        list.add(infoDTO5);
        List<StockPlankTimeInfoDTO> haha = list.subList(list.size() - 3,list.size());

        List<StockPlankTimeInfoDTO> stockPlankTimeInfoDTOS = StockPlankTimeInfoDTO.planksLevel(list);
        System.out.println(haha.get(0));
    }


}
