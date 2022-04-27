package com.bazinga.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.dto.HuShen300ExcelDTO;
import com.bazinga.replay.dto.TransferableBondInfoExcelDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class JiHeWuDiComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private RedisMoniorService redisMoniorService;

    public static final ExecutorService THREAD_POOL_QUOTE_JIHE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void hs300Info() {
        File file = new File("D:/circulate/hs300.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/hs300.xlsx" + "不存在");
        }
        ArrayList<CirculateInfo> list = Lists.newArrayList();
        try {
            List<HuShen300ExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(HuShen300ExcelDTO.class);
            dataList.forEach(item -> {
                CirculateInfo circulateInfo = new CirculateInfo();
                circulateInfo.setStockCode(item.getStockCode());
                circulateInfo.setStockName(item.getStockName());
                list.add(circulateInfo);
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
        jiHeTest(list);
        //exportJiHeInfo(list);
    }


    public void exportJiHeInfo(List<CirculateInfo> circulateInfos){
        Map<String, JiHeWudiTotalDTO> map = getJiHeInfo(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for(String kbarDate:map.keySet()){
            JiHeWudiTotalDTO dto = map.get(kbarDate);
            BigDecimal avgRate915 = dto.getRate915().divide(new BigDecimal(dto.getRateCount915()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal avgRate920 = dto.getRate920().divide(new BigDecimal(dto.getRateCount920()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal avgRate924 = dto.getRate924().divide(new BigDecimal(dto.getRateCount924()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal avgRate925 = dto.getRate925().divide(new BigDecimal(dto.getRateCount925()), 2, BigDecimal.ROUND_HALF_UP);
            List<Object> list = new ArrayList<>();
            list.add(kbarDate);
            list.add(kbarDate);
            list.add(avgRate915);
            list.add(dto.getAmount915());
            list.add(dto.getBuyTwoAmount915());
            list.add(dto.getBuyTwoCount915());

            list.add(avgRate920);
            list.add(dto.getAmount920());
            list.add(dto.getBuyTwoAmount920());
            list.add(dto.getBuyTwoCount920());

            list.add(avgRate924);
            list.add(dto.getAmount924());
            list.add(dto.getBuyTwoAmount924());
            list.add(dto.getBuyTwoCount924());

            list.add(avgRate925);
            list.add(dto.getAmount925());
            list.add(dto.getBuyTwoAmount925());
            list.add(dto.getBuyTwoCount925());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","日期","915平均涨幅","915总成交额","915买二总成交额","915有买二的数量",
                "920平均涨幅","920总成交额","920买二总成交额","920有买二的数量",
                "924平均涨幅","924总成交额","924买二总成交额","924有买二的数量",
                "925平均涨幅","925总成交额","925买二总成交额","925有买二的数量"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("集合相关信息",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("集合相关信息");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public Map<String,JiHeWudiTotalDTO> getJiHeInfo(List<CirculateInfo> circulateInfos){
        Map<String, JiHeWudiTotalDTO> map = new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setStockCode(circulateInfo.getStockCode());
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            for (StockKbar stockKbar : stockKbars) {
                Date dateyyyyMMdd = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if (dateyyyyMMdd.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))) {
                    continue;
                }

                if (!dateyyyyMMdd.before(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))) {
                    continue;
                }
                String stockCode = stockKbar.getStockCode()+"_"+stockKbar.getKbarDate();
                RedisMonior redisMonior = redisMoniorService.getByRedisKey(stockCode);
                if(redisMonior==null||StringUtils.isBlank(redisMonior.getRedisValue())){
                    continue;
                }
                String redisValue = redisMonior.getRedisValue();
                JiHeWudiDTO jiHeWudiDTO = JSONObject.parseObject(redisValue, JiHeWudiDTO.class);
                JiHeWudiTotalDTO jiHe = map.get(stockKbar.getKbarDate());
                if(jiHe==null){
                    jiHe = new JiHeWudiTotalDTO();
                    map.put(stockKbar.getKbarDate(),jiHe);
                }
                if(jiHeWudiDTO.getRate915()!=null){
                    BigDecimal rate915 = jiHe.getRate915().add(jiHeWudiDTO.getRate915());
                    int rateCount915 = jiHe.getRateCount915()+1;
                    BigDecimal amount915 = jiHe.getAmount915().add(jiHeWudiDTO.getAmount915());
                    int buyTwoCount915 = jiHe.getBuyTwoCount915();
                    BigDecimal buyTwoAmount915 = jiHe.getBuyTwoAmount915();
                    if(jiHeWudiDTO.getBuyTwoAmount915()!=null){
                        buyTwoAmount915 = jiHe.getBuyTwoAmount915().add(jiHeWudiDTO.getBuyTwoAmount915());
                        buyTwoCount915 = buyTwoCount915+1;
                    }
                    jiHe.setRate915(rate915);
                    jiHe.setRateCount915(rateCount915);
                    jiHe.setAmount915(amount915);
                    jiHe.setBuyTwoAmount915(buyTwoAmount915);
                    jiHe.setBuyTwoCount915(buyTwoCount915);
                }

                if(jiHeWudiDTO.getRate920()!=null){
                    BigDecimal rate920 = jiHe.getRate920().add(jiHeWudiDTO.getRate920());
                    int rateCount920 = jiHe.getRateCount920()+1;
                    BigDecimal amount920 = jiHe.getAmount920().add(jiHeWudiDTO.getAmount920());
                    int buyTwoCount920 = jiHe.getBuyTwoCount920();
                    BigDecimal buyTwoAmount920 = jiHe.getBuyTwoAmount920();
                    if(jiHeWudiDTO.getBuyTwoAmount920()!=null){
                        buyTwoAmount920 = jiHe.getBuyTwoAmount920().add(jiHeWudiDTO.getBuyTwoAmount920());
                        buyTwoCount920 = buyTwoCount920+1;
                    }
                    jiHe.setRate920(rate920);
                    jiHe.setRateCount920(rateCount920);
                    jiHe.setAmount920(amount920);
                    jiHe.setBuyTwoAmount920(buyTwoAmount920);
                    jiHe.setBuyTwoCount920(buyTwoCount920);
                }

                if(jiHeWudiDTO.getRate924()!=null){
                    BigDecimal rate924 = jiHe.getRate924().add(jiHeWudiDTO.getRate924());
                    int rateCount924 = jiHe.getRateCount924()+1;
                    BigDecimal amount924 = jiHe.getAmount924().add(jiHeWudiDTO.getAmount924());
                    int buyTwoCount924 = jiHe.getBuyTwoCount924();
                    BigDecimal buyTwoAmount924 = jiHe.getBuyTwoAmount924();
                    if(jiHeWudiDTO.getBuyTwoAmount924()!=null){
                        buyTwoAmount924 = jiHe.getBuyTwoAmount924().add(jiHeWudiDTO.getBuyTwoAmount924());
                        buyTwoCount924 = buyTwoCount924+1;
                    }
                    jiHe.setRate924(rate924);
                    jiHe.setRateCount924(rateCount924);
                    jiHe.setAmount924(amount924);
                    jiHe.setBuyTwoAmount924(buyTwoAmount924);
                    jiHe.setBuyTwoCount924(buyTwoCount924);
                }

                if(jiHeWudiDTO.getRate925()!=null){
                    BigDecimal rate925 = jiHe.getRate925().add(jiHeWudiDTO.getRate925());
                    int rateCount925 = jiHe.getRateCount925()+1;
                    BigDecimal amount925 = jiHe.getAmount925().add(jiHeWudiDTO.getAmount925());
                    int buyTwoCount925 = jiHe.getBuyTwoCount925();
                    BigDecimal buyTwoAmount925 = jiHe.getBuyTwoAmount925();
                    if(jiHeWudiDTO.getBuyTwoAmount925()!=null){
                        buyTwoAmount925 = jiHe.getBuyTwoAmount925().add(jiHeWudiDTO.getBuyTwoAmount925());
                        buyTwoCount925 = buyTwoCount925+1;
                    }
                    jiHe.setRate925(rate925);
                    jiHe.setRateCount925(rateCount925);
                    jiHe.setAmount925(amount925);
                    jiHe.setBuyTwoAmount925(buyTwoAmount925);
                    jiHe.setBuyTwoCount925(buyTwoCount925);
                }
            }
        }
        return map;
    }


    public void jiHeTest(List<CirculateInfo> circulateInfos) {
        int index = 0;
        int ret = thsLogin();
        for( CirculateInfo circulateInfo:circulateInfos) {
           /* if(!circulateInfo.getStockCode().equals("605138")){
                continue;
            }*/
            index++;
            System.out.println(circulateInfo.getStockCode()+"======"+index);
            THREAD_POOL_QUOTE_JIHE.execute(() -> {
                StockKbarQuery stockKbarQuery = new StockKbarQuery();
                stockKbarQuery.setStockCode(circulateInfo.getStockCode());
                List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
                String thsStockCode = ThsCommonUtil.getThsStockCode(circulateInfo.getStockCode());
                for (StockKbar stockKbar : stockKbars) {
                    Date dateyyyyMMdd = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                    if (dateyyyyMMdd.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))) {
                        continue;
                    }
                    if (!dateyyyyMMdd.before(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))) {
                        continue;
                    }
                    RedisMonior rediseMin = redisMoniorService.getByRedisKey(stockKbar.getStockCode() + "_" + stockKbar.getKbarDate());
                    if(rediseMin!=null){
                        continue;
                    }
                    if(stockKbar.getKbarDate().equals("20210719")){
                        continue;
                    }
                    String dateStryyyy_MM_dd = DateUtil.format(dateyyyyMMdd, DateUtil.yyyy_MM_dd);
                    String timeStart15 = dateStryyyy_MM_dd + " 09:15:00";
                    String timeEnd15 = dateStryyyy_MM_dd + " 09:15:30";

                    String timeStart20 = dateStryyyy_MM_dd + " 09:20:00";
                    String timeEnd20 = dateStryyyy_MM_dd + " 09:20:30";

                    String timeStart24 = dateStryyyy_MM_dd + " 09:24:00";
                    String timeEnd24 = dateStryyyy_MM_dd + " 09:25:30";

                    String timeEnd = dateStryyyy_MM_dd + " 09:26:00";
                    String quoteStr15 = JDIBridge.THS_Snapshot(thsStockCode, "bid1;bid2;bidSize1;bidSize2;amt;tradeTime;preClose;tradeDate;latest", "", timeStart15, timeEnd15);
                    String quoteStr20 = JDIBridge.THS_Snapshot(thsStockCode, "bid1;bid2;bidSize1;bidSize2;amt;tradeTime;preClose;tradeDate;latest", "", timeStart20, timeEnd20);
                    String quoteStr24 = JDIBridge.THS_Snapshot(thsStockCode, "bid1;bid2;bidSize1;bidSize2;amt;tradeTime;preClose;tradeDate;latest", "", timeStart24, timeEnd24);
                    List<ThsQuoteDTO> quotes15 = convertQuote(quoteStr15, circulateInfo.getStockCode());
                    List<ThsQuoteDTO> quotes20 = convertQuote(quoteStr20, circulateInfo.getStockCode());
                    List<ThsQuoteDTO> quotes24 = convertQuote(quoteStr24, circulateInfo.getStockCode());
                    List<ThsQuoteDTO> list = Lists.newArrayList();
                    if(!CollectionUtils.isEmpty(quotes15)){
                        list.addAll(quotes15);
                    }
                    if(!CollectionUtils.isEmpty(quotes20)){
                        list.addAll(quotes20);
                    }
                    if(!CollectionUtils.isEmpty(quotes24)){
                        list.addAll(quotes24);
                    }
                    wuDiJiheInfo(circulateInfo, stockKbar.getKbarDate(),list);
                }
            });
            System.out.println(circulateInfo.getStockCode()+"======jiesu======"+index);
        }
        try {
            THREAD_POOL_QUOTE_JIHE.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void wuDiJiheInfo(CirculateInfo circulateInfo,String tradeDate,List<ThsQuoteDTO> quotes){
        if(CollectionUtils.isEmpty(quotes)){
            return;
        }
        JiHeWudiDTO jiHeWudiDTO = new JiHeWudiDTO();
        jiHeWudiDTO.setStockCode(circulateInfo.getStockCode());
        for (ThsQuoteDTO quote:quotes){
           if(quote.getTradeTime().startsWith("09:15")){
               if(jiHeWudiDTO.getRate915()==null){
                   if(quote.getBuyOneQuantity()!=null&&quote.getBuyOneQuantity()>0) {
                       BigDecimal rate = PriceUtil.getPricePercentRate(quote.getBuyOnePrice().subtract(quote.getPreEndPrice()), quote.getPreEndPrice());
                       BigDecimal amount = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyOneQuantity()));
                       jiHeWudiDTO.setRate915(rate);
                       jiHeWudiDTO.setAmount915(amount);
                   }
                   if(quote.getBuyTwoQuantity()!=null&&quote.getBuyTwoQuantity()>0) {
                       BigDecimal amount2 = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyTwoQuantity()));
                       jiHeWudiDTO.setBuyTwoAmount915(amount2);
                   }
               }
           }

            if(quote.getTradeTime().startsWith("09:20")){
                if(jiHeWudiDTO.getRate920()==null){
                    if(quote.getBuyOneQuantity()!=null&&quote.getBuyOneQuantity()>0) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(quote.getBuyOnePrice().subtract(quote.getPreEndPrice()), quote.getPreEndPrice());
                        BigDecimal amount = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyOneQuantity()));
                        jiHeWudiDTO.setRate920(rate);
                        jiHeWudiDTO.setAmount920(amount);
                    }
                    if(quote.getBuyTwoQuantity()!=null&&quote.getBuyTwoQuantity()>0) {
                        BigDecimal amount2 = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyTwoQuantity()));
                        jiHeWudiDTO.setBuyTwoAmount920(amount2);
                    }
                }
            }
            if(quote.getTradeTime().startsWith("09:24")){
                if(quote.getBuyOneQuantity()!=null&&quote.getBuyOneQuantity()>0) {
                    BigDecimal rate = PriceUtil.getPricePercentRate(quote.getBuyOnePrice().subtract(quote.getPreEndPrice()), quote.getPreEndPrice());
                    BigDecimal amount = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyOneQuantity()));
                    jiHeWudiDTO.setRate924(rate);
                    jiHeWudiDTO.setAmount924(amount);
                }
                if(quote.getBuyTwoQuantity()!=null&&quote.getBuyTwoQuantity()>0) {
                    BigDecimal amount2 = quote.getBuyOnePrice().multiply(new BigDecimal(quote.getBuyTwoQuantity()));
                    jiHeWudiDTO.setBuyTwoAmount924(amount2);
                }
            }

            if(quote.getTradeAmount()!=null&&quote.getTradeAmount().compareTo(new BigDecimal(1))>0){
                if(jiHeWudiDTO.getRate925()==null){
                    if(quote.getBuyOneQuantity()!=null&&quote.getBuyOneQuantity()>0) {
                        BigDecimal rate = PriceUtil.getPricePercentRate(quote.getCurrentPrice().subtract(quote.getPreEndPrice()), quote.getPreEndPrice());
                        BigDecimal amount = quote.getTradeAmount();
                        jiHeWudiDTO.setRate925(rate);
                        jiHeWudiDTO.setAmount925(amount);
                    }
                    if(quote.getBuyTwoQuantity()!=null&&quote.getBuyTwoQuantity()>0) {
                        BigDecimal amount2 = quote.getBuyTwoPrice().multiply(new BigDecimal(quote.getBuyTwoQuantity()));
                        jiHeWudiDTO.setBuyTwoAmount925(amount2);
                    }
                }
            }

        }
        String str = JSONObject.toJSONString(jiHeWudiDTO);
        RedisMonior redisMonior = new RedisMonior();
        redisMonior.setRedisKey(circulateInfo.getStockCode()+"_"+tradeDate);
        redisMonior.setRedisValue(str);
        redisMonior.setCreateTime(new Date());
        redisMoniorService.save(redisMonior);
    }



    public List<ThsQuoteDTO> convertQuote(String quoteStr,String stockCode){
        ArrayList<ThsQuoteDTO> quotes = Lists.newArrayList();
      if(!StringUtils.isEmpty(quoteStr)){
            JSONObject jsonObject = JSONObject.parseObject(quoteStr);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return quotes;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONArray timeArray = tableJson.getJSONArray("time");
            if(timeArray==null||timeArray.size()==0){
                return quotes;
            }
            List<String> times = timeArray.toJavaList(String.class);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<String> tradeTimes = tableInfo.getJSONArray("tradeTime").toJavaList(String.class);
           // List<BigDecimal> ask1s = tableInfo.getJSONArray("ask1").toJavaList(BigDecimal.class);
            List<BigDecimal> bid1s = tableInfo.getJSONArray("bid1").toJavaList(BigDecimal.class);
            List<BigDecimal> bid2s = tableInfo.getJSONArray("bid2").toJavaList(BigDecimal.class);
            //List<Long> askSize1s = tableInfo.getJSONArray("askSize1").toJavaList(Long.class);
            List<Long> bidSize1s = tableInfo.getJSONArray("bidSize1").toJavaList(Long.class);
            List<Long> bidSize2s = tableInfo.getJSONArray("bidSize2").toJavaList(Long.class);
            List<BigDecimal> preCloses = tableInfo.getJSONArray("preClose").toJavaList(BigDecimal.class);
            List<BigDecimal> amts = tableInfo.getJSONArray("amt").toJavaList(BigDecimal.class);
            List<BigDecimal> latests = tableInfo.getJSONArray("latest").toJavaList(BigDecimal.class);

            int i = 0;
            for (String time:times){
                Date date = DateUtil.parseDate(time, DateUtil.DEFAULT_FORMAT);
                ThsQuoteDTO quote = new ThsQuoteDTO();
                quote.setStockCode(stockCode);
                quote.setQuoteTime(date);
                quote.setTradeDate(DateUtil.format(date, DateUtil.yyyyMMdd));
                quote.setTradeTime(tradeTimes.get(i));
                quote.setCurrentPrice(latests.get(i));
                quote.setPreEndPrice(preCloses.get(i));
                quote.setTradeAmount(amts.get(i));
                quote.setBuyOnePrice(bid1s.get(i));
                quote.setBuyTwoPrice(bid2s.get(i));
                //quote.setSellOnePrice(ask1s.get(i));
                quote.setBuyOneQuantity(bidSize1s.get(i));
                quote.setBuyTwoQuantity(bidSize2s.get(i));
                //quote.setSellOneQuantity(askSize1s.get(i));
                quotes.add(quote);
                i++;
            }
        }
        return quotes;
    }
    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz200", "620865");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("ylz200", "620865");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }


}
