package com.bazinga.replay.dto;

import java.io.Serializable;

/**
 * 〈CirculateInfo〉<p>
 *
 * @author
 * @date 2021-05-10
 */
@lombok.Data
@lombok.ToString
public class ThsStockIndexDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String stockCode;
    private String stockName;
    private String tradeDate ;
    private String marketDate;
}
