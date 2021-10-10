package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
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
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
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
public class StockGraphComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void graphBuy(List<ZiDongHuaDTO> dataList){
        List<GraphBuyDTO> dailys = Lists.newArrayList();
        for (ZiDongHuaDTO excelDTO:dataList){
            try {
                if (!excelDTO.getStockCode().equals("605066")) {
                    continue;
                }
                System.out.println(excelDTO.getStockCode());
                GraphBuyDTO graphBuyDTO = new GraphBuyDTO();
                BeanUtils.copyProperties(graphBuyDTO,excelDTO);
                String tradeDateStr = excelDTO.getTradeDate();
                String buyTime = excelDTO.getBuyTime();
                Date tradeDate = DateUtil.parseDate(tradeDateStr + " " + buyTime + ":00", DateUtil.DEFAULT_FORMAT);
                StockKbarQuery query = new StockKbarQuery();
                query.setStockCode(excelDTO.getStockCode());
                query.addOrderBy("kbar_date", Sort.SortType.ASC);
                List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
                StockKbar preKbar = null;
                if(!CollectionUtils.isEmpty(stockKbars)){
                    for(StockKbar stockKbar:stockKbars) {
                        if(stockKbar.getKbarDate().equals(DateUtil.format(tradeDate, DateUtil.yyyyMMdd))) {
                            break;
                        }
                        preKbar = stockKbar;
                    }
                }
                if(preKbar!=null){
                    longLegGraph(excelDTO.getStockCode(),tradeDate,preKbar.getClosePrice(),graphBuyDTO);
                }
                dailys.add(graphBuyDTO);
                /*if(dailys.size()>=10){
                    break;
                }*/
            }catch (Exception e){
                log.info("复盘数据 异常  e：{}", e);
            }
        }
        List<Object[]> datas = Lists.newArrayList();
        for(GraphBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getBuyAmount());
            list.add(dto.getSellAmount());
            list.add(dto.getRealProfit());
            if(dto.getRealProfitRate()!=null) {
                list.add(new BigDecimal(dto.getRealProfitRate()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }else {
                list.add(null);
            }
            list.add(dto.getRealPlanks());
            list.add(dto.getBuyTime());
            list.add(dto.getIncreaseRate());
            list.add(dto.getSpaceTime());
            list.add(dto.getIncreaseRate10());
            list.add(dto.getSpaceTime10());

            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","交易日期","买金额","卖金额","正盈利","盈亏比","连板情况","买入时间","涨幅","最低点到上板时候心跳间隔","10跳涨幅","10跳最低点到上板时候心跳间隔"};

        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("图形买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("图形买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void longLegGraph(String stockCode, Date buyTime,BigDecimal preEndPrice,GraphBuyDTO graphBuyDTO){
        Date date = DateTimeUtils.getDate000000(buyTime);
        String dateHHMM = DateUtil.format(buyTime, DateUtil.HH_MM);
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, date);
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue = new LimitQueue<>(61);
        LimitQueue<ThirdSecondTransactionDataDTO> limitQueue10 = new LimitQueue<>(11);
        for (ThirdSecondTransactionDataDTO data:datas){
            limitQueue.offer(data);
            limitQueue10.offer(data);
            Integer tradeType = data.getTradeType();
            String tradeTime = data.getTradeTime();
            BigDecimal tradePrice = data.getTradePrice();
            boolean isUpper = PriceUtil.isUpperPrice(stockCode, tradePrice, preEndPrice);
            boolean isSell = false;
            if(tradeType==null || tradeType==1){
                isSell = true;
            }
            if(tradeTime.equals(dateHHMM)){
                if(isUpper&&isSell){
                    break;
                }
            }
        }
        BigDecimal lowestPrice = null;
        BigDecimal highPrice = null;
        int i = 0;
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator.next();
            i++;
            if(lowestPrice==null||next.getTradePrice().compareTo(lowestPrice)==-1){
                lowestPrice = next.getTradePrice();
                i=0;
            }
            highPrice = next.getTradePrice();
        }
        if(lowestPrice!=null&&highPrice!=null){
            BigDecimal rate = PriceUtil.getPricePercentRate(highPrice.subtract(lowestPrice), preEndPrice);
            int interDays = i;
            graphBuyDTO.setSpaceTime(interDays);
            graphBuyDTO.setIncreaseRate(rate);
        }

        BigDecimal lowestPrice10 = null;
        BigDecimal highPrice10 = null;
        int i10 = 0;
        Iterator<ThirdSecondTransactionDataDTO> iterator10 = limitQueue10.iterator();
        while (iterator10.hasNext()){
            ThirdSecondTransactionDataDTO next = iterator10.next();
            i10++;
            if(lowestPrice10==null||next.getTradePrice().compareTo(lowestPrice10)==-1){
                lowestPrice10 = next.getTradePrice();
                i10=0;
            }
            highPrice10 = next.getTradePrice();
        }
        if(lowestPrice10!=null&&highPrice10!=null){
            BigDecimal rate = PriceUtil.getPricePercentRate(highPrice10.subtract(lowestPrice10), preEndPrice);
            int interDays = i10;
            graphBuyDTO.setSpaceTime10(interDays);
            graphBuyDTO.setIncreaseRate10(rate);
        }

    }



}
