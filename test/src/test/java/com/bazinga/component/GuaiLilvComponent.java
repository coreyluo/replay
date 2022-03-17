package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.DaPanDropDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class GuaiLilvComponent {
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
        List<DaPanDropDTO> list = Lists.newArrayList();
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("999999");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(23);
        StockKbar preStockKbar = null;
        int i = 10;
        for (StockKbar stockKbar:stockKbars){
            System.out.println(stockKbar.getKbarDate());
            if(preStockKbar!=null){
                List<DaPanDropDTO> daPanDropDTOS = getHistoryInfo(stockKbar, limitQueue,preStockKbar);
                if(!CollectionUtils.isEmpty(daPanDropDTOS)) {
                    //list.addAll(daPanDropDTOS);
                    list.add(daPanDropDTOS.get(0));
                }
            }
            limitQueue.offer(stockKbar);
            preStockKbar = stockKbar;
        }
        return list;
    }
    public List<DaPanDropDTO> getHistoryInfo(StockKbar stockKbar,LimitQueue<StockKbar> limitQueue,StockKbar preStockKbar){
        List<DaPanDropDTO> list = new ArrayList<>();
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), stockKbar.getKbarDate());
        if(stockKbar.getKbarDate().equals("20190506")){
            System.out.println(111111111);
        }
        Long totalGather = null;
        Long total0930 = null;
        Long totalCurrent = 0L;
        String preTimeStamp = "09:30";
        String buyTimeStamp = null;
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueueSpeed = new LimitQueue<ThirdSecondTransactionDataDTO>(36);
        for(ThirdSecondTransactionDataDTO data:datas){
            limitQueueSpeed.offer(data);
            if(data.getTradeTime().equals("09:25")){
                totalGather = data.getTradeQuantity().longValue();
            }
            if(data.getTradeTime().equals("09:30")){
                if(total0930==null){
                    total0930 = data.getTradeQuantity().longValue();
                }else{
                    total0930 = total0930+data.getTradeQuantity().longValue();
                }
            }
            if(!preTimeStamp.equals(data.getTradeTime())){
                totalCurrent = 0L;
                preTimeStamp = data.getTradeTime();
            }
            totalCurrent = totalCurrent+data.getTradeQuantity().longValue();
            if(data.getTradeTime().equals("09:25")||data.getTradeTime().equals("09:30")||data.getTradeTime().equals("13:00")||data.getTradeTime().startsWith("15")){
                continue;
            }
            BigDecimal avgPrice = calAvgPrice(limitQueue, data.getTradePrice());
            if(avgPrice==null){
                continue;
            }
            if(totalCurrent==0||total0930==null||totalGather==null){
                continue;
            }
            BigDecimal percent = new BigDecimal(totalCurrent).divide(new BigDecimal(total0930+totalGather), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal glv = (data.getTradePrice().subtract(avgPrice).divide(avgPrice,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))).setScale(2);
            BigDecimal relativeOpenRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(stockKbar.getOpenPrice()), preStockKbar.getClosePrice());
            BigDecimal raiseRate = raiseSpeed(limitQueueSpeed, preStockKbar.getClosePrice());
            //if(glv.compareTo(new BigDecimal("-4.8"))==-1&&percent.compareTo(new BigDecimal("0.4"))==1/*&&relativeOpenRate.compareTo(new BigDecimal("-1.8"))==-1*/){
            Date currentDate = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
            Date signDate = DateUtil.parseDate("09:58",DateUtil.HH_MM);
            if(currentDate.after(signDate)) {
                if (glv.compareTo(new BigDecimal("-2.4")) == -1 && raiseRate != null && raiseRate.compareTo(new BigDecimal(0.75)) > 0) {
                    DaPanDropDTO daPanDropDTO = new DaPanDropDTO();
                    daPanDropDTO.setDropRate(glv);
                    daPanDropDTO.setPercent(percent);
                    daPanDropDTO.setTimeStamp(data.getTradeTime());
                    daPanDropDTO.setTradeDate(stockKbar.getKbarDate());
                    daPanDropDTO.setRelativeOpenRate(raiseRate);
                    list.add(daPanDropDTO);
                }
            }else {
                if (glv.compareTo(new BigDecimal("-2.4")) == -1 && raiseRate != null && raiseRate.compareTo(new BigDecimal(1)) > 0) {
                    if (stockKbar.getKbarDate().equals("20200319")) {
                        System.out.println(111111111);
                    }
                    DaPanDropDTO daPanDropDTO = new DaPanDropDTO();
                    daPanDropDTO.setDropRate(glv);
                    daPanDropDTO.setPercent(percent);
                    daPanDropDTO.setTimeStamp(data.getTradeTime());
                    daPanDropDTO.setTradeDate(stockKbar.getKbarDate());
                    daPanDropDTO.setRelativeOpenRate(raiseRate);
                    list.add(daPanDropDTO);
                }
            }
        }
        return list;
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
