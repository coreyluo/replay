package com.bazinga.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.dto.JiHeWudiDTO;
import com.bazinga.dto.JiHeWudiTotalDTO;
import com.bazinga.dto.ThsQuoteDTO;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.dto.HuShen300ExcelDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
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
public class DisableSellUtilComponent {
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
        File file = new File("D:/circulate/sellAvailableStock.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/sellAvailableStock.xlsx" + "不存在");
        }
        try {
            int i = 0;
            List<HuShen300ExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(HuShen300ExcelDTO.class);
            for (HuShen300ExcelDTO item:dataList){
                CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
                circulateInfoQuery.setStockCode(item.getStockCode());
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
                CirculateInfo circulateInfo = circulateInfos.get(0);
                StockKbar stockKbar = stockKbarService.getByUniqueKey(circulateInfo.getStockCode() + "_20220425");
                BigDecimal multiply = stockKbar.getClosePrice().multiply(new BigDecimal(circulateInfo.getCirculate()));
                if(multiply.compareTo(new BigDecimal("100000000000"))>0){
                    i++;
                    String str = "INSERT INTO `disable_sell_stock_pool` (`stock_code`, `stock_name`, `create_time`, `update_time`) VALUES ('"+circulateInfo.getStockCode()+"', '"+circulateInfo.getStockName()+"', '2022-04-27 20:51:39', '2022-04-27 20:51:42');";
                    System.out.println(str);
                }
            }
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }



}
