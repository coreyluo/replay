package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.dto.BuyOrSellDTO;
import com.bazinga.dto.OtherExcelDTO;
import com.bazinga.dto.RealBuyOrSellDTO;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.ThsBlockInfoService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
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
    public void blockRealInfo(String blockCode,Date date){
        ThsBlockStockDetailQuery thsBlockStockDetailQuery = new ThsBlockStockDetailQuery();
        thsBlockStockDetailQuery.setBlockCode(blockCode);
        List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(thsBlockStockDetailQuery);
        Map<String, RealBuyOrSellDTO> map = new HashMap<>();
        for (ThsBlockStockDetail detail:thsBlockStockDetails){
            RealBuyOrSellDTO realBuyOrSellDTO = realBuyOrSell(detail.getStockCode(), date);
            map.put(detail.getStockCode(),realBuyOrSellDTO);
        }
        ThsBlockInfoQuery thsBlockInfoQuery = new ThsBlockInfoQuery();
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(thsBlockInfoQuery);

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


    public Map<String, Map<String,ThirdSecondTransactionDataDTO>> realBuyOrSellMin(Date date){
        Map<String, Map<String,ThirdSecondTransactionDataDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo);
            Map<String, ThirdSecondTransactionDataDTO> dtoMap = map.get(circulateInfo.getStockCode());
            if(dtoMap==null){
                dtoMap = new HashMap<>();
                map.put(circulateInfo.getStockCode(),dtoMap);
            }
            List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), date);
            for (ThirdSecondTransactionDataDTO data:datas){
                dtoMap.put(data.getTradeTime(),data);
            }
        }
        return map;
    }


}
