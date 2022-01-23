package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeiDaoRateDTO {

    private String tradeTime;
    private BigDecimal buyPrice;
    private BigDecimal profit;


}
