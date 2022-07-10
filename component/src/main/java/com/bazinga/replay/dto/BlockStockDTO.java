package com.bazinga.replay.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 〈CirculateInfo〉<p>
 *
 * @author
 * @date 2021-05-10
 */
@lombok.Data
@lombok.ToString
public class BlockStockDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String stockCode;
    private String stockName;
    private Integer status;
    private String marketDate;
}
