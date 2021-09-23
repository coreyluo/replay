package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.HotBlockDropBuyDTO;
import com.bazinga.dto.LevelDTO;
import com.bazinga.dto.StockRateDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
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
public class HotBlockBestBuyComponent {
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

    public static Map<String,BlockLevelDTO> levelMap = new ConcurrentHashMap<>(8192);



    public void hotDrop(List<HotBlockDropBuyDTO> dailys){




        List<Object[]> datas = Lists.newArrayList();
        for(HotBlockDropBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());

            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","流通z","交易日期","板块代码","板块名称","大涨涨幅","大涨时排名","大涨间隔日期","大跌幅度","买入日开盘板块涨幅","买入日开盘板块排名",
                "股票买入日开盘涨幅","股票买入日开盘排名","股票大涨日涨幅","股票大涨日排名",
                "股票大跌日涨幅","股票大跌日排名","股票大跌日成交量","买入前3日涨跌幅","买入前5日涨跌幅","买入前10日涨跌幅","大涨日上板时间","买入前5日平均成交量","大跌日板块内收盘上涨数量","大跌日板块内收盘下跌数量","大跌日板块内板数",
                "买入前5日板数","大涨次日是否开一字","大涨次日开盘涨幅","大涨日板块5日涨幅","大涨日板块10日涨幅","买入前5日开一字次数","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("热门大跌",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("热门大跌");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void splitRegion(List<HotBlockDropBuyDTO> dailys){
        List<LevelDTO> levelDTOS  = Lists.newArrayList();
        for (HotBlockDropBuyDTO hotBlockDropBuyDTO:dailys){
            String key = hotBlockDropBuyDTO.getStockCode()+hotBlockDropBuyDTO.getTradeDate();
            LevelDTO levelDTO = new LevelDTO();
            levelDTO.setKey(key);
            levelDTO.setRate(hotBlockDropBuyDTO.getBlockDropRate());
            levelDTOS.add(levelDTO);
        }
        Collections.sort(levelDTOS);
    }

}
