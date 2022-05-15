package com.bazinga.replay.model;

import java.util.Date;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;

import java.io.Serializable;

/**
 * 〈ShStockOrder〉<p>
 *
 * @author
 * @date 2022-04-26
 */
@lombok.Data
@lombok.ToString
public class ShStockOrder implements Serializable {

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
     * @最大长度   100
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
     * @最大长度   28
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
    private BigDecimal orderSeq;

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
     * @最大长度   28
     * @允许为空   YES
     * @是否索引   NO
     */
    private String orderType;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal originalOrderSeq;

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
    private BigDecimal remainOrderVolume;

    /**
     * 
     *
     * @最大长度   28
     * @允许为空   YES
     * @是否索引   NO
     */
    private String orderDirection;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal businessSeq;

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