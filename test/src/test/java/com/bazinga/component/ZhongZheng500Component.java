package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
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


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ZhongZheng500Component {
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
    public void zz500Buy(){
        List<Zz500BuyDTO> zz500BuyDTOS = zz500LevelInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(Zz500BuyDTO dto:zz500BuyDTOS){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getTradeDate());
            list.add(dto.getType());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","交易日期","1收盘前50 2收盘后50 3 10天涨幅高 4 10天涨幅低"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("中证500",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("中证500");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<Zz500BuyDTO> zz500LevelInfo(){
        List<Zz500BuyDTO> list = Lists.newArrayList();
        List<KBarDTO> zz500ZhiShu = getZZ500ZhiShu();
        Map<String, List<StockKbarRateDTO>> map = zz500KbarInfo();
        KBarDTO preKbar = null;
        for (KBarDTO zzKbar:zz500ZhiShu){
            if(preKbar!=null){
                String dateyyyyMMdd = zzKbar.getDateStr();
                Date date = DateUtil.parseDate(dateyyyyMMdd, DateUtil.yyyyMMdd);
                BigDecimal zzRate = PriceUtil.getPricePercentRate(zzKbar.getEndPrice().subtract(preKbar.getEndPrice()), preKbar.getEndPrice());
                if(zzRate.compareTo(new BigDecimal(-1))==-1&&date.after(DateUtil.parseDate("20210301",DateUtil.yyyyMMdd))){
                    List<StockKbarRateDTO> dtos = map.get(dateyyyyMMdd);
                    if(!CollectionUtils.isEmpty(dtos)&&dtos.size()>300){
                        List<Zz500BuyDTO> stockBuys = getStockBuys(dtos);
                        list.addAll(stockBuys);
                        /*if(list.size()>0){
                            return list;
                        }*/
                    }
                }
            }
            preKbar = zzKbar;
        }

        return list;
    }

    public List<Zz500BuyDTO> getStockBuys(List<StockKbarRateDTO> list){
        List<Zz500BuyDTO> result = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        List<StockKbarRateDTO> listHighEnd = Lists.newArrayList();
        List<StockKbarRateDTO> listLowEnd = Lists.newArrayList();
        List<StockKbarRateDTO> listHighTen = Lists.newArrayList();
        List<StockKbarRateDTO> listLowTen = Lists.newArrayList();
        listHighEnd.addAll(list);
        listLowEnd.addAll(list);
        listHighTen.addAll(list);
        listLowTen.addAll(list);
        StockKbarRateDTO.endRateHighSort(listHighEnd);
        StockKbarRateDTO.endRateLowSort(listLowEnd);
        StockKbarRateDTO.day10RateHighSort(listHighTen);
        StockKbarRateDTO.day10RateLowSort(listLowTen);
        int i = 0;
        for (StockKbarRateDTO stockKbarRateDTO:listHighEnd){
            i++;
            if(i<=50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setType(1);
                result.add(buyDTO);
            }
        }
        int j = 0;
        for (StockKbarRateDTO stockKbarRateDTO:listLowEnd){
            j++;
            if(j<=50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setType(2);
                result.add(buyDTO);
            }
        }
        int m = 0;
        for (StockKbarRateDTO stockKbarRateDTO:listHighTen){
            m++;
            if(m<=50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setType(3);
                result.add(buyDTO);
            }
        }
        int n = 0;
        for (StockKbarRateDTO stockKbarRateDTO:listLowTen){
            n++;
            if(n<=50){
                Zz500BuyDTO buyDTO = new Zz500BuyDTO();
                buyDTO.setStockCode(stockKbarRateDTO.getStockCode());
                buyDTO.setStockName(stockKbarRateDTO.getStockName());
                buyDTO.setCirculateZ(stockKbarRateDTO.getCirculateZ());
                buyDTO.setTradeDate(stockKbarRateDTO.getTradeDate());
                buyDTO.setType(4);
                result.add(buyDTO);
            }
        }

        return result;
    }

    public Map<String, List<StockKbarRateDTO>>  zz500KbarInfo(){
        Map<String, List<StockKbarRateDTO>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode(), 300);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(11);
            StockKbar preKbar = null;
            StockKbarRateDTO preRateDTO = null;
            for (StockKbar stockKbar:stockKbars){
                if(preKbar!=null){
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    StockKbarRateDTO rateDTO = new StockKbarRateDTO();
                    rateDTO.setStockCode(circulateInfo.getStockCode());
                    rateDTO.setStockName(circulateInfo.getStockName());
                    rateDTO.setCirculateZ(circulateInfo.getCirculateZ());
                    rateDTO.setStockKbar(stockKbar);
                    rateDTO.setTradeDate(stockKbar.getKbarDate());
                    rateDTO.setOpenRate(openRate);
                    rateDTO.setEndRate(endRate);
                    BigDecimal day10Rate = getDay10Rate(limitQueue);
                    rateDTO.setDay10Rate(day10Rate);
                    List<StockKbarRateDTO> stockKbarRateDTOS = map.get(rateDTO.getTradeDate());
                    if(stockKbarRateDTOS==null){
                        stockKbarRateDTOS = Lists.newArrayList();
                        map.put(rateDTO.getTradeDate(),stockKbarRateDTOS);
                    }
                    stockKbarRateDTOS.add(rateDTO);
                    if(preRateDTO!=null){
                        preRateDTO.setNextDayStockKbar(stockKbar);
                    }
                    preRateDTO = rateDTO;
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public BigDecimal getDay10Rate(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<11){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar first = null;
        int i = 0;
        while (iterator.hasNext()){
            i++;
            StockKbar stockKbar = iterator.next();
            if(i==1){
                first = stockKbar;
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(first.getAdjClosePrice()), first.getAdjClosePrice());
                return rate;
            }
        }
        return null;
    }

    public BigDecimal profit(StockKbarRateDTO buyDTO){
        if(buyDTO.getNextDayStockKbar()==null){
            return null;
        }
        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(buyDTO.getStockCode(), DateUtil.parseDate(buyDTO.getNextDayStockKbar().getKbarDate(), DateUtil.yyyyMMdd));
        avgPrice = chuQuanAvgPrice(avgPrice, buyDTO.getNextDayStockKbar());
        if(avgPrice==null||buyDTO.getStockKbar()==null||buyDTO.getStockKbar().getAdjClosePrice()==null){
            System.out.println(buyDTO.getStockCode()+"没有收盘价格");
            return null;
        }
        BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyDTO.getStockKbar().getAdjClosePrice()), buyDTO.getStockKbar().getAdjClosePrice());
        return profit;
    }

    public List<StockKbar> getStockKBarsDelete30Days(String stockCode, int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            if(reverse.size()<=20){
                return null;
            }
            List<StockKbar> bars = reverse.subList(20, reverse.size());
            return bars;
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


    public List<KBarDTO> getZZ500ZhiShu(){
        List<KBarDTO> list = Lists.newArrayList();
        for (int i=300;i>=0;i--) {
            DataTable securityBars = TdxHqUtil.getBlockSecurityBars(KCate.DAY, "000905", i, 1);
            KBarDTO kbar = KBarDTOConvert.convertSZKBar(securityBars);
            list.add(kbar);
        }
        return list;
    }

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<Integer> integers = list.subList(3, list.size());
        System.out.println(integers);
    }


}
