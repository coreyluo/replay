package com.bazinga.replay.component;



import com.bazinga.base.Sort;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.google.common.collect.Lists;
import com.tradex.util.StockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CommonComponent {
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private StockKbarService stockKbarService;

    public Date preTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateTimeUtils.getDate000000(date));
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return new Date();
        }
        return dates.get(0).getTradeDate();
    }

    public Date afterTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate235959(date));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return new Date();
        }
        return dates.get(0).getTradeDate();
    }

    public boolean isTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return false;
        }
        return true;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(300);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            List<StockKbar> list = deleteNewStockTimes(reverse, 300);
            return list;
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

    public boolean isNewStock(String stockCode, String kbarDate) {
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        query.setKbarDateTo(kbarDate);
        query.setLimit(30);
        List<StockKbar> stockKbarList = stockKbarService.listByCondition(query);
        return stockKbarList.size() < 30;
    }

}
