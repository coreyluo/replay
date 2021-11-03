package com.bazinga.component;

import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.*;
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
        for (ZhuanZaiExcelDTO zhuanZai:zhuanZais){
            System.out.println(zhuanZai.getStockCode());
            List<StockKbar> kbars = getKbars(zhuanZai.getStockCode(), zhuanZai.getStockName());
            List<ZhuanZaiBuyDTO> zhuanZaiBuyDTOS = buyReason(kbars,plankMap,zhuanZai);
            alls.addAll(zhuanZaiBuyDTOS);
            if(alls.size()>=10){
                break;
            }
        }
        for (ZhuanZaiBuyDTO zhuanZaiBuyDTO:alls){
            Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(zhuanZaiBuyDTO.getTradeDate(), DateUtil.yyyyMMdd));
            List<String> stocks = plankMap.get(DateUtil.format(preTradeDate, DateUtil.yyyyMMdd));
            if(!CollectionUtils.isEmpty(stocks)){
                zhuanZaiBuyDTO.setHavePlank(stocks.size());
            }else{
                zhuanZaiBuyDTO.setHavePlank(0);
            }
        }
        for(ZhuanZaiBuyDTO dto:alls){
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
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","市值","交易日期","买入价格","买入时候相对开盘涨幅","买入时间","前一天几只票停牌过","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("转债",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("转债");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<ZhuanZaiBuyDTO> buyReason(List<StockKbar> stockKbars,Map<String, List<String>> plankMap,ZhuanZaiExcelDTO zhuanZai){
        List<ZhuanZaiBuyDTO> buys = new ArrayList<>();
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=1){
            return buys;
        }
        BigDecimal preEndPrice = null;
        for (StockKbar stockKbar:stockKbars){
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
                    List<BigDecimal> sellPrices = Lists.newArrayList();
                    boolean buyFlag = false;
                    ZhuanZaiBuyDTO zhuanZaiBuyDTO = new ZhuanZaiBuyDTO();
                    for (ThirdSecondTransactionDataDTO data:datas){
                        avgPriceCalDto.add(data);
                        BigDecimal avgPrice = calAvgPrice(avgPriceCalDto);
                        if(buyFlag){
                            afterBuys.add(data);
                            BigDecimal raiseRate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(zhuanZaiBuyDTO.getBuyPrice()), preEndPrice);
                        }
                        if("09:59".equals(data.getTradeTime())){
                            if(data.getTradePrice().compareTo(stockKbar.getOpenPrice())==1&&data.getTradePrice().compareTo(avgPrice)==1){
                                buyFlag = true;
                                zhuanZaiBuyDTO.setStockCode(stockKbar.getStockCode());
                                zhuanZaiBuyDTO.setStockName(stockKbar.getStockName());
                                zhuanZaiBuyDTO.setBuyTime(data.getTradeTime());
                                zhuanZaiBuyDTO.setTradeDate(stockKbar.getKbarDate());
                                zhuanZaiBuyDTO.setBuyPrice(data.getTradePrice());
                                zhuanZaiBuyDTO.setMarketAmount(zhuanZai.getMarketAmount());
                                BigDecimal rate = PriceUtil.getPricePercentRate(data.getTradePrice().subtract(stockKbar.getOpenPrice()), preEndPrice);
                                zhuanZaiBuyDTO.setRelativeOpenRate(rate);
                                buys.add(zhuanZaiBuyDTO);
                                break;
                            }
                        }
                    }
                }
            }
            preEndPrice = stockKbar.getClosePrice();
        }
        return buys;
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
        if(totalCount==0){
            BigDecimal divide = totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            return divide;
        }
        return null;
    }

    public static BigDecimal calUpperPrice(BigDecimal yesterdayPrice) {
        return yesterdayPrice.multiply(CommonConstant.UPPER_RATE300).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public List<StockKbar> getKbars(String stockCode,String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 50);
        List<StockKbar> stockKbars = StockKbarConvert.convert(securityBars,stockCode,stockName);
        return stockKbars;
    }



}
