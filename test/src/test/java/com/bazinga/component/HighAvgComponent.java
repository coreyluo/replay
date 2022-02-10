package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.constant.DateConstant;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
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
public class HighAvgComponent {
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

    public static Map<String,Map<String,BlockLevelDTO>> levelMap = new ConcurrentHashMap<>(8192);


    public void highThanAvgBuys(){
        List<HighThanAvgBuyDTO> dailys = Lists.newArrayList();
        List<String> minutes = tradeTimeInfo();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos){
            System.out.println(circulateInfo.getStockCode());
            List<HighThanAvgBuyDTO> fastPlank = getHighTanAvgBuys(circulateInfo,minutes);
            dailys.addAll(fastPlank);
            if(dailys.size()>=100){
                break;
            }
        }

        List<Object[]> datas = Lists.newArrayList();
        for(HighThanAvgBuyDTO dto:dailys){
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getMarketMoney());
            list.add(dto.getTradeDate());
            list.add(dto.getNQuantity());
            list.add(dto.getMQuantity());
            list.add(dto.getXTimes());
            list.add(dto.getQuantityUpperTimes());
            list.add(dto.getAvgPrice());
            list.add(dto.getBuyTimeStr());
            list.add(dto.getBuyRate());
            list.add(dto.getXMThanN());
            list.add(dto.getBeforeDay8());
            list.add(dto.getBeforeDay18());
            list.add(dto.getBeforeDay38());
            list.add(dto.getBeforeDay88());
            list.add(dto.getBefore1AvgThanN());
            list.add(dto.getBefore5AvgThanN());
            list.add(dto.getBefore15AvgThanN());
            list.add(dto.getBefore1AvgThanM());
            list.add(dto.getBefore5AvgThanM());
            list.add(dto.getBefore15AvgThanM());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","stockCode","stockName","流通市值","买入日期","N量","M量","买入时候的x值","买入前量符合次数",
                "买入时均价","买入时候时间戳","买入时候涨幅", "XM/N","8天前涨幅","18天前涨幅","38天前涨幅","88天前涨幅",
                "前1日均量/N","前5日均量/N","前15日均量/N","前1日均量/M","前5日均量/M","前15日均量/M","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("量价买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("量价买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<HighThanAvgBuyDTO> getHighTanAvgBuys(CirculateInfo circulateInfo,List<String> minutes){
        List<HighThanAvgBuyDTO> list = Lists.newArrayList();
        List<StockKbar> stockKBars = getStockKBars(circulateInfo.getStockCode(),250);
        if(CollectionUtils.isEmpty(stockKBars)){
            return list;
        }
        LimitQueue<StockKbar> limitQueue6 = new LimitQueue<>(6);
        LimitQueue<StockKbar> limitQueue16 = new LimitQueue<>(16);
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKBars){
            limitQueue6.offer(stockKbar);
            limitQueue16.offer(stockKbar);
            if(preStockKbar!=null) {
                if(DateUtil.parseDate(stockKbar.getKbarDate(),DateUtil.yyyyMMdd).after(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd))){
                    HighThanAvgBuyDTO buyDTO = new HighThanAvgBuyDTO();
                    buyDTO.setStockCode(circulateInfo.getStockCode());
                    buyDTO.setStockName(circulateInfo.getStockName());
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    List<ThirdSecondTransactionDataDTO> transactions = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                    needBuy(transactions, buyDTO, minutes, stockKbar, preStockKbar, circulateInfo);
                    if (buyDTO.getBuyTimeStr() != null) {
                        beforeRateInfo(buyDTO, stockKbar, stockKBars);
                        list.add(buyDTO);
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        return list;
    }
    public void beforeRateInfo(HighThanAvgBuyDTO buyDTO,StockKbar stockKbar, List<StockKbar> bars){
        List<StockKbar> reverse = Lists.reverse(bars);
        StockKbar preKbar = null;
        boolean flag = false;
        int i = 0;
        StockKbar endKbar = null;
        Long totalQuantity = 0l;
        for (StockKbar bar:reverse){
            if(flag){
                i++;
                totalQuantity = totalQuantity+bar.getTradeQuantity();
            }
            if(i==1){
                endKbar = bar;
                if(totalQuantity>0){
                    long avgMinQuantity = totalQuantity / (i * 240);
                    if(buyDTO.getNQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getNQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore1AvgThanN(divide);
                    }
                    if(buyDTO.getMQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getMQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore1AvgThanM(divide);
                    }
                }
            }
            if(i==5){
                if(totalQuantity>0){
                    long avgMinQuantity = totalQuantity / (i * 240);
                    if(buyDTO.getNQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getNQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore5AvgThanN(divide);
                    }
                    if(buyDTO.getMQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getMQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore5AvgThanM(divide);
                    }
                }
            }
            if(i==15){
                if(totalQuantity>0){
                    long avgMinQuantity = totalQuantity / (i * 240);
                    if(buyDTO.getNQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getNQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore15AvgThanN(divide);
                    }
                    if(buyDTO.getMQuantity()!=null) {
                        BigDecimal divide = new BigDecimal(avgMinQuantity).divide(new BigDecimal(buyDTO.getMQuantity()),2,BigDecimal.ROUND_HALF_UP);
                        buyDTO.setBefore15AvgThanM(divide);
                    }
                }
            }
            if(i==9){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay8(rate);
            }
            if(i==19){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay18(rate);
            }
            if(i==39){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay38(rate);
            }
            if(i==89){
                BigDecimal rate = PriceUtil.getPricePercentRate(endKbar.getAdjClosePrice().subtract(bar.getAdjClosePrice()), bar.getAdjClosePrice());
                buyDTO.setBeforeDay88(rate);
                return;
            }
            if(bar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
                if(preKbar!=null){
                    BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(buyDTO.getStockCode(), preKbar.getKbarDate());
                    BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, preKbar);
                    if(chuQuanAvgPrice!=null) {
                        BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                        buyDTO.setProfit(profit);
                    }
                }
            }
            preKbar = bar;
        }
    }





    public void needBuy (List<ThirdSecondTransactionDataDTO> datas,HighThanAvgBuyDTO buyDTO,List<String> minutes,StockKbar stockKbar,StockKbar preStockKbar,CirculateInfo circulateInfo){
        if(CollectionUtils.isEmpty(datas)){
            return ;
        }
        Integer nQuantity = null;
        BigDecimal totalMoney = BigDecimal.ZERO;
        int totalCount = 0;
        Map<String, MinuteQuantityDTO> map = new HashMap<>();
        for (ThirdSecondTransactionDataDTO data:datas){
            totalCount = totalCount+data.getTradeQuantity();
            BigDecimal money = new BigDecimal(data.getTradeQuantity() * 100).multiply(data.getTradePrice());
            totalMoney = totalMoney.add(money);
            BigDecimal avgPrice = null;
            if(totalCount>0){
                avgPrice = totalMoney.divide(new BigDecimal(totalCount*100),2,BigDecimal.ROUND_HALF_UP);
            }
            if(data.getTradeTime().equals("09:25")){
                nQuantity = data.getTradeQuantity();
                continue;
            }
            if(PriceUtil.isUpperPrice(stockKbar.getStockCode(),data.getTradePrice(),preStockKbar.getClosePrice())&&data.getTradeType()==1){
                return;
            }
            if(data.getTradeTime().equals("09:30")){
                if(nQuantity==null){
                    nQuantity = data.getTradeQuantity();
                }else{
                    nQuantity = nQuantity+data.getTradeQuantity();
                }
                continue;
            }

            MinuteQuantityDTO quantityDTO = map.get(data.getTradeTime());
            if(quantityDTO==null){
                quantityDTO = new MinuteQuantityDTO();
                quantityDTO.setTradeTime(data.getTradeTime());
                map.put(data.getTradeTime(),quantityDTO);
            }
            quantityDTO.setQuantity(quantityDTO.getQuantity()+data.getTradeQuantity());
            quantityDTO.setAvgPrice(avgPrice);
            quantityDTO.setTradePrice(data.getTradePrice());

        }
        if(nQuantity==null||nQuantity==0){
            return;
        }
        int xTime = 0;
        int quantityUpperTimes = 0;
        for (String minute:minutes){
            xTime++;
            MinuteQuantityDTO dto = map.get(minute);
            if(dto!=null&&dto.getQuantity()>nQuantity){
                quantityUpperTimes++;
            }
            if(dto!=null&&dto.getTradePrice().compareTo(dto.getAvgPrice())>=0&&dto.getQuantity()>nQuantity){
                buyDTO.setBuyTimeStr(minute);
                buyDTO.setNQuantity(nQuantity);
                buyDTO.setMQuantity(dto.getQuantity());
                buyDTO.setQuantityUpperTimes(quantityUpperTimes);
                buyDTO.setXTimes(xTime);
                buyDTO.setAvgPrice(dto.getAvgPrice());
                buyDTO.setXMThanN(new BigDecimal(dto.getQuantity()).divide(new BigDecimal(nQuantity),2,BigDecimal.ROUND_HALF_UP));
                BigDecimal chuQuanTradePrice = chuQuanAvgPrice(dto.getTradePrice(), stockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanTradePrice.subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                buyDTO.setBuyRate(rate);
                BigDecimal marketMoney = (new BigDecimal(circulateInfo.getCirculateZ()).multiply(dto.getTradePrice())).divide(new BigDecimal(100000000), 2, BigDecimal.ROUND_HALF_UP);
                buyDTO.setMarketMoney(marketMoney);
                return;
            }
        }

    }
    public static List<String> tradeTimeInfo(){
        List<String> minutes = Lists.newArrayList();
        Date am093000 = DateConstant.AM_09_30_00;
        for (int i = 0;i<=350;i++){
            if((i>0&&i<=120)||(i>=210&&i<=326)) {
                Date date = DateUtil.addMinutes(am093000, i);
                String format = DateUtil.format(date, DateUtil.HH_MM);
                minutes.add(format);
            }
        }
        return minutes;
    }


    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,StockKbar kbar){
        BigDecimal reason = null;
        if(!(kbar.getClosePrice().equals(kbar.getAdjClosePrice()))&&!(kbar.getOpenPrice().equals(kbar.getAdjOpenPrice()))){
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }
    }

    public List<StockKbar> getStockKBars(String stockCode,int size){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.DESC);
            query.setLimit(size);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            List<StockKbar> reverse = Lists.reverse(stockKbars);
            List<StockKbar> list = deleteNewStockTimes(reverse, size);
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



}
