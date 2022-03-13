package com.bazinga.component;

import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.DaPanDropDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.service.*;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class GuaiLilvComponent {
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
    private StockAverageLineService stockAverageLineService;

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void chuangYeBuy(){
        List<DaPanDropDTO> dailys = getStockUpperShowInfo();
        List<Object[]> datas = Lists.newArrayList();
        for(DaPanDropDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getDropRate());
            list.add(dto.getDays());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","日期","下跌比例","下跌天数"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("zz500大跌日期",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("zz500大跌日期");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<DaPanDropDTO> getStockUpperShowInfo(){
        List<DaPanDropDTO> list = Lists.newArrayList();
        List<KBarDTO> stockKBars = getStockKBars("399905");
        LimitQueue<KBarDTO> limitQueue = new LimitQueue<>(20);
        for (KBarDTO kBarDTO:stockKBars){
            limitQueue.offer(kBarDTO);
            DaPanDropDTO daPanDropDTO = dropRate(limitQueue);
            if(daPanDropDTO!=null){
                daPanDropDTO.setTradeDate(kBarDTO.getDateStr());
                list.add(daPanDropDTO);
            }
        }
        return list;
    }
    public DaPanDropDTO dropRate(LimitQueue<KBarDTO> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        Iterator<KBarDTO> iterator = limitQueue.iterator();
        List<KBarDTO> list = new ArrayList<>();
        while (iterator.hasNext()){
            KBarDTO next = iterator.next();
            list.add(next);
        }
        List<KBarDTO> reverse = Lists.reverse(list);
        KBarDTO first = null;
        KBarDTO nextKbar = null;
        BigDecimal dropRate = null;
        int i = 0;
        for (KBarDTO kBarDTO:reverse){
            if(first!=null){
                i++;
                BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getEndPrice().subtract(kBarDTO.getEndPrice()), kBarDTO.getEndPrice());
                if(rate.compareTo(BigDecimal.ZERO)>=0){
                    if(i==1){
                        return null;
                    }
                    dropRate = PriceUtil.getPricePercentRate(first.getEndPrice().subtract(nextKbar.getEndPrice()), nextKbar.getEndPrice());
                    DaPanDropDTO daPanDropDTO = new DaPanDropDTO();
                    daPanDropDTO.setDays(i-1);
                    daPanDropDTO.setDropRate(dropRate);
                    return daPanDropDTO;
                }
            }
            if(first==null) {
                first = kBarDTO;
            }
            nextKbar = kBarDTO;
        }
        return null;
    }



    public List<KBarDTO> getStockKBars(String stockCode){
        List<KBarDTO> list = Lists.newArrayList();
        for (int i=2000;i>=0;i--) {
            DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, i, 1);
            KBarDTO kbar = KBarDTOConvert.convertSZKBar(securityBars);
            list.add(kbar);
        }
        return list;
    }


    public static void main(String[] args) {

    }


}
