package com.bazinga.replay.component;

import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockCommonReplayService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.tradex.enums.KCate;
import com.tradex.exception.TradeException;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class StockCommonReplayComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    @Autowired
    private StockCommonReplayService stockCommonReplayService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;


    public void saveCommonReplay(Date date){

        try {
            List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
            Date currentDate = date;
            if(!commonComponent.isTradeDate(currentDate)){
                log.info("当前日期不是交易日期");
                return;
            }
            Date preTradeDate = commonComponent.preTradeDate(currentDate);
            String currentKbarDate = DateUtil.format(currentDate,DateUtil.yyyyMMdd);
            for (CirculateInfo circulateInfo : circulateInfos) {
                DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 2);
                List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(dataTable);

                for (KBarDTO kBarDTO : kBarDTOS) {
                    if(DateUtil.format(preTradeDate,DateUtil.yyyy_MM_dd).equals(kBarDTO.getDateStr())){
                        BigDecimal closePrice = kBarDTO.getEndPrice();
                        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), currentDate);
                        List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getPreOneHourData(data);
                        Float averagePrice = historyTransactionDataComponent.calAveragePrice(list);
                        String uniqueKey = circulateInfo.getStockCode() + SymbolConstants.UNDERLINE + currentKbarDate;
                        StockCommonReplay byUniqueKey = stockCommonReplayService.getByUniqueKey(uniqueKey);
                        if(byUniqueKey == null){

                            StockCommonReplay stockCommonReplay = new StockCommonReplay();
                            stockCommonReplay.setStockCode(circulateInfo.getStockCode());
                            stockCommonReplay.setStockName(circulateInfo.getStockName());
                            stockCommonReplay.setAvgPre1Price(new BigDecimal(averagePrice.toString()));
                            stockCommonReplay.setKbarDate(currentKbarDate);
                            stockCommonReplay.setUniqueKey(uniqueKey);
                            stockCommonReplay.setAvgPre1Rate(PriceUtil.getPricePercentRate(new BigDecimal(averagePrice.toString()).subtract(closePrice),closePrice));
                            stockCommonReplayService.save(stockCommonReplay);
                            log.info("保存护盘数据成功 stockCode{} kbarDate{}",circulateInfo.getStockCode(),stockCommonReplay.getKbarDate());
                        }


                    }
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }


    }

}
