package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockPlankDaily〉<p>
 *
 * @author
 * @date 2021-05-09
 */
@lombok.Data
@lombok.ToString
public class StockPlankDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   PRIMARY
     */
    private Long id;

    /**
     * 
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     * 
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date tradeDate;

    /**
     * 1 首板  2 2板  3 3板  4 4板  5 5板以上
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer plankType;

    /**
     * 0 尾盘炸板  1 尾盘封住
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer endStatus;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date insertTime;

    private BigDecimal beforeRateFive;
    private BigDecimal beforeRateTen;
    private BigDecimal beforeRateFifteen;
    private Long exchangeQuantity;

    private BigDecimal max100PriceScale;
    private BigDecimal max100AvgPriceScale;
    private BigDecimal min15PriceScale;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date updateTime;


}
