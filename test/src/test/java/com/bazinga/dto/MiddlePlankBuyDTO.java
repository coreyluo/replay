package com.bazinga.dto;


import com.bazinga.replay.dto.KBarDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MiddlePlankBuyDTO {

    /**
     *
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

    private int counts;

    private List<MinuteSumDTO> minuteSumDTOS;

}
