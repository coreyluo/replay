package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlockLevelDTO;
import com.bazinga.dto.OtherExcelDTO;
import com.bazinga.dto.PlankExchangeDailyDTO;
import com.bazinga.dto.StockRateDTO;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class OtherBuyStockComponent {
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


    public void zgcBuy(List<OtherExcelDTO> excelDTOS){
        List<PlankExchangeDailyDTO> dailys = Lists.newArrayList();
        for (OtherExcelDTO excelDTO:excelDTOS){
            CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
            circulateInfoQuery.setStockCode(excelDTO.getStock());
            List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
            CirculateInfo circulateInfo = circulateInfos.get(0);
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            try {
               /* if (!stockCode.equals("605299")) {
                    continue;
                }*/
                System.out.println(stockCode);
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo);
                if (CollectionUtils.isEmpty(stockKBars)) {
                    log.info("复盘数据 没有获取到k线数据 或者数据日期长度不够 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                List<KBarDTO> adjStockBars = getAdjStockBars(circulateInfo.getStockCode(), stockKBars);
                if (CollectionUtils.isEmpty(adjStockBars)) {
                    log.info("复盘数据 没有获取到复权k线数据 或者数据日期长度不够 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                PlankExchangeDailyDTO dto = findDays(adjStockBars, excelDTO, circulateInfo);
                dailys.add(dto);
                /*if(dailys.size()>=20){
                    break;
                }*/
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }
        }
        List<Object[]> datas = Lists.newArrayList();
        for(PlankExchangeDailyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getCirculateZ());
            list.add(dto.getBuyAmount());
            list.add(dto.getSellAmount());
            list.add(dto.getRealProfit());
            list.add(dto.getRealProfitRate());
            list.add(dto.getRealPlanks());
            list.add(dto.getKBarDTO().getDateStr());
            list.add(dto.getKBarDTO().getStartPrice());
            list.add(dto.getStartRate());
            list.add(dto.getStartExchangeMoney());
            list.add(dto.getBeforeTotalExchangeMoney());
            list.add(dto.getKBarDTO().getAdjStartPrice());
            list.add(dto.getRate3());
            list.add(dto.getRate5());
            list.add(dto.getRate10());
            list.add(dto.getPlankProfit());
            list.add(dto.getBeforePlanks5());
            list.add(dto.isHighThanOpen15());
            list.add(dto.getBuyPrice15());
            list.add(dto.getAvgRate15());
            list.add(dto.isHighThanOpen20());
            list.add(dto.getBuyPrice20());
            list.add(dto.getAvgRate20());
            list.add(dto.isHighThanOpen25());
            list.add(dto.getBuyPrice25());
            list.add(dto.getAvgRate25());
            list.add(dto.isHighThanOpen30());
            list.add(dto.getBuyPrice30());

            list.add(dto.getPreBlockLevel());
            list.add(dto.getPreBlockName());
            list.add(dto.getPreBlockRate());
            list.add(dto.getBlockLevel());
            list.add(dto.getBlockName());
            list.add(dto.getBlockRate());
            list.add(dto.getPreHighBlockLevel());
            list.add(dto.getPreHighBlockRate());

            list.add(dto.getAvgRate30());

            Object[] objects = list.toArray();
            datas.add(objects);

        }

        String[] rowNames = {"index","stockCode","stockName","流通z","买金额","卖金额","正盈利","盈亏比","连板情况","交易日期","开盘价格","开盘涨幅","开盘成交额","前一天总成交额","开盘时候价格","3日涨幅","5日涨幅","10日涨幅","次日收益","5日板数",
                "15分是否大于开盘价","15价格","15分溢价","20分是否大于开盘价","20价格","20分溢价","25分是否大于开盘价","25价格","25分溢价","30分是否大于开盘价","30价格",
                "昨日收盘最高板块排名","昨日收盘最高板块名称","昨日收盘最高板块幅度","开盘最高板块排名","开盘最高板块名称","开盘最高板块幅度","昨日收盘最高板块排名","昨日收盘最高板块幅度","30分溢价"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("中关村数据回撤",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("中关村数据回撤");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void getPreBlockLevel(String stockCode,PlankExchangeDailyDTO dto){
        KBarDTO preKbarDTO = dto.getPreKbarDTO();
        KBarDTO prePreKbarDTO = dto.getPreKbarDTO();
        if(preKbarDTO==null||prePreKbarDTO==null){
            return;
        }
        List<StockRateDTO> rates = Lists.newArrayList();
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        for (CirculateInfo circulateInfo:circulateInfos){
            String preUnique = circulateInfo.getStockCode()+"_"+DateUtil.format(preKbarDTO.getDate(),DateUtil.yyyyMMdd);
            String prePreUnique = circulateInfo.getStockCode()+"_"+DateUtil.format(prePreKbarDTO.getDate(),DateUtil.yyyyMMdd);
            StockKbar stockKbar = stockKbarService.getByUniqueKey(preUnique);
            StockKbar preStockKbar = stockKbarService.getByUniqueKey(prePreUnique);
            if(stockKbar==null||preStockKbar==null){
                continue;
            }
            StockRateDTO stockRateDTO = new StockRateDTO();
            stockRateDTO.setStockCode(dto.getStockCode());
            stockRateDTO.setStockName(dto.getStockName());
            BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
            stockRateDTO.setRate(rate);
            rates.add(stockRateDTO);
        }
        Map<String, BlockLevelDTO> stringBlockLevelDTOMap = blockLevelReplayComponent.calBlockLevelDTO(rates);
        BlockLevelDTO blockLevel = blockLevelReplayComponent.getBlockLevel(stringBlockLevelDTOMap, stockCode);
        if(blockLevel!=null&&blockLevel.getLevel()!=null){
            dto.setPreBlockName(blockLevel.getBlockName());
            dto.setPreBlockLevel(blockLevel.getLevel());
            dto.setPreBlockRate(blockLevel.getAvgRate());
            dto.setPreBlockCode(blockLevel.getBlockCode());
        }
    }

    public void getBlockLevel(String stockCode,PlankExchangeDailyDTO dto){
        KBarDTO kbarDTO = dto.getKBarDTO();
        KBarDTO preKbarDTO = dto.getPreKbarDTO();
        if(preKbarDTO==null||preKbarDTO==null){
            return;
        }
        List<StockRateDTO> rates = Lists.newArrayList();
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        for (CirculateInfo circulateInfo:circulateInfos){
            String uniqueKey = circulateInfo.getStockCode()+"_"+DateUtil.format(kbarDTO.getDate(),DateUtil.yyyyMMdd);
            String preUniqueKey = circulateInfo.getStockCode()+"_"+DateUtil.format(preKbarDTO.getDate(),DateUtil.yyyyMMdd);
            StockKbar uniqueKbar = stockKbarService.getByUniqueKey(uniqueKey);
            StockKbar preUniqueKbar = stockKbarService.getByUniqueKey(preUniqueKey);
            if(uniqueKbar==null||preUniqueKbar==null){
                continue;
            }
            StockRateDTO stockRateDTO = new StockRateDTO();
            stockRateDTO.setStockCode(dto.getStockCode());
            stockRateDTO.setStockName(dto.getStockName());
            BigDecimal rate = PriceUtil.getPricePercentRate(uniqueKbar.getOpenPrice().subtract(preUniqueKbar.getClosePrice()), preUniqueKbar.getClosePrice());
            stockRateDTO.setRate(rate);
            rates.add(stockRateDTO);

        }
        Map<String, BlockLevelDTO> stringBlockLevelDTOMap = blockLevelReplayComponent.calBlockLevelDTO(rates);
        BlockLevelDTO blockLevel = blockLevelReplayComponent.getBlockLevel(stringBlockLevelDTOMap, stockCode);
        BlockLevelDTO blockLevelDTOHigh = blockLevelReplayComponent.userBlockCodeBlockLevel(stringBlockLevelDTOMap, dto.getPreBlockCode());
        if(blockLevel!=null&&blockLevel.getLevel()!=null){
            dto.setBlockName(blockLevel.getBlockName());
            dto.setBlockLevel(blockLevel.getLevel());
            dto.setBlockRate(blockLevel.getAvgRate());
            dto.setBlockCode(blockLevel.getBlockCode());
            if(blockLevelDTOHigh!=null) {
                dto.setPreHighBlockLevel(blockLevelDTOHigh.getLevel());
                dto.setPreHighBlockRate(blockLevelDTOHigh.getAvgRate());
            }
        }
    }

    public void maxTotalExchange(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        boolean flag = false;
        long maxExchange= 0l;
        String maxExchangeDateStr = null;
        long totalExchange = 0l;
        int divideDays  = 0;
        BigDecimal rangeTotal = new BigDecimal(0);
        int i =0;
        KBarDTO preKbar = null;
        for (KBarDTO kbar:reverse){
            if(flag){
                i++;
            }
            if(i>1&&i<=201){
                if(preKbar!=null){
                    BigDecimal range = PriceUtil.getPricePercentRate(preKbar.getHighestPrice().subtract(preKbar.getLowestPrice()), kbar.getEndPrice());
                    rangeTotal  = rangeTotal.add(range);
                }
            }
            if(i>200){
                break;
            }
            if(i>0&&i<=200){
                totalExchange  =  totalExchange+kbar.getTotalExchange();
                if(kbar.getTotalExchange()>maxExchange){
                    maxExchange = kbar.getTotalExchange();
                    maxExchangeDateStr = kbar.getDateStr();
                    divideDays = i;
                }
            }
            if(kbar.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preKbar = kbar;
        }
        wuDiLianBanDTO.setMaxExchange(maxExchange);
        wuDiLianBanDTO.setMaxExchangeDateStr(maxExchangeDateStr);
        int days = i>=200?200:i;
        long avgExchange = totalExchange/days;
        BigDecimal rangeAvg = rangeTotal.divide(new BigDecimal(days), 2, BigDecimal.ROUND_HALF_UP);
        wuDiLianBanDTO.setAvgExchange(avgExchange);
        wuDiLianBanDTO.setSpaceDays(divideDays);
        wuDiLianBanDTO.setAvgRange(rangeAvg);
        return;
    }

    public void maxTotalExchange100(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        boolean flag = false;
        long maxExchange= 0l;
        String maxExchangeDateStr = null;
        long totalExchange = 0l;
        int divideDays  = 0;
        int i =0;
        BigDecimal rangeTotal = new BigDecimal(0);
        BigDecimal rangeTotalOpenClose = new BigDecimal(0);
        KBarDTO maxKbar = null;
        Integer maxDays = null;
        KBarDTO minKbar  = null;
        Integer minDays = null;
        KBarDTO preKbar = null;
        for (KBarDTO kbar:reverse){
            if(flag){
                i++;
            }
            if(i>102){
                break;
            }
            if(i>1&&i<=101){
                if(preKbar!=null){
                    BigDecimal range = PriceUtil.getPricePercentRate(preKbar.getAdjHighPrice().subtract(preKbar.getAdjLowPrice()), kbar.getAdjEndPrice());
                    rangeTotal  = rangeTotal.add(range);

                    BigDecimal rangeOpenAndClose = PriceUtil.getPricePercentRate(preKbar.getAdjEndPrice().subtract(preKbar.getAdjStartPrice()), kbar.getAdjEndPrice());
                    if(preKbar.getAdjStartPrice().compareTo(preKbar.getAdjEndPrice())==1){
                        rangeOpenAndClose = PriceUtil.getPricePercentRate(preKbar.getAdjStartPrice().subtract(preKbar.getAdjEndPrice()), kbar.getAdjEndPrice());
                    }
                    rangeTotalOpenClose  = rangeTotalOpenClose.add(rangeOpenAndClose);
                }
            }
            if(i>1 && i<=100){
                if(maxKbar==null || kbar.getAdjHighPrice().compareTo(maxKbar.getAdjHighPrice())==1){
                    maxKbar = kbar;
                    maxDays = i;
                }
                if(minKbar==null||kbar.getAdjLowPrice().compareTo(minKbar.getAdjLowPrice())==-1){
                    minKbar = kbar;
                    minDays = i;
                }
            }
            if(i>0&&i<=100){
                totalExchange  =  totalExchange+kbar.getTotalExchange();
                if(kbar.getTotalExchange()>maxExchange){
                    maxExchange = kbar.getTotalExchange();
                    maxExchangeDateStr = kbar.getDateStr();
                    divideDays = i;
                }
            }
            if(kbar.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preKbar = kbar;
        }
        wuDiLianBanDTO.setMaxExchange100(maxExchange);
        wuDiLianBanDTO.setMaxExchangeDateStr100(maxExchangeDateStr);
        int days = i>=100?100:i;
        long avgExchange = totalExchange/days;
        BigDecimal rangeAvg = rangeTotal.divide(new BigDecimal(days), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal rangeOpenCloseAvg = rangeTotalOpenClose.divide(new BigDecimal(days), 2, BigDecimal.ROUND_HALF_UP);

        wuDiLianBanDTO.setAvgExchange100(avgExchange);
        wuDiLianBanDTO.setSpaceDays100(divideDays);
        wuDiLianBanDTO.setAvgRange100(rangeAvg);
        wuDiLianBanDTO.setAvgRangeOpenClose100(rangeOpenCloseAvg);
        if(maxKbar!=null) {
            wuDiLianBanDTO.setMaxDayExchange(maxKbar.getTotalExchange());
            wuDiLianBanDTO.setMaxDayHighPrice(maxKbar.getAdjHighPrice());
            BigDecimal maxAvgPrice = historyTransactionDataComponent.calAvgPrice(wuDiLianBanDTO.getStockCode(), maxKbar.getDate());
            maxAvgPrice = chuQuanAvgPrice(maxAvgPrice, maxKbar, kbars);
            wuDiLianBanDTO.setMaxDayAvgPrice(maxAvgPrice);
        }else{
            wuDiLianBanDTO.setMaxDayExchange(null);
            wuDiLianBanDTO.setMaxDayHighPrice(null);
            wuDiLianBanDTO.setMaxDayAvgPrice(null);
        }
        wuDiLianBanDTO.setMaxDays(maxDays);
        if(minKbar!=null) {
            wuDiLianBanDTO.setMinDayExchange(minKbar.getTotalExchange());
            wuDiLianBanDTO.setMinDayLowPrice(minKbar.getAdjLowPrice());
            BigDecimal minAvgPrice = historyTransactionDataComponent.calAvgPrice(wuDiLianBanDTO.getStockCode(), minKbar.getDate());
            minAvgPrice = chuQuanAvgPrice(minAvgPrice, minKbar, kbars);
            wuDiLianBanDTO.setMinDayAvgPrice(minAvgPrice);
        }else{
            wuDiLianBanDTO.setMinDayExchange(null);
            wuDiLianBanDTO.setMinDayLowPrice(null);
            wuDiLianBanDTO.setMinDayAvgPrice(null);
        }
        wuDiLianBanDTO.setMinDays(minDays);
        return;
    }

    public void maxTotalExchange15(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        boolean flag = false;
        long maxExchange= 0l;
        String maxExchangeDateStr = null;
        long totalExchange = 0l;
        int divideDays  = 0;
        int i =0;
        BigDecimal rangeTotal = new BigDecimal(0);
        KBarDTO maxKbar = null;
        Integer maxDays = null;
        KBarDTO minKbar  = null;
        Integer minDays = null;
        KBarDTO preKbar = null;
        for (KBarDTO kbar:reverse){
            if(flag){
                i++;
            }
            if(i>16){
                break;
            }
            if(i>1&&i<=16){
                if(preKbar!=null){
                    BigDecimal range = PriceUtil.getPricePercentRate(preKbar.getAdjHighPrice().subtract(preKbar.getAdjLowPrice()), kbar.getAdjEndPrice());
                    rangeTotal  = rangeTotal.add(range);
                }
            }
            if(i>1 && i<=16){
                if(maxKbar==null || kbar.getAdjHighPrice().compareTo(maxKbar.getAdjHighPrice())==1){
                    maxKbar = kbar;
                    maxDays = i;
                }
                if(minKbar==null||kbar.getAdjLowPrice().compareTo(minKbar.getAdjLowPrice())==-1){
                    minKbar = kbar;
                    minDays = i;
                }
            }
            if(i>0&&i<=15){
                totalExchange  =  totalExchange+kbar.getTotalExchange();
                if(kbar.getTotalExchange()>maxExchange){
                    maxExchange = kbar.getTotalExchange();
                    maxExchangeDateStr = kbar.getDateStr();
                    divideDays = i;
                }
            }
            if(kbar.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preKbar = kbar;
        }
        wuDiLianBanDTO.setMaxExchange15(maxExchange);
        wuDiLianBanDTO.setMaxExchangeDateStr15(maxExchangeDateStr);
        int days = i>=15?15:i;
        long avgExchange = totalExchange/days;
        BigDecimal rangeAvg = rangeTotal.divide(new BigDecimal(days), 2, BigDecimal.ROUND_HALF_UP);

        wuDiLianBanDTO.setAvgExchange15(avgExchange);
        wuDiLianBanDTO.setSpaceDays15(divideDays);
        wuDiLianBanDTO.setAvgRange15(rangeAvg);
        if(maxKbar!=null) {
            wuDiLianBanDTO.setMaxDayExchange15(maxKbar.getTotalExchange());
            wuDiLianBanDTO.setMaxDayHighPrice15(maxKbar.getAdjHighPrice());
            BigDecimal maxAvgPrice = historyTransactionDataComponent.calAvgPrice(wuDiLianBanDTO.getStockCode(), maxKbar.getDate());
            maxAvgPrice = chuQuanAvgPrice(maxAvgPrice, maxKbar, kbars);
            wuDiLianBanDTO.setMaxDayAvgPrice15(maxAvgPrice);
        }else{
            wuDiLianBanDTO.setMaxDayExchange15(null);
            wuDiLianBanDTO.setMaxDayHighPrice15(null);
            wuDiLianBanDTO.setMaxDayAvgPrice15(null);
        }
        wuDiLianBanDTO.setMaxDays15(maxDays);
        if(minKbar!=null) {
            wuDiLianBanDTO.setMinDayExchange15(minKbar.getTotalExchange());
            wuDiLianBanDTO.setMinDayLowPrice15(minKbar.getAdjLowPrice());
            BigDecimal minAvgPrice = historyTransactionDataComponent.calAvgPrice(wuDiLianBanDTO.getStockCode(), minKbar.getDate());
            minAvgPrice = chuQuanAvgPrice(minAvgPrice, minKbar, kbars);
            wuDiLianBanDTO.setMinDayAvgPrice15(minAvgPrice);
        }else{
            wuDiLianBanDTO.setMinDayExchange15(null);
            wuDiLianBanDTO.setMinDayLowPrice15(null);
            wuDiLianBanDTO.setMinDayAvgPrice15(null);
        }
        wuDiLianBanDTO.setMinDays15(minDays);
        return;
    }

    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,KBarDTO kbar,List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        BigDecimal reason = null;
        for (KBarDTO kBarDTO:reverse){
            if(!(kBarDTO.getEndPrice().equals(kBarDTO.getAdjEndPrice()))&&!(kBarDTO.getStartPrice().equals(kBarDTO.getAdjStartPrice()))){
                reason = kBarDTO.getAdjStartPrice().divide(kBarDTO.getStartPrice(),4,BigDecimal.ROUND_HALF_UP);
                break;
            }
            if(kBarDTO.getDateStr().equals(kbar.getDateStr())){
                break;
            }
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }

    }


    public void beforeInfo10(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        boolean flag = false;
        int i =0;
        BigDecimal rateTotal = null;
        Long totalExchange = null;
        Integer times = null;
        KBarDTO preKbar = null;
        for (KBarDTO kbar:reverse){
            if(flag){
                i++;
            }
            if(i>13){
                break;
            }
            if(i==2){
                wuDiLianBanDTO.setBeforeFirstExchange(kbar.getTotalExchange());
            }
            if(i==3){
                BigDecimal rate = PriceUtil.getPricePercentRate(preKbar.getEndPrice().subtract(kbar.getEndPrice()), kbar.getEndPrice());
                wuDiLianBanDTO.setBeforeFirstRate(rate);
            }
            if(i>=2&&i<=11){
                if(kbar.getEndPrice().compareTo(kbar.getStartPrice())==1){
                    if(times==null){
                        times = 1;
                    }else{
                        times = times+1;
                    }

                    if(totalExchange==null){
                        totalExchange = kbar.getTotalExchange();
                    }else{
                        totalExchange = totalExchange+kbar.getTotalExchange();
                    }
                }
            }

            if(i>=3&&i<=12){
                if(preKbar.getEndPrice().compareTo(preKbar.getStartPrice())==1){
                    BigDecimal rate = PriceUtil.getPricePercentRate(preKbar.getEndPrice().subtract(preKbar.getStartPrice()), kbar.getEndPrice());
                    if(rateTotal==null){
                        rateTotal = rate;
                    }else{
                        rateTotal = rateTotal.add(rate);
                    }
                }
            }
            if(kbar.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preKbar = kbar;
        }
        if(times!=null){
            wuDiLianBanDTO.setSunTimes10(times);
            wuDiLianBanDTO.setSunTotalRate10(rateTotal);
            wuDiLianBanDTO.setSunExchange10(totalExchange);
            BigDecimal avg = rateTotal.divide(new BigDecimal(times), 2, BigDecimal.ROUND_HALF_UP);
            wuDiLianBanDTO.setSunAvgRate10(avg);
        }
        return;
    }

    public void avgPrice(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        BigDecimal preEndPrice = null;
        boolean flag = false;
        int i =0;
        for (KBarDTO kbar:kbars){
            if(flag){
                i++;
            }
            if(preEndPrice!=null){
                boolean lowestPlank = PriceUtil.isUpperPrice(kbar.getLowestPrice(),preEndPrice);
                if (MarketUtil.isChuangYe(wuDiLianBanDTO.getStockCode()) && !kbar.getDate().before(DateUtil.parseDate("2020-08-24", DateUtil.yyyy_MM_dd))) {
                    lowestPlank = PriceUtil.isUpperPrice(wuDiLianBanDTO.getStockCode(), kbar.getLowestPrice(),preEndPrice);
                }
                boolean highSudden = PriceUtil.isSuddenPrice(kbar.getHighestPrice(),preEndPrice);
                if (MarketUtil.isChuangYe(wuDiLianBanDTO.getStockCode()) && !kbar.getDate().before(DateUtil.parseDate("2020-08-24", DateUtil.yyyy_MM_dd))) {
                    highSudden = PriceUtil.isSuddenPrice(wuDiLianBanDTO.getStockCode(),kbar.getHighestPrice(),preEndPrice);
                }
                if(i>0){
                    if(!highSudden && !lowestPlank){
                        BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(wuDiLianBanDTO.getStockCode(), kbar.getDate());
                        if(avgPrice!=null) {
                            BigDecimal startProfit = PriceUtil.getPricePercentRate(avgPrice.subtract(wuDiLianBanDTO.getKBarDTO().getAdjStartPrice()), wuDiLianBanDTO.getKBarDTO().getAdjStartPrice());
                            wuDiLianBanDTO.setPlankProfit(startProfit);
                            if(wuDiLianBanDTO.getBuyPrice15()!=null){
                                BigDecimal avgRate15 = PriceUtil.getPricePercentRate(avgPrice.subtract(wuDiLianBanDTO.getBuyPrice15()), wuDiLianBanDTO.getBuyPrice15());
                                wuDiLianBanDTO.setAvgRate15(avgRate15);
                            }
                            if(wuDiLianBanDTO.getBuyPrice20()!=null){
                                BigDecimal avgRate20 = PriceUtil.getPricePercentRate(avgPrice.subtract(wuDiLianBanDTO.getBuyPrice20()), wuDiLianBanDTO.getBuyPrice20());
                                wuDiLianBanDTO.setAvgRate20(avgRate20);
                            }
                            if(wuDiLianBanDTO.getBuyPrice25()!=null){
                                BigDecimal avgRate25 = PriceUtil.getPricePercentRate(avgPrice.subtract(wuDiLianBanDTO.getBuyPrice25()), wuDiLianBanDTO.getBuyPrice25());
                                wuDiLianBanDTO.setAvgRate25(avgRate25);
                            }
                            if(wuDiLianBanDTO.getBuyPrice30()!=null){
                                BigDecimal avgRate30 = PriceUtil.getPricePercentRate(avgPrice.subtract(wuDiLianBanDTO.getBuyPrice30()), wuDiLianBanDTO.getBuyPrice30());
                                wuDiLianBanDTO.setAvgRate30(avgRate30);
                            }
                        }
                        return;
                    }
                }
            }

            if(kbar.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preEndPrice = kbar.getEndPrice();
        }
    }


    public PlankExchangeDailyDTO findDays(List<KBarDTO> kbars, OtherExcelDTO excelDTO,CirculateInfo circulateInfo){
        PlankExchangeDailyDTO dto = new PlankExchangeDailyDTO();
        KBarDTO preKbarDTO  = null;
        KBarDTO prePreKbarDTO = null;
        for (KBarDTO kbar:kbars){
            if(kbar.getDateStr().equals(excelDTO.getTradeDate())){
                dto.setStockCode(circulateInfo.getStockCode());
                dto.setStockName(circulateInfo.getStockName());
                dto.setCirculateZ(circulateInfo.getCirculateZ());
                dto.setBuyAmount(excelDTO.getBuyAmount());
                dto.setSellAmount(excelDTO.getSellAmount());
                dto.setRealProfit(excelDTO.getRealProfit());
                dto.setRealProfitRate(excelDTO.getRealProfitRate());
                dto.setRealPlanks(excelDTO.getRealPlanks());
                dto.setKBarDTO(kbar);
                dto.setPreKbarDTO(preKbarDTO);
                dto.setPrePreKbarDTO(prePreKbarDTO);
                dto.setBeforeTotalExchangeMoney(preKbarDTO.getTotalExchangeMoney());
                BigDecimal startRate = PriceUtil.getPricePercentRate(kbar.getAdjStartPrice().subtract(preKbarDTO.getAdjEndPrice()), preKbarDTO.getAdjEndPrice());
                dto.setStartRate(startRate);
                calGatherExchange(kbar,dto);
                beforeRate(dto,kbars);
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(circulateInfo.getStockCode(), kbar.getDate());
                buyTimeRate(datas,dto);
                avgPrice(dto,kbars);
                if(preKbarDTO!=null) {
                    getPreBlockLevel(dto.getStockCode(), dto);
                    getBlockLevel(dto.getStockCode(), dto);
                }
            }
            prePreKbarDTO = preKbarDTO;
            preKbarDTO = kbar;
        }
        return dto;
    }

    public void buyTimeRate(List<ThirdSecondTransactionDataDTO> datas, PlankExchangeDailyDTO dto){
        if(CollectionUtils.isEmpty(datas)){
            return;
        }

        for (ThirdSecondTransactionDataDTO data:datas){
            String tradeTime = data.getTradeTime();
            if(tradeTime.startsWith("09:45") && dto.getBuyPrice15()==null){
                if(dto.getKBarDTO().getStartPrice().compareTo(data.getTradePrice())==-1) {
                    dto.setHighThanOpen15(true);
                }
                dto.setBuyPrice15(data.getTradePrice());
            }
            if(tradeTime.startsWith("09:50") && dto.getBuyPrice20()==null){
                if(dto.getKBarDTO().getStartPrice().compareTo(data.getTradePrice())==-1) {
                    dto.setHighThanOpen20(true);
                }
                dto.setBuyPrice20(data.getTradePrice());
            }
            if(tradeTime.startsWith("09:55") && dto.getBuyPrice25()==null){
                if(dto.getKBarDTO().getStartPrice().compareTo(data.getTradePrice())==-1) {
                    dto.setHighThanOpen25(true);
                }
                dto.setBuyPrice25(data.getTradePrice());
            }
            if(tradeTime.startsWith("10:00") && dto.getBuyPrice30()==null){
                if(dto.getKBarDTO().getStartPrice().compareTo(data.getTradePrice())==-1) {
                    dto.setHighThanOpen30(true);
                }
                dto.setBuyPrice30(data.getTradePrice());
            }

        }
    }

    public void calGatherExchange(KBarDTO kbar,PlankExchangeDailyDTO dto){
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData(dto.getStockCode(), kbar.getDate());
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        ThirdSecondTransactionDataDTO thirdSecondTransactionDataDTO = data.get(0);
        BigDecimal exchangeMoney = new BigDecimal(thirdSecondTransactionDataDTO.getTradeQuantity()).multiply(thirdSecondTransactionDataDTO.getTradePrice().multiply(new BigDecimal(100)));
        dto.setStartExchangeMoney(exchangeMoney);
    }

    public void beforeRate(PlankExchangeDailyDTO wuDiLianBanDTO, List<KBarDTO> kbars){
        List<KBarDTO> reverse = Lists.reverse(kbars);
        int i = 0;
        boolean flag = false;
        BigDecimal endPrice = null;
        KBarDTO preKbar  = null;
        int planks = 0;
        for (KBarDTO kBarDTO:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endPrice = kBarDTO.getAdjEndPrice();
            }
            if(i>=2&&i<=6){
                boolean upperPrice = PriceUtil.isUpperPrice(wuDiLianBanDTO.getStockCode(), preKbar.getEndPrice(), kBarDTO.getEndPrice());
                if(upperPrice){
                    planks++;
                }
                wuDiLianBanDTO.setBeforePlanks5(planks);
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getAdjEndPrice()), kBarDTO.getAdjEndPrice());
                wuDiLianBanDTO.setRate3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getAdjEndPrice()), kBarDTO.getAdjEndPrice());
                wuDiLianBanDTO.setRate5(rate);
            }

            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endPrice.subtract(kBarDTO.getAdjEndPrice()), kBarDTO.getAdjEndPrice());
                wuDiLianBanDTO.setRate10(rate);
            }
            if(i>12){
                return;
            }
            if(kBarDTO.getDateStr().equals(wuDiLianBanDTO.getKBarDTO().getDateStr())){
                flag = true;
            }
            preKbar = kBarDTO;
        }
    }

    public void isFitExchange(List<ThirdSecondTransactionDataDTO> data,PlankExchangeDailyDTO plankExchangeDailyDTO){
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        Integer beforeBuyQuantity = 0;
        boolean canBuy  = false;
        for (ThirdSecondTransactionDataDTO dto:data){
            String tradeTime = dto.getTradeTime();
            BigDecimal tradePrice = dto.getTradePrice();
            Integer tradeQuantity = dto.getTradeQuantity();
            Integer tradeType = dto.getTradeType();
            boolean isUpperPrice = PriceUtil.isUpperPrice(tradePrice, plankExchangeDailyDTO.getPreEndPrice());
            if(MarketUtil.isChuangYe(plankExchangeDailyDTO.getStockCode())&&!plankExchangeDailyDTO.getKBarDTO().getDate().before(DateUtil.parseDate("2020-08-24",DateUtil.yyyy_MM_dd))){
                isUpperPrice = PriceUtil.isUpperPrice(plankExchangeDailyDTO.getStockCode(),tradePrice,plankExchangeDailyDTO.getPreEndPrice());
            }
            boolean isSell = false;
            if(tradeType==null || tradeType!=0){
                isSell = true;
            }
            boolean isPlank = false;
            if(isSell&&isUpperPrice){
                isPlank = true;
            }
            if(!isPlank){
                canBuy = true;
            }
            if(canBuy&&isPlank){
                plankExchangeDailyDTO.setInsertTime(tradeTime);
                plankExchangeDailyDTO.setBeforePlankQuantity(beforeBuyQuantity);
                return ;
            }
            beforeBuyQuantity = beforeBuyQuantity+tradeQuantity;
        }
    }
    //判断是不是二板
    public boolean isTwoPlank(List<KBarDTO> list,CirculateInfo circulateInfo){
        if(list.size()<3){
            return false;
        }
        List<KBarDTO> reverse = Lists.reverse(list);
        BigDecimal preEndPrice = null;
        BigDecimal preHighPrice = null;
        int i=0;
        for (KBarDTO kBarDTO:reverse){
            i++;
            if(preEndPrice!=null) {
                BigDecimal endPrice = kBarDTO.getEndPrice();
                boolean endPlank = PriceUtil.isUpperPrice(preEndPrice,endPrice);
                if(MarketUtil.isChuangYe(circulateInfo.getStockCode())&&!kBarDTO.getDate().before(DateUtil.parseDate("2020-08-23",DateUtil.yyyy_MM_dd))){
                    endPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(),preEndPrice,endPrice);
                }
                boolean highPlank = PriceUtil.isUpperPrice(preHighPrice,endPrice);
                if(MarketUtil.isChuangYe(circulateInfo.getStockCode())&&!kBarDTO.getDate().before(DateUtil.parseDate("2020-08-23",DateUtil.yyyy_MM_dd))){
                    highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(),preHighPrice,endPrice);
                }
                if(i==2){
                    if(!highPlank){
                        return false;
                    }
                }
                if(i==3){
                    if(!endPlank){
                        return false;
                    }
                }
                if(i==4){
                    if(endPlank){
                        return false;
                    }
                }
                if(i==5){
                    if(endPlank){
                        return false;
                    }
                }
            }
            preEndPrice = kBarDTO.getEndPrice();
            preHighPrice = kBarDTO.getHighestPrice();
        }
        return true;
    }

    public void highExchangeDate(List<KBarDTO> kbars, PlankExchangeDailyDTO plankExchangeDailyDTO){
        Long exchangeMoney = null;
        String dateStr = null;
        for (KBarDTO kbar :kbars){
            if (exchangeMoney == null || exchangeMoney < kbar.getTotalExchangeMoney()) {
                exchangeMoney = kbar.getTotalExchangeMoney();
                dateStr = kbar.getDateStr();
            }
        }
        BigDecimal maxExchangeMoney = new BigDecimal(exchangeMoney);
        plankExchangeDailyDTO.setMaxExchangeMoney(maxExchangeMoney);
        plankExchangeDailyDTO.setMaxExchangeMoneyDate(dateStr);
        return;
    }


    public List<KBarDTO> getStockKBars(CirculateInfo circulateInfo){
        try {
            List<KBarDTO> listNew = Lists.newArrayList();
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 100);
            List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
            List<KBarDTO> list = deleteNewStockTimes(kbars, 100, circulateInfo.getStockCode());
            for (KBarDTO dto:list){
                if(dto==null){
                    continue;
                }
                listNew.add(dto);
            }
            return listNew;
        }catch (Exception e){
            return null;
        }
    }

    public List<KBarDTO> getAdjStockBars(String stockCode,List<KBarDTO> kBarDTOS){
        if(stockCode.equals("300996")){
            System.out.println("1111");
        }
        KBarDTO startKBarDTO = kBarDTOS.get(0);
        KBarDTO endKbarDTO = kBarDTOS.get(kBarDTOS.size()-1);
        String startDateStr = DateUtil.format(startKBarDTO.getDate(), DateUtil.yyyyMMdd);
        String endDateStr = DateUtil.format(endKbarDTO.getDate(), DateUtil.yyyyMMdd);
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode(stockCode);
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        List<KBarDTO> list = Lists.newArrayList();
        if(CollectionUtils.isEmpty(stockKbars)){
            return list;
        }
        boolean flag = false;
        for (StockKbar kbar:stockKbars){
            if(kbar.getKbarDate().equals(startDateStr)){
                flag = true;
            }
            if(flag){
                KBarDTO kBarDTO = new KBarDTO();
                kBarDTO.setStartPrice(kbar.getOpenPrice());
                kBarDTO.setEndPrice(kbar.getClosePrice());
                kBarDTO.setHighestPrice(kbar.getHighPrice());
                kBarDTO.setLowestPrice(kbar.getLowPrice());
                kBarDTO.setAdjStartPrice(kbar.getAdjOpenPrice());
                kBarDTO.setAdjEndPrice(kbar.getAdjClosePrice());
                kBarDTO.setAdjHighPrice(kbar.getAdjHighPrice());
                kBarDTO.setAdjLowPrice(kbar.getAdjLowPrice());
                kBarDTO.setDate(DateUtil.parseDate(kbar.getKbarDate(),DateUtil.yyyyMMdd));
                kBarDTO.setDateStr(DateUtil.format(kBarDTO.getDate(),DateUtil.yyyy_MM_dd));
                kBarDTO.setTotalExchange(kbar.getTradeQuantity());
                kBarDTO.setTotalExchangeMoney(kbar.getTradeAmount().longValue());
                list.add(kBarDTO);
            }
        }
        return list;
    }


    //包括新股最后一个一字板
    public List<KBarDTO> deleteNewStockTimes(List<KBarDTO> list,int size,String stockCode){
        List<KBarDTO> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        KBarDTO first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (KBarDTO dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighestPrice().equals(dto.getLowestPrice()))){
                        i++;
                        datas.add(first);
                    }
                    first = dto;
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getEndPrice();
            }
        }else{
            return list;
        }
        return datas;
    }



}
