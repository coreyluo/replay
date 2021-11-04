package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.enums.PlankSignEnum;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.StockRehabilitationService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BadPlankComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private StockRehabilitationService stockRehabilitationService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;


    public void badPlankJudge(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        StockPlankDailyQuery stockPlankDailyQuery = new StockPlankDailyQuery();
        stockPlankDailyQuery.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        stockPlankDailyQuery.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<StockPlankDaily> dailies = stockPlankDailyService.listByCondition(stockPlankDailyQuery);
        for (StockPlankDaily daily:dailies){
            String stockCode = daily.getStockCode();
            String stockName = daily.getStockName();
            try {
                /*if (!stockCode.equals("600505")) {
                    continue;
                }*/
                List<StockKbar> stockKBars = getStockKBars(stockCode,DateUtil.format(date,DateUtil.yyyyMMdd));
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘烂板没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                List<ThirdSecondTransactionDataDTO> datas = currentDayTransactionDataComponent.getData(stockCode);
                if(CollectionUtils.isEmpty(datas)){
                    log.info("复盘烂板没有获取到分时成交数据 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                boolean badPlank = isBadPlank(datas, daily.getStockCode(), stockKBars.get(1).getClosePrice());
                if(badPlank){
                    daily.setBadPlankType(1);
                    stockPlankDailyService.updateById(daily);
                }
            }catch (Exception e){
                log.info("复盘烂板异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }
    public boolean isBadPlank(List<ThirdSecondTransactionDataDTO> datas,String stockCode,BigDecimal preEndPrice){
        int plankTimes = 0;
        boolean plankFlag = false;
        for (ThirdSecondTransactionDataDTO dto:datas){
            boolean upperPrice = PriceUtil.isUpperPrice(stockCode, dto.getTradePrice(), preEndPrice);
            if(dto.getTradeTime().equals("09:25")){
                if(upperPrice) {
                    plankFlag = true;
                }else{
                    plankFlag = false;
                }
                continue;
            }
            if(dto.getTradeType()!=0&&dto.getTradeType()!=1){
                continue;
            }
            boolean plankData = false;
            if(dto.getTradeType()==1&&upperPrice){
                plankData = true;
            }
            if(!plankFlag&&plankData){
                plankTimes++;
            }
            if(plankData){
                plankFlag = true;
            }else{
                plankFlag = false;
            }
        }
        if(plankTimes>=4){
            return true;
        }
        return false;
    }




    public List<StockKbar> getStockKBars(String stockCode,String tradeDateStr){
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.setStockCode(stockCode);
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
        stockKbarQuery.setLimit(2);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<2||!stockKbars.get(0).getKbarDate().equals(tradeDateStr)){
            return null;
        }
        return stockKbars;
    }


}
