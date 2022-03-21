package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.BoxBuyDTO;
import com.bazinga.dto.DaPanDropDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
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
public class BoxBuyComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void guaiLiLv(){
        List<DaPanDropDTO> dailys = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(DaPanDropDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getPercent());
            list.add(dto.getDropRate());
            list.add(dto.getTimeStamp());
            list.add(dto.getRelativeOpenRate());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","日期","比例","乖离率","时间","涨跌幅"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("乖离率",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("乖离率");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<DaPanDropDTO> getStockUpperShowInfo(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(circulateInfo.getStockCode());
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(11);
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
            }
        }
        List<DaPanDropDTO> list = Lists.newArrayList();
        return list;
    }
    public BoxBuyDTO calBoxBuy(StockKbar stockKbar,StockKbar preStockKbar, LimitQueue<StockKbar> limitQueue){
        if(limitQueue==null||limitQueue.size()<11){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        int i = 0;
        BigDecimal highPrice = null;
        BigDecimal lowPrice = null;
        BigDecimal maxExchangeMoney = null;
        BigDecimal totalExchangeMoney = BigDecimal.ZERO;
        while (iterator.hasNext()){
            i++;
            StockKbar kbar = iterator.next();
            if(i<=10) {
                if (highPrice == null || kbar.getAdjHighPrice().compareTo(highPrice) == 1) {
                    highPrice = kbar.getAdjHighPrice();
                }
                if (lowPrice == null || kbar.getAdjLowPrice().compareTo(lowPrice) == -1) {
                    lowPrice = kbar.getAdjLowPrice();
                }
                if(maxExchangeMoney==null||kbar.getTradeAmount().compareTo(maxExchangeMoney)==1){
                    maxExchangeMoney = kbar.getTradeAmount();
                }
            }
            totalExchangeMoney = totalExchangeMoney.add(kbar.getTradeAmount());
        }
        if(highPrice!=null&&lowPrice!=null){

            BigDecimal boxRate = PriceUtil.getPricePercentRate(highPrice.subtract(lowPrice), lowPrice);
            BigDecimal avgExchangeMoneyDay11 = totalExchangeMoney.divide(new BigDecimal(11), 2, BigDecimal.ROUND_HALF_UP);
            if(boxRate.compareTo(new BigDecimal("8"))<0 && stockKbar.getAdjClosePrice().compareTo(highPrice)==1 && stockKbar.getAdjClosePrice().compareTo(preStockKbar.getAdjClosePrice())==1){
                BoxBuyDTO boxBuyDTO = new BoxBuyDTO();
                boxBuyDTO.setBoxRate(boxRate);
                boxBuyDTO.setStockCode(stockKbar.getStockCode());
                boxBuyDTO.setStockName(stockKbar.getStockName());
                boxBuyDTO.setBuyKbar(stockKbar);
                boxBuyDTO.setBoxMaxExchangeMoney(maxExchangeMoney);
                boxBuyDTO.setAvgExchangeMoneyDay11(avgExchangeMoneyDay11);

            }
        }
        return null;
    }

    private BigDecimal boxPercent(BigDecimal highPrice,BigDecimal lowPrice,StockKbar stockKbar){
        BigDecimal subPrice = (highPrice.subtract(lowPrice)).multiply(new BigDecimal(0.25)).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal halfPrice = (highPrice.add(lowPrice)).divide(new BigDecimal(2), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal upperPrice = halfPrice.add(subPrice).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal downPrice = halfPrice.divide(subPrice).setScale(2,BigDecimal.ROUND_HALF_UP);
        return null;

    }

    public BigDecimal raiseSpeed(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal preEndPrice){
        if(limitQueue.size()<=2){
            return null;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        BigDecimal lowPrice = null;
        while (iterator.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator.next();
            if(lowPrice==null||next.getTradePrice().compareTo(lowPrice)==-1){
                lowPrice = next.getTradePrice();
            }
        }
        BigDecimal rate = PriceUtil.getPricePercentRate(limitQueue.getLast().getTradePrice().subtract(lowPrice), preEndPrice);
        return rate;
    }

    public BigDecimal calAvgPrice(LimitQueue<StockKbar> limitQueue,BigDecimal currentPrice){
        if(limitQueue.size()<23){
            return null;
        }
        BigDecimal totalPrice = currentPrice;
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            totalPrice = totalPrice.add(next.getClosePrice());
        }
        BigDecimal rate = totalPrice.divide(new BigDecimal(24), 4, BigDecimal.ROUND_HALF_UP);
        return rate;
    }


    public static void main(String[] args) {

    }


}
