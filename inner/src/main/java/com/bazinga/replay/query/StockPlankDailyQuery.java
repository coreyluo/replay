package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockPlankDaily 查询参数〉<p>
 *
 * @author
 * @date 2021-05-09
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockPlankDailyQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String stockCode;

    /**
     * 
     */
    private String stockName;

    /**
     *  开始
     */
    private Date tradeDateFrom;

    /**
     *  结束
     */
    private Date tradeDateTo;

    /**
     * 1 首板  2 2板  3 3板  4 4板  5 5板以上
     */
    private Integer plankType;
    private Integer plankSign;

    /**
     * 0 尾盘炸板  1 尾盘封住
     */
    private Integer endStatus;

    /**
     *  开始
     */
    private Date insertTimeFrom;

    /**
     *  结束
     */
    private Date insertTimeTo;

    private BigDecimal beforeRateFive;
    private BigDecimal beforeRateTen;
    private BigDecimal beforeRateFifteen;
    private Long exchangeQuantity;

    private BigDecimal max100PriceScale;
    private BigDecimal max100AvgPriceScale;
    private BigDecimal min15PriceScale;

    /**
     *  开始
     */
    private Date createTimeFrom;

    /**
     *  结束
     */
    private Date createTimeTo;

    /**
     *  开始
     */
    private Date updateTimeFrom;

    /**
     *  结束
     */
    private Date updateTimeTo;


}
