package com.bazinga.dto;


import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthDTO<MonthD> {
    private String startMonth;
    private String endMonth;


}
