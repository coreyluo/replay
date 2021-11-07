package com.bazinga.replay.component;


import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.DragonTigerDaily;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.DragonTigerDailyQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.DragonTigerDailyService;
import com.bazinga.replay.util.JoinQuantUtil;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class DragonTigerDailyComponent {

    @Autowired
    private DragonTigerDailyService dragonTigerDailyService;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private CommonComponent commonComponent;

    /**
     * 106001	涨幅偏离值达7%的证券
     * 106002	跌幅偏离值达7%的证券
     * 106003	日价格振幅达到15%的证券
     * 106004	换手率达20%的证券
     * 106005	无价格涨跌幅限制的证券
     * 106006	连续三个交易日内收盘价格涨幅偏离值累计达到20%的证券
     * 106007	连续三个交易日内收盘价格跌幅偏离值累计达到20%的证券
     * 106008	连续三个交易日内收盘价格涨幅偏离值累计达到15%的证券
     * 106009	连续三个交易日内收盘价格跌幅偏离值累计达到15%的证券
     * 106010	连续三个交易日内涨幅偏离值累计达到12%的ST证券、*ST证券和未完成股改证券
     * 106011	连续三个交易日内跌幅偏离值累计达到12%的ST证券、*ST证券和未完成股改证券
     * 106012	连续三个交易日的日均换手率与前五个交易日日均换手率的比值到达30倍
     * 106013	单只标的证券的当日融资买入数量达到当日该证券总交易量的50％以上的证券
     * 106014	单只标的证券的当日融券卖出数量达到当日该证券总交易量的50％以上的证券
     * 106015	日价格涨幅达到20%的证券
     * 106016	日价格跌幅达到-15%的证券
     * 106099	其它异常波动的证券
     */
    public void  save2Db(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Date preTradeDate = commonComponent.preTradeDate(new Date());
        String kbarDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
        try {
            String token = JoinQuantUtil.getToken();
            for (CirculateInfo circulateInfo : circulateInfos) {
              /*  if(!"300264".equals(circulateInfo.getStockCode())){
                    continue;
                }*/
                String quantStockCode = circulateInfo.getStockCode().startsWith("6")? circulateInfo.getStockCode() + ".XSHG":circulateInfo.getStockCode() + ".XSHE";
                String quantDate = DateUtil.format(preTradeDate,DateUtil.yyyy_MM_dd);
                String dragonTigerString = JoinQuantUtil.getDragonTiger(quantStockCode, quantDate, token);

                String[] dragonTigerArray = dragonTigerString.split(" ");
                if(dragonTigerArray.length<2){
                    continue;
                }
                for (int i = 1; i < dragonTigerArray.length; i++) {
                    String objectString = dragonTigerArray[i];
                    log.info("{}",objectString);
                    String[] objArr = objectString.split(SymbolConstants.COMMA);
                    if("BUY".equals(objArr[2])){
                        DragonTigerDaily dragonTiger = new DragonTigerDaily();
                        dragonTiger.setStockCode(circulateInfo.getStockCode());
                        dragonTiger.setStockName(circulateInfo.getStockName());
                        dragonTiger.setKbarDate(kbarDate);
                        dragonTiger.setRank(Integer.valueOf(objArr[3]));
                        dragonTiger.setAbnormalCode(objArr[4]);
                        dragonTiger.setReason(objArr[5]);
                        dragonTiger.setChair(objArr[6]);
                        if("财通证券股份有限公司杭州上塘路证券营业部".equals(dragonTiger.getChair())){
                            if("106001".equals(dragonTiger.getAbnormalCode()) && dragonTiger.getRank()<3){
                                DragonTigerDaily byUnique =  getByUniqueKey(circulateInfo.getStockCode(),kbarDate,dragonTiger.getAbnormalCode());
                                if(byUnique == null){
                                    dragonTigerDailyService.save(dragonTiger);
                                }
                            }
                        }

                        if("华鑫证券有限责任公司上海分公司".equals(dragonTiger.getChair())){
                            if("106015".equals(dragonTiger.getAbnormalCode()) && dragonTiger.getRank()<5 ){
                                DragonTigerDaily byUnique =  getByUniqueKey(circulateInfo.getStockCode(),kbarDate,dragonTiger.getAbnormalCode());
                                if(byUnique == null){
                                    dragonTigerDailyService.save(dragonTiger);
                                }
                            }

                        }
                    }
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DragonTigerDaily getByUniqueKey(String stockCode, String kbarDate,String abNormalCode) {

        DragonTigerDailyQuery query = new DragonTigerDailyQuery();
        query.setStockCode(stockCode);
        query.setKbarDate(kbarDate);
        query.setAbnormalCode(abNormalCode);
        List<DragonTigerDaily> dragonTigerDailies = dragonTigerDailyService.listByCondition(query);
        if(CollectionUtils.isEmpty(dragonTigerDailies)){
            return null;
        }
        return dragonTigerDailies.get(0);

    }

}
