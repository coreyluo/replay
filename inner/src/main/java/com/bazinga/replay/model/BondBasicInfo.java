package com.bazinga.replay.model;

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
 * 〈BondBasicInfo〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.ToString
public class BondBasicInfo implements Serializable {

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
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String code;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String shortName;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String fullName;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long listStatusId;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String listStatus;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String issuer;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String companyCode;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date issueStartDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date issueEndDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double planRaiseFund;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double actualRaiseFund;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long issuePar;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double issuePrice;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long isGuarantee;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String fundRaisingPurposes;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date listDeclareDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String convertPriceReason;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double convertPrice;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date convertStartDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date convertEndDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String convertCode;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double coupon;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long exchangeCode;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String exchange;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String currencyId;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long couponTypeId;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String couponType;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long couponFrequency;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long paymentTypeId;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String paymentType;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Double par;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long repaymentPeriod;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long bondTypeId;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String bondType;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Long bondFormId;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String bondForm;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date listDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date delistDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date interestBeginDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date maturityDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String interestDate;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date lastCashDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String cashComment;


}