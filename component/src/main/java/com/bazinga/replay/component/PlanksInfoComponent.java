package com.bazinga.replay.component;

import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.query.StockReplayDailyQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockCommonReplayService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.StockReplayDailyService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PlanksInfoComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockReplayDailyService stockReplayDailyService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;


    public void planksInfo(Date date){

        try {
            StockReplayDailyQuery query = new StockReplayDailyQuery();
            query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
            query.setTradeDateTo(DateTimeUtils.getDate235959(date));
            List<StockReplayDaily> replayDailies = stockReplayDailyService.listByCondition(query);
            int totalPlanks = 0;
            int onePlanks = 0;
            int twoPlanks = 0;
            int threePlanks = 0;
            int totalBreaks = 0;
            int oneBreaks = 0;
            int twoBreaks = 0;
            int threeBreaks = 0;
            for (StockReplayDaily daily:replayDailies){
                if(MarketUtil.isChuangYe(daily.getStockCode())){
                    continue;
                }

                if(daily.getPlankType().equals("1连板")){

                } else if(daily.getPlankType().equals("2连板")||daily.getPlankType().equals("3天2板")){

                } else if(daily.getPlankType().equals("3连板")||daily.getPlankType().equals("4天3板")){

                }else{

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }


    }


}
