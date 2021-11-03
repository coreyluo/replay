package com.bazinga.component;

import com.bazinga.dto.*;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.model.DropFactor;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.service.*;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
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

        for (ZhuanZaiExcelDTO zhuanZai:zhuanZais){
            List<StockKbar> kbars = getKbars(zhuanZai.getStockCode(), zhuanZai.getStockName());
            System.out.println(kbars);
        }
       /* for(FastRaiseBankerDTO dto:da){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","流通z","市值","交易日期","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("庄逼",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("庄逼");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }
    public List<StockKbar> getKbars(String stockCode,String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 100);
        List<StockKbar> stockKbars = StockKbarConvert.convert(securityBars,stockCode,stockName);
        System.out.println(stockKbars);
        return stockKbars;
    }



}
