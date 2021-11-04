package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RotPlankExportDTO {

    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券代码")
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券名称")
    private String stockName;

    private BigDecimal shRate;

    private BigDecimal shOpenRate;

    private BigDecimal buyPrice;

    private BigDecimal openRate;

    @ExcelElement("买入日期")
    private String kbarDate;

    @ExcelElement("板高")
    private Integer plankHigh;

    @ExcelElement("断板天数")
    private Integer unPlankHigh;

    @ExcelElement("第一次板时间")
    private String firstPlankTime;

    @ExcelElement("第n次板时间")
    private String nPlankTime;

    @ExcelElement("盈利")
    private BigDecimal premium;

    @ExcelElement("封板")
    private Integer sealType;

    @ExcelElement("流通z")
    private Long circulateZ;
}
