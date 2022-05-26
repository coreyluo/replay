package com.bazinga.replay.query;

import com.bazinga.base.ShardingQuery;
import java.util.Date;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;

import java.io.Serializable;

/**
 * 〈StockOrder 查询参数〉<p>
 *
 * @author
 * @date 2022-05-15
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockOrderQuery extends ShardingQuery implements Serializable  {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String thscode;

    /**
     * 
     */
    private String market;

    /**
     * 
     */
    private String secType;

    /**
     *  开始
     */
    private Date dateTrade;

    private String timeTrade;
    private String timeTradeFrom;
    private String timeTradeTo;


    /**
     * 
     */
    private BigDecimal orderAisle;

    /**
     * 
     */
    private BigDecimal orderIndex;

    /**
     * 
     */
    private BigDecimal orderPrice;

    /**
     * 
     */
    private BigDecimal orderVolume;

    /**
     * 
     */
    private String orderType;

    /**
     * 
     */
    private String orderCode;

    /**
     * 
     */
    private String quotationType;

    /**
     * 
     */
    private String secCodeSource;

    /**
     * 
     */
    private String tradeDirection;

    /**
     * 
     */
    private String orderFormType;

    /**
     * 
     */
    private String priceQuoteNum;

    /**
     * 
     */
    private String term;

    /**
     * 
     */
    private String termType;

    /**
     * 
     */
    private String contact;

    /**
     * 
     */
    private String contactInfo;

    /**
     * 
     */
    private BigDecimal seq;

    /**
     * 
     */
    private BigDecimal mseq;


}