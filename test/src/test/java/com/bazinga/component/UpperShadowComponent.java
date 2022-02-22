package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.FastPlankDTO;
import com.bazinga.dto.HotBlockDropBuyDTO;
import com.bazinga.dto.ShadowKbarDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import javafx.scene.effect.Shadow;
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
public class UpperShadowComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void fastPlank(){
        List<FastPlankDTO> dailys = Lists.newArrayList();

        List<Object[]> datas = Lists.newArrayList();
        for(FastPlankDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getMarketMoney());
            list.add(dto.getBuyKbar().getKbarDate());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通z","流通市值","买入日期","开盘涨幅","集合成交额","第一次能买入时间","买入价格",
                "3日涨幅","5日涨幅","10日涨幅", "30日涨幅","前一日255涨幅","前一日收盘涨幅","前一日收盘-255","前一日230至收盘最低点涨幅",
                "前10日板数","前10日最低价格","前一日均价涨幅","前10日平均成交量","前11日-50日平均成交量","板价/10日最低","前10日平均换手率","前10日平均成交量/前50日平均成交量","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("早上板股票",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("早上板股票");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void getStockUpperShowInfo(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, Date> startDateMap = kbarStartDate(circulateInfos);
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20220103",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        TradeDatePool preTradeDatePool = null;
        TradeDatePool prePreTradeDatePool = null;
        for(TradeDatePool tradeDatePool:tradeDatePools){
            if(prePreTradeDatePool!=null) {
                List<ShadowKbarDTO> shadows = getStockKbarByDate(circulateInfos, DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),
                        DateUtil.format(preTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd),DateUtil.format(prePreTradeDatePool.getTradeDate(), DateUtil.yyyyMMdd), startDateMap);
                if(shadows.size()>3){
                    int useSize = shadows.size() / 3;
                    ShadowKbarDTO.marketMoneySort(shadows);
                    shadows = shadows.subList(0,useSize);
                }

            }
            prePreTradeDatePool = preTradeDatePool;
            preTradeDatePool = tradeDatePool;
        }


    }

    public void calShadowLength(List<ShadowKbarDTO> shadows){
        for (ShadowKbarDTO shadow:shadows){
            StockKbar preStockKbar = shadow.getPreStockKbar();
            BigDecimal upperShadow = (preStockKbar.getAdjHighPrice().subtract(preStockKbar.getAdjClosePrice())).divide(preStockKbar.getAdjClosePrice(), 2, BigDecimal.ROUND_HALF_UP);

        }
    }

    public Map<String, Date> kbarStartDate(List<CirculateInfo> circulateInfos){
        Map<String, Date> map = new HashMap<>();
        int i = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println("开始日期"+circulateInfo.getStockCode()+"==="+i);
            List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKBars)){
                continue;
            }
            List<StockKbar> list = Lists.newArrayList();
            for (StockKbar stockKbar:stockKBars){
                if(stockKbar.getTradeQuantity()>=100){
                    list.add(stockKbar);
                }
            }
           /* if(list.size()<=180){
                continue;
            }
            list = stockKBars.subList(180, list.size());*/
            Date date = DateUtil.parseDate(list.get(0).getKbarDate(), DateUtil.yyyyMMdd);
            map.put(circulateInfo.getStockCode(),date);
            if(map.size()>=100){
                return map;
            }
        }
        return map;
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

    public List<ShadowKbarDTO> getStockKbarByDate(List<CirculateInfo> circulateInfos,String dateStr,String preDateStr,String prePreDateStr,Map<String,Date> map){
        List<ShadowKbarDTO> list = new ArrayList<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(dateStr);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbarQuery preKbarQuery = new StockKbarQuery();
        preKbarQuery.setKbarDate(preDateStr);
        List<StockKbar> preStockKbars = stockKbarService.listByCondition(preKbarQuery);
        StockKbarQuery prePreKbarQuery = new StockKbarQuery();
        prePreKbarQuery.setKbarDate(prePreDateStr);
        List<StockKbar> prePreStockKbars = stockKbarService.listByCondition(prePreKbarQuery);

        Map<String, StockKbar> tradeDateMap = new HashMap<>();
        Map<String, StockKbar> preTradeDateMap = new HashMap<>();
        Map<String, StockKbar> prePreTradeDateMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getTradeQuantity()>=100) {
                tradeDateMap.put(stockKbar.getStockCode(), stockKbar);
            }
        }
        for (StockKbar stockKbar:preStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                preTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        for (StockKbar stockKbar:prePreStockKbars){
            if(stockKbar.getTradeQuantity()>=100){
                preTradeDateMap.put(stockKbar.getStockCode(),stockKbar);
            }
        }
        for(CirculateInfo circulateInfo:circulateInfos){
            StockKbar stockKbar = tradeDateMap.get(circulateInfo.getStockCode());
            StockKbar preStockKbar = preTradeDateMap.get(circulateInfo.getStockCode());
            StockKbar prePreStockKbar = prePreTradeDateMap.get(circulateInfo.getStockCode());
            if(stockKbar!=null&&preStockKbar!=null) {
                ShadowKbarDTO shadowKbarDTO = new ShadowKbarDTO();
                shadowKbarDTO.setStockCode(circulateInfo.getStockCode());
                shadowKbarDTO.setStockName(circulateInfo.getStockName());
                shadowKbarDTO.setStockKbar(stockKbar);
                shadowKbarDTO.setPreStockKbar(preStockKbar);
                shadowKbarDTO.setPrePreStockKbar(prePreStockKbar);
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculate()).multiply(shadowKbarDTO.getPreStockKbar().getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP);
                shadowKbarDTO.setMarketMoney(marketMoney);
                list.add(shadowKbarDTO);
            }
        }
        return list;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> list = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>=100){
                    list.add(stockKbar);
                }
            }
            List<StockKbar> kbars = deleteNewStockTimes(list, 1200);
            return kbars;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<StockKbar> deleteNewStockTimes(List<StockKbar> list,int size){
        List<StockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        StockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }

    public static void main(String[] args) {
        ArrayList<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);

    }


}
