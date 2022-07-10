package com.bazinga.replay.convert;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsDataConvert {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;


    public List<BlockStockDTO> getDEA(String blockCode,String blockName){
       
        return null;
    }


}
