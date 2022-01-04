package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈BondBasicInfo 查询参数〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BondBasicInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private String shortName;

    /**
     * 
     */
    private String fullName;

    /**
     * 
     */
    private Long listStatusId;

    /**
     * 
     */
    private String listStatus;

    /**
     * 
     */
    private String issuer;

    /**
     * 
     */
    private String companyCode;

    /**
     *  开始
     */
    private Date issueStartDateFrom;

    /**
     *  结束
     */
    private Date issueStartDateTo;

    /**
     *  开始
     */
    private Date issueEndDateFrom;

    /**
     *  结束
     */
    private Date issueEndDateTo;

    /**
     * 
     */
    private Double planRaiseFund;

    /**
     * 
     */
    private Double actualRaiseFund;

    /**
     * 
     */
    private Long issuePar;

    /**
     * 
     */
    private Double issuePrice;

    /**
     * 
     */
    private Long isGuarantee;

    /**
     * 
     */
    private String fundRaisingPurposes;

    /**
     *  开始
     */
    private Date listDeclareDateFrom;

    /**
     *  结束
     */
    private Date listDeclareDateTo;

    /**
     * 
     */
    private String convertPriceReason;

    /**
     * 
     */
    private Double convertPrice;

    /**
     *  开始
     */
    private Date convertStartDateFrom;

    /**
     *  结束
     */
    private Date convertStartDateTo;

    /**
     *  开始
     */
    private Date convertEndDateFrom;

    /**
     *  结束
     */
    private Date convertEndDateTo;

    /**
     * 
     */
    private String convertCode;

    /**
     * 
     */
    private Double coupon;

    /**
     * 
     */
    private Long exchangeCode;

    /**
     * 
     */
    private String exchange;

    /**
     * 
     */
    private String currencyId;

    /**
     * 
     */
    private Long couponTypeId;

    /**
     * 
     */
    private String couponType;

    /**
     * 
     */
    private Long couponFrequency;

    /**
     * 
     */
    private Long paymentTypeId;

    /**
     * 
     */
    private String paymentType;

    /**
     * 
     */
    private Double par;

    /**
     * 
     */
    private Long repaymentPeriod;

    /**
     * 
     */
    private Long bondTypeId;

    /**
     * 
     */
    private String bondType;

    /**
     * 
     */
    private Long bondFormId;

    /**
     * 
     */
    private String bondForm;

    /**
     *  开始
     */
    private Date listDateFrom;

    /**
     *  结束
     */
    private Date listDateTo;

    /**
     *  开始
     */
    private Date delistDateFrom;

    /**
     *  结束
     */
    private Date delistDateTo;

    /**
     *  开始
     */
    private Date interestBeginDateFrom;

    /**
     *  结束
     */
    private Date interestBeginDateTo;

    /**
     *  开始
     */
    private Date maturityDateFrom;

    /**
     *  结束
     */
    private Date maturityDateTo;

    /**
     * 
     */
    private String interestDate;

    /**
     *  开始
     */
    private Date lastCashDateFrom;

    /**
     *  结束
     */
    private Date lastCashDateTo;

    /**
     * 
     */
    private String cashComment;


}