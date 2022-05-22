package com.bazinga.replay.model;

import java.util.Date;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;

import java.io.Serializable;

/**
 * 〈StockOrder〉<p>
 *
 * @author
 * @date 2022-05-15
 */
@lombok.Data
@lombok.ToString
public class StockOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   YES
     * @是否索引   YES
     */
    private String thscode;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   YES
     * @是否索引   NO
     */
    private String market;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String secType;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date dateTrade;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String timeTrade;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal orderAisle;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal orderIndex;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal orderPrice;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal orderVolume;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String orderType;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String orderCode;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String quotationType;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String secCodeSource;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String tradeDirection;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String orderFormType;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String priceQuoteNum;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String term;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String termType;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String contact;

    /**
     * 
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String contactInfo;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal seq;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal mseq;


}