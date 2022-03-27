package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.Index500NDayDTO;
import com.bazinga.dto.IndexRate500DTO;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.IndexDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.IndexDetailService;
import com.bazinga.replay.service.RedisMoniorService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.StockKbarUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Index500Component {

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private RedisMoniorService redisMoniorService;

    @Autowired
    private IndexDetailService indexDetailService;

    private List<String> kbarNodes = Lists.newArrayList(
            "20171031" ,
            "20171130" ,
            "20171229" ,
            "20180131" ,
            "20180228" ,
            "20180330" ,
            "20180427" ,
            "20180531" ,
            "20180629" ,
            "20180731" ,
            "20180831" ,
            "20180928" ,
            "20181031" ,
            "20181130" ,
            "20181228" ,
            "20190131" ,
            "20190228" ,
            "20190329" ,
            "20190430" ,
            "20190531" ,
            "20190628" ,
            "20190731" ,
            "20190830" ,
            "20190930" ,
            "20191031" ,
            "20191129" ,
            "20191231" ,
            "20200123" ,
            "20200228" ,
            "20200331" ,
            "20200430" ,
            "20200529" ,
            "20200630" ,
            "20200731" ,
            "20200831" ,
            "20200930" ,
            "20201030" ,
            "20201130" ,
            "20201231" ,
            "20210129" ,
            "20210226" ,
            "20210331" ,
            "20210430" ,
            "20210531" ,
            "20210630" ,
            "20210730" ,
            "20210930" ,
            "20211029" ,
            "20211130" ,
            "20211231" ,
            "20220128" ,
            "20220228");

    public  Map<String, IndexRate500DTO> getIndex500RateMap(){
        Map<String, IndexRate500DTO> resultMap = new HashMap<>();

        String key = "zz_500_index_rate";
        RedisMonior redisMonior = redisMoniorService.getByRedisKey(key);
        if(redisMonior !=null){
            JSONObject jsonObject = JSONObject.parseObject(redisMonior.getRedisValue());
            jsonObject.forEach((jsonKey,value)->{
                resultMap.put(jsonKey,JSONObject.parseObject(value.toString(), IndexRate500DTO.class));
            });
        }else {
            String[] headList = getHeadList();
            TradeDatePoolQuery tradeDateQuery = new TradeDatePoolQuery();
            tradeDateQuery.setTradeDateFrom(DateUtil.parseDate("20171101", DateUtil.yyyyMMdd));
            tradeDateQuery.setTradeDateTo(DateUtil.parseDate("20220318", DateUtil.yyyyMMdd));
            tradeDateQuery.addOrderBy("trade_date", Sort.SortType.ASC);
            List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDateQuery);
            BigDecimal closePrice = BigDecimal.ZERO;
            for (TradeDatePool tradeDatePool : tradeDatePools) {

                String kbarDate = DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyyMMdd);
                List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getIndexData("000905", kbarDate);
                if(CollectionUtils.isEmpty(list)){
                    log.info("");
                }
                if(closePrice.compareTo(BigDecimal.ZERO)==0){
                    closePrice = list.get(list.size()-1).getTradePrice();
                    continue;
                }
                BigDecimal openPrice = list.get(0).getTradePrice();
                BigDecimal openRate = PriceUtil.getPricePercentRate(openPrice.subtract(closePrice),closePrice);
                BigDecimal lowPrice = new BigDecimal("1000000");
                BigDecimal highPrice = new BigDecimal("-10");

                Map<String, IndexRate500DTO> tempMap = new HashMap<>();
                Integer overOpenCount = 0;
                BigDecimal min5TradeAmount = BigDecimal.ZERO;
                List<ThirdSecondTransactionDataDTO> list0935 = historyTransactionDataComponent.getFixTimeData(list, "09:35");
                List<ThirdSecondTransactionDataDTO> list0940 = historyTransactionDataComponent.getFixTimeData(list, "09:40");
                List<ThirdSecondTransactionDataDTO> list0950 = historyTransactionDataComponent.getFixTimeData(list, "09:50");
                ThirdSecondTransactionDataDTO open = list.get(0);
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list0935) {
                    min5TradeAmount = min5TradeAmount.add(new BigDecimal(transactionDataDTO.getTradeQuantity().toString()));
                    if(transactionDataDTO.getTradePrice().compareTo(open.getTradePrice())>0){
                        overOpenCount++;
                    }
                }

                Integer overOpen10 = 0;
                BigDecimal min10TradeAmount = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list0940) {
                    min10TradeAmount = min10TradeAmount.add(new BigDecimal(transactionDataDTO.getTradeQuantity().toString()));
                    if(transactionDataDTO.getTradePrice().compareTo(open.getTradePrice())>0){
                        overOpen10++;
                    }
                }

                Integer overOpen20 = 0;
                BigDecimal min20TradeAmount = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO transactionDataDTO : list0950) {
                    min20TradeAmount = min20TradeAmount.add(new BigDecimal(transactionDataDTO.getTradeQuantity().toString()));
                    if(transactionDataDTO.getTradePrice().compareTo(open.getTradePrice())>0){
                        overOpen20++;
                    }
                }

                for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
                    if(highPrice.compareTo(transactionDataDTO.getTradePrice())<0){
                        highPrice = transactionDataDTO.getTradePrice();
                    }
                    if(lowPrice.compareTo(transactionDataDTO.getTradePrice())>0){
                        lowPrice = transactionDataDTO.getTradePrice();
                    }
                    BigDecimal lowRate = PriceUtil.getPricePercentRate(lowPrice.subtract(closePrice),closePrice);
                    BigDecimal highRate = PriceUtil.getPricePercentRate(highPrice.subtract(closePrice),closePrice);
                    BigDecimal buyRate = PriceUtil.getPricePercentRate(transactionDataDTO.getTradePrice().subtract(closePrice),closePrice);
                    tempMap.put(transactionDataDTO.getTradeTime(),new IndexRate500DTO(openRate,lowRate,highRate,buyRate,overOpenCount,overOpen10,overOpen20,min5TradeAmount,min10TradeAmount));
                }
                for (int i = 0; i < headList.length-1; i++) {
                    IndexRate500DTO indexRate500DTO = tempMap.get(headList[i]);
                    resultMap.put(kbarDate + headList[i+1],indexRate500DTO);

                }
                closePrice = list.get(list.size()-1).getTradePrice();
            }
            RedisMonior monior = new RedisMonior();
            monior.setRedisKey(key);
            monior.setRedisValue(JSONObject.toJSONString(resultMap));
            redisMoniorService.save(monior);

        }


        return resultMap;

    }

    private  String[] getHeadList(){
        List<String> headList = Lists.newArrayList();
        headList.add("09:25");
        Date date = DateUtil.parseDate("20210818092900", DateUtil.yyyyMMddHHmmss);
        int count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
            /*for (int i = 1; i < 21; i++) {
                headList.add(DateUtil.format(date,"HH:mm") + SymbolConstants.UNDERLINE +i);
            }*/
        }
        headList.add("13:00");
        date = DateUtil.parseDate("20210531130000", DateUtil.yyyyMMddHHmmss);
        count = 0;
        while (count< 120){
            date = DateUtil.addMinutes(date, 1);
            count++;
            headList.add(DateUtil.format(date,"HH:mm"));
        }
        return headList.toArray(new String[]{});
    }


    public Map<String, Index500NDayDTO> getNdayRateMap(int days){
        List<StockKbar> resultList = Lists.newArrayList();
        Map<String,Index500NDayDTO> resultMap = new HashMap<>(2048);
        for(int i=0;i<days;i++) {
            DataTable dataTable = TdxHqUtil.getIndexSecurityBars(KCate.DAY, "000905", i, 1);
            List<StockKbar> stockKbarList = StockKbarConvert.convertSpecial(dataTable, "000905", "中证500");
            if (CollectionUtils.isEmpty(stockKbarList)) {
                return null;
            }
            for (StockKbar stockKbar : stockKbarList) {
                stockKbar.setAdjClosePrice(stockKbar.getClosePrice());
                stockKbar.setAdjOpenPrice(stockKbar.getOpenPrice());
                stockKbar.setAdjHighPrice(stockKbar.getHighPrice());
                stockKbar.setAdjLowPrice(stockKbar.getLowPrice());
                resultList.add(stockKbar);
            }
        }
        resultList = Lists.reverse(resultList);
        for (int i = 16; i < resultList.size(); i++) {
            StockKbar stockKbar = resultList.get(i);
            BigDecimal day5Rate = StockKbarUtil.getNDaysUpperRate(resultList.subList(i - 16, i), 5);
            BigDecimal day10Rate = StockKbarUtil.getNDaysUpperRate(resultList.subList(i - 16, i), 10);
            BigDecimal day15Rate = StockKbarUtil.getNDaysUpperRate(resultList.subList(i - 16, i), 15);
            resultMap.put(stockKbar.getKbarDate(),new Index500NDayDTO(stockKbar.getTradeAmount(),day5Rate,day10Rate,day15Rate));
        }

        return resultMap;
    }

    public Map<String,List<String>> getNodeList(){
        Map<String,List<String>> resultMap = new HashMap<>();
        for (String kbarNode : kbarNodes) {
            IndexDetailQuery query= new IndexDetailQuery();
            query.setKbarDate(kbarNode);
            List<IndexDetail> indexDetails = indexDetailService.listByCondition(query);
            resultMap.put(kbarNode,indexDetails.stream().map(IndexDetail::getStockCode).collect(Collectors.toList()));
        }
        return resultMap;
    }

    public String getRecentNode(String kbarDate){
        for (int i = 0; i < kbarNodes.size()-1; i++) {
            String node = kbarNodes.get(i);
            String afterNode = kbarNodes.get(i+1);
            if(Integer.parseInt(kbarDate) >=Integer.parseInt(node) && Integer.parseInt(kbarDate) <Integer.parseInt(afterNode)){
                return node;
            }

        }
        return kbarNodes.get(kbarNodes.size()-1);
    }
}
