package com.bazinga.replay.query;



import com.bazinga.base.PagingQuery;

import java.io.Serializable;

/**
 * 〈BondConvertPriceAdjust 查询参数〉<p>
 *
 * @author
 * @date 2022-01-04
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BondConvertPriceAdjustQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String pubDate;

    /**
     * 
     */
    private String adjustDate;

    /**
     * 
     */
    private String newConvertPrice;

    /**
     * 
     */
    private String adjustReason;


}