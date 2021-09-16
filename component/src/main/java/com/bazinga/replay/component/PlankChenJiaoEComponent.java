package com.bazinga.replay.component;


import com.bazinga.base.Sort;
import com.bazinga.enums.MarketTypeEnum;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankExchangeMoneyDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class PlankChenJiaoEComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private KbarDtoHandleComponent kbarDtoHandleComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public void exportData(){
        Map<String, PlankExchangeMoneyDTO> map = new HashMap<>();

        List<Object[]> datas = Lists.newArrayList();
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        circulateInfoQuery.setMarketType(MarketTypeEnum.GENERAL.getCode());
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        for (CirculateInfo circulateInfo : circulateInfos) {
            if(circulateInfo.getStockCode().startsWith("688")){
                continue;
            }
            /*if(!circulateInfo.getStock().equals("300238")){
                continue;
            }*/
            System.out.println(circulateInfo.getStockCode());
            highestPlankInfo(circulateInfo,map);
        }
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("2021-08-25",DateUtil.yyyy_MM_dd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> dates = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String dateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            if(!datas.contains(dateStr)){
                dates.add(dateStr);
            }
        }
        for (String dateStr:dates){
            PlankExchangeMoneyDTO dto = map.get(dateStr);
            if(dto==null){
                continue;
            }
            List<Object> list = new ArrayList<>();
            list.add(dateStr);
            list.add(dateStr);
            list.add(new BigDecimal(dto.getPlank150UpMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getPlank150UpAmount());
            list.add(new BigDecimal(dto.getPlank150DownMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getPlank150DownAmount());

            list.add(new BigDecimal(dto.getNoPlank150UpMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getNoPlank150UpAmount());
            list.add(new BigDecimal(dto.getNoPlank150DownMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getNoPlank150DownAmount());

            list.add(new BigDecimal(dto.getPlankEnd150UpMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getPlankEnd150UpAmount());
            list.add(new BigDecimal(dto.getPlankEnd150DownMoney()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(dto.getPlankEnd150DownAmount());

            list.add(dto.getPlankHigh300Count());
            list.add(dto.getPlankEnd300Count());
            list.add(new BigDecimal(dto.getPlankHigh300Money()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(new BigDecimal(dto.getPlankEnd300Money()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));
            list.add(new BigDecimal(dto.getTenRate300Money()/100000000).setScale(2,BigDecimal.ROUND_HALF_UP));

            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","日期","触碰涨停150亿以上","触碰涨停150亿以上个数","触碰涨停150亿以下","触碰涨停150亿以下个数",
                "未触碰涨停150亿以上","未触碰涨停150亿以上个数","未触碰涨停150亿以下","未触碰涨停150亿以下个数",
                "涨停封住150亿以上","涨停封住150亿以上个数","涨停封住150亿以下","涨停封住150亿以下个数",
                "创业板触碰涨停数量","创业板涨停数量","创业板触碰涨停金额","创业板涨停封住金额","创业板10个点以上成交额"};

        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("数据",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("无敌数据");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public void highestPlankInfo(CirculateInfo circulateInfo,Map<String, PlankExchangeMoneyDTO> map){
        List<KBarDTO> kbars = getStockKBars(circulateInfo);
        BigDecimal preEndPrice = null;
        for (KBarDTO kbar:kbars){
            BigDecimal marketValue = marketValue(circulateInfo, kbar.getHighestPrice());
            PlankExchangeMoneyDTO dto = map.get(kbar.getDateStr());
            if(dto==null){
                dto = new PlankExchangeMoneyDTO();
                map.put(kbar.getDateStr(),dto);
            }
            if(preEndPrice!=null){
                boolean highPlank = PriceUtil.isUpperPrice(kbar.getHighestPrice(), preEndPrice);
                if (MarketUtil.isChuangYe(circulateInfo.getStockCode()) && !kbar.getDate().before(DateUtil.parseDate("2020-08-24", DateUtil.yyyy_MM_dd))) {
                    highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), kbar.getHighestPrice(), preEndPrice);
                }
                boolean endPlank = PriceUtil.isUpperPrice(kbar.getEndPrice(), preEndPrice);
                if (MarketUtil.isChuangYe(circulateInfo.getStockCode()) && !kbar.getDate().before(DateUtil.parseDate("2020-08-24", DateUtil.yyyy_MM_dd))) {
                    endPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), kbar.getEndPrice(), preEndPrice);
                }
                if(highPlank){
                    if(marketValue.compareTo(new BigDecimal(150))==1){
                        long exchangeMoney = dto.getPlank150UpMoney() + kbar.getTotalExchangeMoney();
                        dto.setPlank150UpMoney(exchangeMoney);
                        dto.setPlank150UpAmount(dto.getPlank150UpAmount()+1);
                    }else {
                        long exchangeMoney = dto.getPlank150DownMoney() + kbar.getTotalExchangeMoney();
                        dto.setPlank150DownMoney(exchangeMoney);
                        dto.setPlank150DownAmount(dto.getPlank150DownAmount()+1);
                    }
                    if(circulateInfo.getStockCode().startsWith("300")){
                        dto.setPlankHigh300Count(dto.getPlankHigh300Count()+1);
                        dto.setPlankHigh300Money(dto.getPlankHigh300Money()+kbar.getTotalExchangeMoney());
                    }
                }else{
                    if(marketValue.compareTo(new BigDecimal(150))==1){
                        long exchangeMoney = dto.getNoPlank150UpMoney() + kbar.getTotalExchangeMoney();
                        dto.setNoPlank150UpMoney(exchangeMoney);
                        dto.setNoPlank150UpAmount(dto.getNoPlank150UpAmount()+1);
                    }else {
                        long exchangeMoney = dto.getNoPlank150DownMoney() + kbar.getTotalExchangeMoney();
                        dto.setNoPlank150DownMoney(exchangeMoney);
                        dto.setNoPlank150DownAmount(dto.getNoPlank150DownAmount()+1);
                    }
                }
                if(endPlank){
                    if(marketValue.compareTo(new BigDecimal(150))==1){
                        long exchangeMoney = dto.getPlankEnd150UpMoney() + kbar.getTotalExchangeMoney();
                        dto.setPlankEnd150UpMoney(exchangeMoney);
                        dto.setPlankEnd150UpAmount(dto.getPlankEnd150UpAmount()+1);
                    }else {
                        long exchangeMoney = dto.getPlankEnd150DownMoney() + kbar.getTotalExchangeMoney();
                        dto.setPlankEnd150DownMoney(exchangeMoney);
                        dto.setPlankEnd150DownAmount(dto.getPlankEnd150DownAmount()+1);
                    }
                    if(circulateInfo.getStockCode().startsWith("300")){
                        dto.setPlankEnd300Count(dto.getPlankEnd300Count()+1);
                        dto.setPlankEnd300Money(dto.getPlankEnd300Money()+kbar.getTotalExchangeMoney());
                    }
                }
                BigDecimal rate = PriceUtil.getPricePercentRate(kbar.getEndPrice().subtract(preEndPrice), preEndPrice);
                if(circulateInfo.getStockCode().startsWith("300") && rate.compareTo(new BigDecimal(10))==1){
                    dto.setTenRate300Money(dto.getTenRate300Money()+kbar.getTotalExchangeMoney());
                }

            }
            preEndPrice = kbar.getEndPrice();

        }
    }

    public BigDecimal marketValue(CirculateInfo circulateInfo,BigDecimal price){
        BigDecimal marketValue = new BigDecimal(circulateInfo.getCirculateZ()).multiply(price);
        BigDecimal marketValueChange = marketValue.divide(new BigDecimal(100000000), 2, BigDecimal.ROUND_HALF_UP);
        return marketValueChange;
    }


    public List<KBarDTO> getStockKBars(CirculateInfo circulateInfo){
        DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 300);
        List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
        List<KBarDTO> list = kbarDtoHandleComponent.deleteNewStockAllTimes(kbars, 300, circulateInfo.getStockCode());
        return list;
    }

}
