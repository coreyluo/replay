package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.dto.*;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockInfoService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.util.Conf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class RealBuyOrSellComponent {
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private BlockLevelReplayComponent blockLevelReplayComponent;
    @Autowired
    private StockKbarService stockKbarService;
    public void test(){
        Date date = DateUtil.parseDate("2021-09-02 09:34:11", DateUtil.DEFAULT_FORMAT);
        Date preTradeDate = DateUtil.parseDate("2021-09-01 00:00:00", DateUtil.DEFAULT_FORMAT);
        Map<String, List<GatherTransactionDataDTO>> transactionData = stockTransactionData(DateTimeUtils.getDate000000(date));
        System.out.println(transactionData);
        //blockRealInfo("601778",date,preTradeDate,transactionData);
    }
    public void blockRealInfo(String stockCode,Date tradeDate,Date preTradeDate,Map<String, List<ThirdSecondTransactionDataDTO>> transactionDataMap){
        Date date000000 = DateTimeUtils.getDate000000(tradeDate);
        String timeStampHHMM = DateUtil.format(tradeDate, DateUtil.HH_MM);
        //昨日股票流入流出信息
        RealBuyOrSellDTO stockRealBuyOrSell = realBuyOrSell(stockCode, DateTimeUtils.getDate000000(preTradeDate));
        //今日板块排名
        BlockLevelDTO blockLevelDTO = stockTimeStampLevel(stockCode, timeStampHHMM, transactionDataMap, preTradeDate);
        if(blockLevelDTO==null||blockLevelDTO.getBlockCode()==null){
            return;
        }
        //昨日板块流入流出信息
        BlockRealBuyOrSellDTO blockRealBuyOrSell = getBlockRealBuyOrSell(blockLevelDTO.getBlockCode(), blockLevelDTO.getBlockName(), preTradeDate);
        if(blockRealBuyOrSell==null||blockRealBuyOrSell.getMoney()==null){
            return;
        }

    }

    public BlockRealBuyOrSellDTO getBlockRealBuyOrSell(String blockCode,String blockName,Date date){
        ThsBlockStockDetailQuery thsBlockStockDetailQuery = new ThsBlockStockDetailQuery();
        thsBlockStockDetailQuery.setBlockCode(blockCode);
        List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(thsBlockStockDetailQuery);
        if(CollectionUtils.isEmpty(details)){
            return null;
        }
        BlockRealBuyOrSellDTO blockRealBuyOrSellDTO = new BlockRealBuyOrSellDTO();
        blockRealBuyOrSellDTO.setBlockCode(blockCode);
        blockRealBuyOrSellDTO.setBlockName(blockName);
        for (ThsBlockStockDetail detail:details){
            RealBuyOrSellDTO realBuyOrSellDTO = realBuyOrSell(detail.getStockCode(), date);
            if(realBuyOrSellDTO!=null && (realBuyOrSellDTO.getRealSell()!=null || realBuyOrSellDTO.getRealBuy()!=null)){
                Integer stockCounts = blockRealBuyOrSellDTO.getStockCounts()==null?1:blockRealBuyOrSellDTO.getStockCounts()+1;
                blockRealBuyOrSellDTO.setStockCounts(stockCounts);
            }
            if(realBuyOrSellDTO!=null&&realBuyOrSellDTO.getRealBuy()!=null) {
                BigDecimal realBuy = blockRealBuyOrSellDTO.getRealBuy() == null ? realBuyOrSellDTO.getRealBuy() : realBuyOrSellDTO.getRealBuy().add(blockRealBuyOrSellDTO.getRealBuy());
                blockRealBuyOrSellDTO.setRealBuy(realBuy);
            }
            if(realBuyOrSellDTO!=null&&realBuyOrSellDTO.getRealSell()!=null) {
                BigDecimal realSell = blockRealBuyOrSellDTO.getRealSell() == null ? realBuyOrSellDTO.getRealSell() : realBuyOrSellDTO.getRealSell().add(blockRealBuyOrSellDTO.getRealSell());
                blockRealBuyOrSellDTO.setRealSell(realSell);
            }
        }
        BigDecimal money = null;
        if(blockRealBuyOrSellDTO.getRealSell()!=null||blockRealBuyOrSellDTO.getRealBuy()!=null){
            if(blockRealBuyOrSellDTO.getRealSell()==null){
                money = blockRealBuyOrSellDTO.getRealBuy();
            }else if(blockRealBuyOrSellDTO.getRealBuy()==null){
                money = BigDecimal.ZERO.subtract(blockRealBuyOrSellDTO.getRealSell());
            }else{
                money = blockRealBuyOrSellDTO.getRealBuy().subtract(blockRealBuyOrSellDTO.getRealSell());
            }
        }
        blockRealBuyOrSellDTO.setMoney(money);
        return blockRealBuyOrSellDTO;
    }


    /**
     * 股票在那个时刻的最高板块排名
     * @param timeStamp  时间戳 09:14   10:22
     * @param transactionMap  当日所有的整分 分时成交数据
     */
    public BlockLevelDTO  stockTimeStampLevel(String stockCode,String timeStamp,Map<String, List<ThirdSecondTransactionDataDTO>> transactionMap,Date preDate){
        List<StockRateDTO> rates = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Date time = DateUtil.parseDate(timeStamp, DateUtil.HH_MM);
        for (CirculateInfo circulateInfo:circulateInfos){
            List<ThirdSecondTransactionDataDTO> list = transactionMap.get(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(list)){
               continue;
            }
            ThirdSecondTransactionDataDTO thirdSecondTransactionDataDTO = null;
            for (ThirdSecondTransactionDataDTO dto:list){
                Date dtoTime = DateUtil.parseDate(dto.getTradeTime(), DateUtil.HH_MM);
                if(dtoTime.before(time)){
                    thirdSecondTransactionDataDTO = dto;
                }
            }
            if(thirdSecondTransactionDataDTO==null){
                continue;
            }
            String uniqueKey = circulateInfo.getStockCode() + "_" + DateUtil.format(preDate, DateUtil.yyyyMMdd);
            StockKbar preKbar = stockKbarService.getByUniqueKey(uniqueKey);
            if(preKbar==null){
                continue;
            }
            BigDecimal rate = PriceUtil.getPricePercentRate(thirdSecondTransactionDataDTO.getTradePrice().subtract(preKbar.getClosePrice()), preKbar.getClosePrice());
            StockRateDTO rateDTO = new StockRateDTO();
            rateDTO.setRate(rate);
            rateDTO.setStockCode(circulateInfo.getStockCode());
            rateDTO.setStockName(circulateInfo.getStockName());
            rates.add(rateDTO);
        }
        Map<String, BlockLevelDTO> blockLevelDTOMap = blockLevelReplayComponent.calBlockLevelDTO(rates);
        BlockLevelDTO blockLevel = blockLevelReplayComponent.getBlockLevel(blockLevelDTOMap, stockCode);
        return blockLevel;
    }


    public Map<String, List<GatherTransactionDataDTO>> stockTransactionData(Date date){
        Map<String, List<GatherTransactionDataDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo);
            List<ThirdSecondTransactionDataDTO> datas = null;
            try {
                datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), date);
            }catch (Exception e){
                log.info("拿取分时成交异常");
            }
            Map<String, GatherTransactionDataDTO> stockTransactionMap = new HashMap<>();
            List<String> times  = Lists.newArrayList();
            List<GatherTransactionDataDTO> list   = Lists.newArrayList();
            for(ThirdSecondTransactionDataDTO data:datas){
                GatherTransactionDataDTO dto = stockTransactionMap.get(data.getTradeTime());
                if(dto==null){
                    dto = new GatherTransactionDataDTO();
                    dto.setTradeTime(data.getTradeTime());
                    stockTransactionMap.put(data.getTradeTime(),dto);
                    times.add(data.getTradeTime());
                }
                dto.setTradePrice(data.getTradePrice());
                Integer tradeQuantity  = dto.getTradeQuantity()==null?data.getTradeQuantity():data.getTradeQuantity()+dto.getTradeQuantity();
                BigDecimal money = data.getTradePrice().multiply(new BigDecimal(data.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal tradeMoney = dto.getTradeMoney() == null ? money : dto.getTradeMoney().add(money);
                dto.setTradeQuantity(tradeQuantity);
                dto.setTradeMoney(tradeMoney);
            }
            for (String time:times){
                if(stockTransactionMap.get(time)!=null&&stockTransactionMap.get(time).getTradeMoney()!=null) {
                    list.add(stockTransactionMap.get(time));
                }
            }
            map.put(circulateInfo.getStockCode(),list);
        }
        return map;
    }

    public RealBuyOrSellDTO realBuyOrSell(String stockCode,Date date){
        Map<String,BuyOrSellDTO> map = new HashMap<>();
        List<String> timeStamps = Lists.newArrayList();
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData(stockCode, DateTimeUtils.getDate000000(date));
        for (ThirdSecondTransactionDataDTO dto:data){
            BuyOrSellDTO buyOrSellDTO = map.get(dto.getTradeTime());
            if(buyOrSellDTO==null){
                buyOrSellDTO = new BuyOrSellDTO();
                buyOrSellDTO.setStockCode(stockCode);
                buyOrSellDTO.setTimeStamp(dto.getTradeTime());
                map.put(dto.getTradeTime(),buyOrSellDTO);
            }
            buyOrSellDTO.setPrice(dto.getTradePrice());
            BigDecimal money = dto.getTradePrice().multiply(new BigDecimal(dto.getTradeQuantity() * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
           if(buyOrSellDTO.getMoney()==null){
               buyOrSellDTO.setMoney(money);
               buyOrSellDTO.setQuantity(dto.getTradeQuantity());
           }else{
               buyOrSellDTO.setMoney(buyOrSellDTO.getMoney().add(money));
               buyOrSellDTO.setQuantity(buyOrSellDTO.getQuantity()+dto.getTradeQuantity());
           }
           if(!timeStamps.contains(dto.getTradeTime())){
               timeStamps.add(dto.getTradeTime());
           }
        }
        BigDecimal buyMoney = null;
        BigDecimal sellMoney = null;
        BigDecimal prePrice = null;
        for (String timeStamp:timeStamps){
            BuyOrSellDTO buyOrSellDTO = map.get(timeStamp);
            if(prePrice!=null){
                if(buyOrSellDTO.getPrice().compareTo(prePrice)==1){
                    if(buyMoney==null){
                        buyMoney = buyOrSellDTO.getMoney();
                    }else{
                        buyMoney = (buyMoney).add(buyOrSellDTO.getMoney());
                    }
                }else if(buyOrSellDTO.getPrice().compareTo(prePrice)==-1){
                    if(sellMoney==null){
                        sellMoney = buyOrSellDTO.getMoney();
                    }else{
                        sellMoney = (sellMoney).add(buyOrSellDTO.getMoney());
                    }
                }
            }
            prePrice = buyOrSellDTO.getPrice();
        }
        BigDecimal money = null;
        if(sellMoney!=null||buyMoney!=null){
            if(sellMoney==null){
                money = buyMoney;
            }else if(buyMoney==null){
                money = BigDecimal.ZERO.subtract(sellMoney);
            }else{
                money = buyMoney.subtract(sellMoney);
            }
        }
        RealBuyOrSellDTO realBuyOrSellDTO = new RealBuyOrSellDTO();
        realBuyOrSellDTO.setStockCode(stockCode);
        realBuyOrSellDTO.setDateStr(DateUtil.format(date,DateUtil.yyyy_MM_dd));
        realBuyOrSellDTO.setRealBuy(buyMoney);
        realBuyOrSellDTO.setRealSell(sellMoney);
        realBuyOrSellDTO.setMoney(money);
        return realBuyOrSellDTO;
    }

}
