package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockKbarImportDTO {

    @ExcelElement("blockRate")
    private BigDecimal blockRate;


    @ExcelElement("premium")
    private BigDecimal premium;

    @ExcelElement("blockNum")
    private Integer blockNum;

}
