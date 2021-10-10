package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MinuteSumDTO {

    private String tradeTime;
    private BigDecimal avgRate;
    private BigDecimal totalRate;
    private Integer count;



}
