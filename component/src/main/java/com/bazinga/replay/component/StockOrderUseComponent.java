package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.BigExchangeTestBuyDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.StockOrder;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ShStockOrderQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.StockOrderQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockOrderUseComponent {
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
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ShStockOrderService shStockOrderService;
    @Autowired
    private StockOrderService stockOrderService;




}
