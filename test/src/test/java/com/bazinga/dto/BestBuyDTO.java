package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestBuyDTO {


    private String reason1;

    private String reason2;

    private String reason3;

    private String reason4;

    private String reason5;

    private BigDecimal profit;

    private BigDecimal profitTotal;

    private int count;


}
