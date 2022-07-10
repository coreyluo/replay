package com.bazinga.replay.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 〈CirculateInfo〉<p>
 *
 * @author
 * @date 2021-05-10
 */
@lombok.Data
@lombok.ToString
public class MacdIndexDTO implements Serializable {
    private String stockCode;
    private String tradeDate;
    private BigDecimal ema12;
    private BigDecimal ema26;
    private BigDecimal diff;
    private BigDecimal dea;
    private BigDecimal bar;
}
