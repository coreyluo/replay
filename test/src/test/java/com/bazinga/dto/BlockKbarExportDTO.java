package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BlockKbarExportDTO {

    @ExcelElement("股票代码")
    private String stockCode;
    @ExcelElement("股票名称")
    private String stockName;
    @ExcelElement("板块代码")
    private String blockCode;
    @ExcelElement("板块名称")
    private String blockName;
    @ExcelElement("板块排名")
    private Integer blockNum;
    @ExcelElement("板块涨幅")
    private BigDecimal blockRate;
    @ExcelElement("一日收益")
    private BigDecimal premium;
}
