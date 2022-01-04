package com.bazinga.replay.model;


import java.io.Serializable;

/**
 * 〈BondConvertPriceAdjust〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.ToString
public class BondConvertPriceAdjust implements Serializable {

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
    private String name;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String pubDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String adjustDate;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String newConvertPrice;

    /**
     * 
     *
     * @最大长度   65535
     * @允许为空   YES
     * @是否索引   NO
     */
    private String adjustReason;


}