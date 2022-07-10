package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuanShangHighDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String highTime;

}
