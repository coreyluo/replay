package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.FastRaiseBestDTO;
import com.bazinga.dto.RaiseAndDropBestDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.HttpClientUtils;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsHttpTestComponent {
    @Autowired
    private HttpClientUtils httpClientUtils;
    public void getAccessToken(){
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("refresh_token","eyJzaWduX3RpbWUiOiIyMDIyLTAzLTMxIDIxOjM2OjQ5In0=.eyJ1aWQiOiI2MjgwOTM0MTMifQ==.45A3BE37D1D666B88582D72D6AB5E7B6F1EBD7945468958EEE5B32ED7293AB28");
        headers.put("Content-Type","application/json");
        JSONObject jsonObject = httpClientUtils.sendPost("https://ft.10jqka.com.cn/api/v1/get_access_token", params, headers);
        System.out.println(jsonObject);
    }

    public void getBas(){
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("refresh_token","eyJzaWduX3RpbWUiOiIyMDIyLTAzLTMxIDIxOjM2OjQ5In0=.eyJ1aWQiOiI2MjgwOTM0MTMifQ==.45A3BE37D1D666B88582D72D6AB5E7B6F1EBD7945468958EEE5B32ED7293AB28");
        headers.put("Content-Type","application/json");
        JSONObject jsonObject = httpClientUtils.sendPost("https://ft.10jqka.com.cn/api/v1/get_access_token", params, headers);
        System.out.println(jsonObject);
    }


}
